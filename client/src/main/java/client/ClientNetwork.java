package client;

import common.SerializationUtils;
import common.dto.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.Map;

/**
 * UDP-транспорт клиентского приложения.
 *
 * <p>Все запросы сериализуются (один активный обмен за раз) — важно для UDP-туннеля и GUI.</p>
 */
public class ClientNetwork implements AutoCloseable {

    public static final int GUI_REQUEST_TIMEOUT_MS = 12_000;
    public static final int GUI_RECEIVE_TIMEOUT_MS = 35_000;
    public static final int GUI_MAX_RETRIES = 3;

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int DEFAULT_REQUEST_TIMEOUT_MS = 25_000;
    private static final int DEFAULT_RECEIVE_TIMEOUT_MS = 60_000;
    private static final int PACKET_IDLE_MS = 6_000;
    private static final int MAX_RETRIES = 6;
    private static final boolean DEBUG = "1".equals(System.getenv("LAB5_CLIENT_NET_DEBUG"));

    private final DatagramChannel channel;
    private final SocketAddress serverAddress;
    private final Object sendLock = new Object();
    private final int requestTimeoutMs;
    private final int receiveTimeoutMs;
    private final int maxRetries;

    public ClientNetwork(String host, int port) throws IOException {
        this(host, port, DEFAULT_REQUEST_TIMEOUT_MS, DEFAULT_RECEIVE_TIMEOUT_MS, MAX_RETRIES);
    }

