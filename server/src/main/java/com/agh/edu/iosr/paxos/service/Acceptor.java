package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.messages.PrepareResponse;
import com.agh.edu.iosr.paxos.messages.PrepareStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class Acceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Acceptor.class);

    private String currentValue = "no value";
    private long lowestSequenceNumber = -1;

    public PrepareResponse processPrepareRequest(String value, long sequenceNumber) {
        PrepareResponse prepareResponse;

        if (sequenceNumber > lowestSequenceNumber) {
            currentValue = value;
            lowestSequenceNumber = sequenceNumber;
            LOGGER.debug("Value ACCEPTED, current value: {}, seq number: {}", value, sequenceNumber);
            prepareResponse = new PrepareResponse(currentValue, lowestSequenceNumber, PrepareStatus.ACCEPTED);
        } else {
            LOGGER.debug("Value DENIED, current value: {}, seq number: {}", value, sequenceNumber);
            prepareResponse = new PrepareResponse(currentValue, lowestSequenceNumber, PrepareStatus.DENIED);
        }
        return prepareResponse;
    }

}
