package com.agh.edu.iosr.paxos.messages.prepare;

public class PrepareRequest {

    private long sequenceNumber;

    public PrepareRequest() {
    }

    public PrepareRequest(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
