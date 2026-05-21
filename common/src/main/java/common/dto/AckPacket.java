package common.dto;

import java.io.Serializable;

public class AckPacket implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String transactionId;
    private final int acknowledgedIndex;
    private final PacketType ackType;

    public AckPacket(String transactionId, int acknowledgedIndex, PacketType ackType) {
        this.transactionId = transactionId;
        this.acknowledgedIndex = acknowledgedIndex;
        this.ackType = ackType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public int getAcknowledgedIndex() {
        return acknowledgedIndex;
    }

    public PacketType getAckType() {
        return ackType;
    }
}