    public ClientNetwork(String host, int port, int requestTimeoutMs, int receiveTimeoutMs, int maxRetries)
            throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.channel.bind(null);
        String connectHost = "localhost".equalsIgnoreCase(host) ? "127.0.0.1" : host;
        this.serverAddress = new InetSocketAddress(connectHost, port);
        this.requestTimeoutMs = requestTimeoutMs;
        this.receiveTimeoutMs = receiveTimeoutMs;
        this.maxRetries = maxRetries;
    }

    public static ClientNetwork forGui(String host, int port) throws IOException {
        return new ClientNetwork(host, port, GUI_REQUEST_TIMEOUT_MS, GUI_RECEIVE_TIMEOUT_MS, GUI_MAX_RETRIES);
    }

    public CommandResponseDTO sendAndReceive(CommandDTO dto) throws IOException, ClassNotFoundException {
        synchronized (sendLock) {
            drainStaleDatagrams();

            byte[] data = SerializationUtils.serialize(dto);
            IOException lastIo = null;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                if (attempt > 1) {
                    drainStaleDatagrams();
                }
                String requestId = "req-" + System.currentTimeMillis() + "-" + attempt;
                Packet requestPacket = new Packet(requestId, 0, 1, PacketType.DATA, data);
                ByteBuffer out = SerializationUtils.serializeToBuffer(requestPacket);
                channel.send(out, serverAddress);
                if (DEBUG) {
                    System.out.println("[ClientNetwork] sent request " + requestId + " to " + serverAddress);
                }

                try {
                    CommandResponseDTO response = receiveResponse(requestTimeoutMs);
                    if (response != null) {
                        return response;
                    }
                } catch (IOException e) {
                    lastIo = e;
                }
            }

            if (lastIo != null) {
                throw lastIo;
            }
            throw new IOException("Сервер не отвечает (проверьте туннель и ./lab5 status на Helios).");
        }
    }

    private void drainStaleDatagrams() throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
        for (int i = 0; i < 64; i++) {
            buf.clear();
            SocketAddress from = channel.receive(buf);
            if (from == null) {
                break;
            }
        }
    }

    private CommandResponseDTO receiveResponse(int firstPacketTimeoutMs) throws IOException, ClassNotFoundException {
        long firstPacketDeadline = System.currentTimeMillis() + firstPacketTimeoutMs;
        long deadline = System.currentTimeMillis() + receiveTimeoutMs;
        String transactionId = null;
        int totalPackets = -1;
        Map<Integer, byte[]> receivedChunks = new HashMap<>();
        long lastPacketAt = System.currentTimeMillis();
        ByteBuffer in = ByteBuffer.allocate(BUFFER_SIZE);

        while (System.currentTimeMillis() < deadline) {
            if (transactionId == null && System.currentTimeMillis() > firstPacketDeadline) {
                break;
            }
            if (transactionId != null && System.currentTimeMillis() - lastPacketAt > PACKET_IDLE_MS) {
                break;
            }

            in.clear();
            SocketAddress from = channel.receive(in);
            if (from == null) {
                sleepQuietly(10);
                continue;
            }
            if (!isFromServer(from)) {
                if (transactionId != null && System.currentTimeMillis() - lastPacketAt > PACKET_IDLE_MS) {
                    break;
                }
                if (DEBUG) {
                    System.out.println("[ClientNetwork] dropped packet from " + from + " (expected " + serverAddress + ")");
                }
                continue;
            }

            in.flip();
            byte[] raw = new byte[in.remaining()];
            in.get(raw);

            Object obj = deserializeAny(raw);
            if (obj instanceof AckPacket) {
                if (DEBUG) {
                    System.out.println("[ClientNetwork] got ACK from " + from);
                }
                continue;
            }
            if (!(obj instanceof Packet packet)) {
                if (DEBUG) {
                    System.out.println("[ClientNetwork] got unexpected payload type: " + obj.getClass().getName());
                }
                continue;
            }
            if (DEBUG) {
                System.out.println("[ClientNetwork] got packet " + packet.getPacketIndex() + "/" + packet.getTotalPackets()
                        + " tx=" + packet.getTransactionId() + " from " + from);
            }

            if (transactionId == null) {
                transactionId = packet.getTransactionId();
                totalPackets = packet.getTotalPackets();
            }
            if (!packet.getTransactionId().equals(transactionId)) {
                continue;
            }

            int idx = packet.getPacketIndex();
            if (!receivedChunks.containsKey(idx)) {
                sendAck(transactionId, idx);
            }
            receivedChunks.put(idx, packet.getData());
            lastPacketAt = System.currentTimeMillis();

            if (receivedChunks.size() == totalPackets) {
                byte[] fullData = assembleChunks(receivedChunks, totalPackets);
                return deserializeResponse(fullData);
            }
        }

        return null;
    }

    private boolean isFromServer(SocketAddress from) {
        if (!(from instanceof InetSocketAddress fromInet)
                || !(serverAddress instanceof InetSocketAddress serverInet)) {
            return from.equals(serverAddress);
        }
        if (fromInet.getPort() != serverInet.getPort()) {
            return false;
        }
        var fromAddr = fromInet.getAddress();
        if (fromAddr != null && fromAddr.isLoopbackAddress()) {
            return true;
        }
        if (from.equals(serverAddress)) {
            return true;
        }
        var serverAddr = serverInet.getAddress();
        if (fromAddr != null && serverAddr != null) {
            return fromAddr.equals(serverAddr);
        }
        return fromInet.getHostString().equalsIgnoreCase(serverInet.getHostString());
    }

    private void sendAck(String transactionId, int packetIndex) throws IOException {
        AckPacket ack = new AckPacket(transactionId, packetIndex, PacketType.ACK);
        channel.send(SerializationUtils.serializeToBuffer(ack), serverAddress);
    }

    private byte[] assembleChunks(Map<Integer, byte[]> chunks, int total) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < total; i++) {
            byte[] chunk = chunks.get(i);
            if (chunk != null) {
                baos.write(chunk, 0, chunk.length);
            }
        }
        return baos.toByteArray();
    }

    private CommandResponseDTO deserializeResponse(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (CommandResponseDTO) ois.readObject();
        }
    }

    private Object deserializeAny(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    private void sleepQuietly(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
