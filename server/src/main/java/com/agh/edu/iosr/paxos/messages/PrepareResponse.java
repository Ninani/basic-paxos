package com.agh.edu.iosr.paxos.messages;

public class PrepareResponse {

    private String value;
    private long sequenceNumber;
    private PrepareStatus status;

    public PrepareResponse(String value, long sequenceNumber, PrepareStatus status) {
        this.value = value;
        this.sequenceNumber = sequenceNumber;
        this.status = status;
    }

    public String getValue() {
        return value;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public PrepareStatus getStatus() {
        return status;
    }
}
