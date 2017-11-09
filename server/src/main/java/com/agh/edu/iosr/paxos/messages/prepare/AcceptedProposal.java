package com.agh.edu.iosr.paxos.messages.prepare;

import java.util.Objects;

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
    public int hashCode() {
        return Objects.hash(sequenceNumber, value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof AcceptedProposal) {
            AcceptedProposal that = (AcceptedProposal) object;

            return Objects.equals(sequenceNumber, that.sequenceNumber) && Objects.equals(value, that.value);
        }

        return false;
    }

    @Override
    public String toString() {
        return "AcceptedProposal{" +
                "sequenceNumber=" + sequenceNumber +
                ", value='" + value + '\'' +
                '}';
    }
}
