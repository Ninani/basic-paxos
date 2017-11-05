package com.agh.edu.iosr.paxos.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class Server {

    // TODO: 11/5/17 put here also the value (and remove from server controller)

    private AtomicLong sequenceNumber = new AtomicLong(-1);

    public long getSeqNumber() {
        return sequenceNumber.get();
    }

    public long incrementAndGetSeqNumber() {
        return sequenceNumber.incrementAndGet();
    }

    //    returns true if the sequenceNumber was updated with a newSequenceNumber
    public boolean compareAndUpdateSeqNumber(long newSequenceNumber) {
        long current = sequenceNumber.updateAndGet(value -> value < newSequenceNumber ? newSequenceNumber : value);
        return current == newSequenceNumber;
    }
}
