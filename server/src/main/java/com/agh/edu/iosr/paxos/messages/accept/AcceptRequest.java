package com.agh.edu.iosr.paxos.messages.accept;

public class AcceptRequest {
    private long sequenceNumber;
    private String value;

    public AcceptRequest() {
    }

    public AcceptRequest(long sequenceNumber, String value) {
        this.sequenceNumber = sequenceNumber;
        this.value = value;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
