package common.dto;

import java.io.Serializable;

public class Packet implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String transactionId;
    private final int packetIndex;
    private final int totalPackets;
    private final PacketType packetType;
    private final byte[] data;

    public Packet(String transactionId, int packetIndex, int totalPackets,
                  PacketType packetType, byte[] data) {
        this.transactionId = transactionId;
        this.packetIndex = packetIndex;
        this.totalPackets = totalPackets;
        this.packetType = packetType;
        this.data = data;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getPacketIndex() {
        return packetIndex;
    }

    public int getTotalPackets() {
        return totalPackets;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public byte[] getData() {
        return data;
    }
}