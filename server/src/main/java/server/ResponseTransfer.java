package server;

import common.SerializationUtils;
import common.dto.CommandResponseDTO;
import common.dto.Packet;
import common.dto.PacketType;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Отправка одного ответа клиенту (скользящее окно + ACK).
 * Без глобальной блокировки — несколько ответов могут идти параллельно.
 */
final class ResponseTransfer {

    private static final int CHUNK_SIZE = 8192;
    private static final int WINDOW_SIZE = 16;
    private static final int MAX_RETRIES = 12;
    private static final int ACK_TIMEOUT_MS = 15000;
    private static final int ACK_TIMEOUT_SINGLE_MS = 2500;
    private static final int ACK_POLL_MS = 5;

    private static final ConcurrentHashMap<String, Set<Integer>> ackRegistry = new ConcurrentHashMap<>();

    private ResponseTransfer() {
    }

    static void onAck(String transactionId, int zeroBasedIndex) {
        Set<Integer> acks = ackRegistry.get(transactionId);
        if (acks != null) {
            acks.add(zeroBasedIndex);
        }
    }

    static void send(
        DatagramChannel channel,
        SocketAddress client,
        String transactionId,
        CommandResponseDTO response
    ) throws IOException {
        byte[] responseData = SerializationUtils.serialize(response);
        List<byte[]> chunks = chunkData(responseData, CHUNK_SIZE);
        int total = chunks.size();
        ServerLog.info(
            "Preparing response " + transactionId
                + ": bytes=" + responseData.length
                + ", chunkSize=" + CHUNK_SIZE
                + ", packets=" + total
        );

        if (total == 0) {
            ServerLog.warn("No packets to send for " + transactionId);
            return;
        }

        Set<Integer> acked = ConcurrentHashMap.newKeySet();
        ackRegistry.put(transactionId, acked);
        try {
            if (total == 1) {
                sendPacket(channel, client, transactionId, total, chunks, 0, false);
                waitForAck(acked, 0, ACK_TIMEOUT_SINGLE_MS);
                ServerLog.info("Single-packet response sent for " + transactionId);
                return;
            }

            int base = 0;
            int next = 0;
            int retries = 0;

            while (base < total) {
                while (next < total && next < base + WINDOW_SIZE) {
                    sendPacket(channel, client, transactionId, total, chunks, next, false);
                    next++;
                }

                long deadline = System.currentTimeMillis() + ACK_TIMEOUT_MS;
                boolean advanced = false;

                while (System.currentTimeMillis() < deadline) {
                    int oldBase = base;
                    while (base < total && acked.contains(base)) {
                        base++;
                    }
                    if (base > oldBase) {
                        advanced = true;
                        retries = 0;
                        break;
                    }
                    try {
                        Thread.sleep(ACK_POLL_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Sliding window interrupted", e);
                    }
                }

                if (!advanced) {
                    retries++;
                    if (retries > MAX_RETRIES) {
                        ServerLog.warn("Giving up on " + transactionId + " at packet " + (base + 1));
                        return;
                    }
                    ServerLog.warn("ACK timeout for " + transactionId + ", resend from packet " + (base + 1));
                    for (int i = base; i < next; i++) {
                        sendPacket(channel, client, transactionId, total, chunks, i, true);
                    }
                }
            }
            ServerLog.info("All packets sent for " + transactionId);
        } finally {
            ackRegistry.remove(transactionId);
        }
    }

    private static void sendPacket(
        DatagramChannel channel,
        SocketAddress addr,
        String tid,
        int total,
        List<byte[]> chunks,
        int index,
        boolean resend
    ) throws IOException {
        PacketType type = resend ? PacketType.RESEND : PacketType.DATA;
        Packet packet = new Packet(tid, index, total, type, chunks.get(index));
        channel.send(ByteBuffer.wrap(SerializationUtils.serialize(packet)), addr);
        if (!resend || index == 0) {
            ServerLog.info((resend ? "Resent" : "Sent") + " packet " + (index + 1) + "/" + total + " for " + tid);
        }
    }

    private static void waitForAck(Set<Integer> acked, int index, int timeoutMs) throws IOException {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (acked.contains(index)) {
                return;
            }
            try {
                Thread.sleep(ACK_POLL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("ACK wait interrupted", e);
            }
        }
        ServerLog.warn("ACK not received for packet " + (index + 1) + " within " + timeoutMs + " ms (continuing)");
    }

    private static List<byte[]> chunkData(byte[] data, int size) {
        List<byte[]> list = new ArrayList<>();
        for (int i = 0; i < data.length; i += size) {
            int end = Math.min(i + size, data.length);
            byte[] chunk = new byte[end - i];
            System.arraycopy(data, i, chunk, 0, chunk.length);
            list.add(chunk);
        }
        return list;
    }
}
