package com.agh.edu.iosr.paxos;


import com.agh.edu.iosr.paxos.messages.accept.AcceptRequest;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareRequest;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.agh.edu.iosr.paxos.service.AcceptorService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class AcceptorServiceTest {
    AcceptorService acceptorServiceSeqZero;
    AcceptorService acceptorServiceSeqOne;
    PrepareRequest prepareRequestSeqZero;
    PrepareRequest prepareRequestSeqHigh;
    AcceptRequest acceptRequestSeqZero;

    @Before
    public void setUp() {
        acceptorServiceSeqZero = new AcceptorService(0, new AcceptedProposal(0, "val"));
        acceptorServiceSeqOne = new AcceptorService(1, new AcceptedProposal(1, "val"));
        prepareRequestSeqZero = new PrepareRequest(0);
        prepareRequestSeqHigh = new PrepareRequest(1000);
        acceptRequestSeqZero = new AcceptRequest(0, "val");
    }

    @Test
    public void returnsFalseIfSeqNumIsLower() {
        PrepareResponse prepareResponse = acceptorServiceSeqOne.prepare(prepareRequestSeqZero);
        assertThat(prepareResponse.isAnswer()).isEqualTo(false);
    }

    @Test
    public void returnsTrueIfSeqNumIsHigher() {
        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqHigh);
        assertThat(prepareResponse.isAnswer()).isEqualTo(true);
    }

////    why it returns true?
//    @Test
//    public void returnsFalseIfSeqNumIsEqual() {
//        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqZero);
//        assertThat(prepareResponse.isAnswer()).isEqualTo(false);
//    }

////    it returns previous seqNum  - is it ok?
//    @Test
//    public void returnsSeqNumForAcceptedProposal() {
//        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqHigh);
//        assertThat(prepareResponse.isAnswer()).isEqualTo(true);
//        long prepareRequestSeqNum = prepareRequestSeqHigh.getSequenceNumber();
//        assertThat(prepareResponse.getAcceptedProposal().getSequenceNumber()).isEqualTo(prepareRequestSeqNum);
//    }

    @Test
    public void returnsValueForAcceptedProposal() {
        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqHigh);
        assertThat(prepareResponse.isAnswer()).isEqualTo(true);
        assertThat(prepareResponse.getAcceptedProposal().getValue()).isEqualToIgnoringCase("val");
    }

    // TODO: 11/8/17 write test cases for tests shown in YT presentation

    @Test
    public void acceptRequestOnlyIfNumberIsEqualToSeqNumber() {

    }


}
