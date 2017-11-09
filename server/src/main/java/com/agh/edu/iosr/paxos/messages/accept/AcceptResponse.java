package com.agh.edu.iosr.paxos.messages.accept;

public class AcceptResponse {
    private long sequenceNumber;

    public AcceptResponse() {
    }

    public AcceptResponse(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
