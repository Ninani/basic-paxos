package com.agh.edu.iosr.paxos.messages.prepare;

public class AcceptedProposal {
    private long sequenceNumber;
    private String value;

    public AcceptedProposal() {
    }

    public AcceptedProposal(long sequenceNumber, String value) {
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

    @Override
    public String toString() {
        return "AcceptedProposal{" +
                "sequenceNumber=" + sequenceNumber +
                ", value='" + value + '\'' +
                '}';
    }
}
