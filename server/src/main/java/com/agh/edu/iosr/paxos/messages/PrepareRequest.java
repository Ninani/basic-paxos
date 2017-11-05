package com.agh.edu.iosr.paxos.messages;

// UNUSED

public class PrepareRequest {

    private int propsedValue;
//  Each proposer’s proposal number must be a positive, monotonically increasing, unique, natural number,
//  with respect to other proposers’ proposal numbers.
    private int sequenceNumber;

    public PrepareRequest() {
    }

    public PrepareRequest(int propsedValue, int sequenceNumber) {
        this.propsedValue = propsedValue;
        this.sequenceNumber = sequenceNumber;
    }

    public int getPropsedValue() {
        return propsedValue;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

}
