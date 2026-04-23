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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientNetwork implements AutoCloseable {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 3;

    private final DatagramChannel channel;
    private final SocketAddress serverAddress;

    public ClientNetwork(String host, int port) throws IOException {
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
        this.serverAddress = new InetSocketAddress(host, port);
    }

    public CommandResponseDTO sendAndReceive(CommandDTO dto) throws IOException, ClassNotFoundException {
        byte[] data = SerializationUtils.serialize(dto);

        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            attempt++;

            String requestId = "req-" + System.currentTimeMillis() + "-" + attempt;
            Packet requestPacket = new Packet(
                    requestId,
                    0,
                    1,
                    PacketType.DATA,
                    data
            );

            ByteBuffer out = SerializationUtils.serializeToBuffer(requestPacket);
            channel.send(out, serverAddress);

            try (Selector selector = Selector.open()) {
                channel.register(selector, SelectionKey.OP_READ);

                int ready = selector.select(TIMEOUT_MS);
                if (ready == 0) {
                    System.out.println("Сервер не отвечает, попытка " + attempt + " из " + MAX_RETRIES);
                    continue;
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (key.isReadable()) {
                        CommandResponseDTO response = receiveResponse(selector);
                        if (response != null) {
                            return response;
                        }
                    }
                }
            }
        }

        throw new IOException("Сервер временно недоступен после " + MAX_RETRIES + " попыток.");
    }

    private CommandResponseDTO receiveResponse(Selector selector) throws IOException, ClassNotFoundException {
        long startTime = System.currentTimeMillis();
        int totalPackets = -1;
        String transactionId = null;
        Map<Integer, byte[]> receivedChunks = new HashMap<>();

        while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
            long elapsed = System.currentTimeMillis() - startTime;
            int remaining = (int) (TIMEOUT_MS - elapsed);
            if (remaining <= 0) break;

            int ready = selector.select(remaining);
            if (ready == 0) {
                continue;
            }

Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isReadable()) continue;

                    ByteBuffer in = ByteBuffer.allocate(BUFFER_SIZE);
                    SocketAddress from = channel.receive(in);
                    if (from == null) continue;
                    if (!from.equals(serverAddress)) continue;

                    in.flip();
                    byte[] data = new byte[in.remaining()];
                    in.get(data);

                Object obj = deserializeAny(data);
                if (obj == null) continue;

                if (obj instanceof Packet) {
                    Packet packet = (Packet) obj;

                    if (transactionId == null) {
                        transactionId = packet.getTransactionId();
                        totalPackets = packet.getTotalPackets();
                    }

                    if (!packet.getTransactionId().equals(transactionId)) {
                        continue;
                    }

                    int idx = packet.getPacketIndex();
                    receivedChunks.put(idx, packet.getData());

                    sendAck(transactionId, idx);

                    System.out.println("Получен пакет " + (idx + 1) + "/" + totalPackets);

                    if (receivedChunks.size() == totalPackets) {
                        byte[] fullData = assembleChunks(receivedChunks, totalPackets);
                        return deserializeResponse(fullData);
                    }
                }
            }
        }

        return null;
    }

    private void sendAck(String transactionId, int packetIndex) throws IOException {
        AckPacket ack = new AckPacket(transactionId, packetIndex, PacketType.ACK);
        ByteBuffer buffer = SerializationUtils.serializeToBuffer(ack);
        channel.send(buffer, serverAddress);
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
            Object obj = ois.readObject();
            return (CommandResponseDTO) obj;
        }
    }

    private Object deserializeAny(byte[] data) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            return ois.readObject();
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}