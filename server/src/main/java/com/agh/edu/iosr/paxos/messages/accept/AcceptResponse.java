package com.agh.edu.iosr.paxos.messages.accept;

public class AcceptResponse implements Comparable<AcceptResponse> {
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

    @Override
    public int compareTo(AcceptResponse other) {
        return Long.compare(other.sequenceNumber, this.sequenceNumber);
    }
}
