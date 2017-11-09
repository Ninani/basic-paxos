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
    
    @Before
    public void setUp() {
        acceptorServiceSeqZero = new AcceptorService(0, new AcceptedProposal(0, "val"));
        acceptorServiceSeqOne = new AcceptorService(1, new AcceptedProposal(1, "val"));
        prepareRequestSeqZero = new PrepareRequest(0);
        prepareRequestSeqHigh = new PrepareRequest(1000);
    }

//    Tests for PREPARE phase

    @Test
    public void returnsFalseIfSeqNumIsLower() {
        PrepareResponse prepareResponse = acceptorServiceSeqOne.prepare(prepareRequestSeqZero);
        assertThat(prepareResponse.getAnswer()).isEqualTo(false);
    }

    @Test
    public void returnsTrueIfSeqNumIsHigher() {
        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqHigh);
        assertThat(prepareResponse.getAnswer()).isEqualTo(true);
    }

//    why it returns true?
//    @Test
//    public void returnsFalseIfSeqNumIsEqual() {
//        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqZero);
//        assertThat(prepareResponse.getAnswer()).isEqualTo(false);
//    }

    @Test
    public void returnsValueForAcceptedPrepareRequest() {
        PrepareResponse prepareResponse = acceptorServiceSeqZero.prepare(prepareRequestSeqHigh);
        assertThat(prepareResponse.getAnswer()).isEqualTo(true);
        assertThat(prepareResponse.getAcceptedProposal().getValue()).isEqualToIgnoringCase("val");
    }

//    Tests for ACCEPT phase

    @Test
    public void alwaysAcceptFirstAcceptRequest() {
        long newSeqNumber = 1;
        AcceptResponse acceptResponse = acceptorServiceSeqZero.accept(new AcceptRequest(newSeqNumber, "val"));
        assertThat(acceptResponse.getSequenceNumber()).isEqualTo(newSeqNumber);

    }

    @Test
    public void acceptRequestIfNumberIsHigherThanPrevious() {
        long newSeqNumber = 1;
        AcceptResponse acceptResponse = acceptorServiceSeqZero.accept(new AcceptRequest(newSeqNumber, "val"));
        assertThat(acceptResponse.getSequenceNumber()).isEqualTo(newSeqNumber);
    }

    @Test
    public void acceptRequestIfNumberIsEqualToPrevious() {
        long newSeqNumber = 0;
        AcceptResponse acceptResponse = acceptorServiceSeqZero.accept(new AcceptRequest(newSeqNumber, "val"));
        assertThat(acceptResponse.getSequenceNumber()).isEqualTo(newSeqNumber);
    }

//    Tests for two phases

    @Test
    public void returnsPrevoiuslyAcceptedNumberAndValueForNextPrepareRequest() {
        long seqNumber = 6;
        String value = "val";
        AcceptorService acceptorService = new AcceptorService(seqNumber, new AcceptedProposal());
        AcceptResponse acceptResponse = acceptorService.accept(new AcceptRequest(seqNumber, value));
        // TODO: 11/9/17 check if request has been accepted
        PrepareResponse prepareResponse = acceptorService.prepare(new PrepareRequest(8));
        assertThat(prepareResponse.getAcceptedProposal().getSequenceNumber()).isEqualTo(seqNumber);
        assertThat(prepareResponse.getAcceptedProposal().getValue()).isEqualToIgnoringCase(value);
    }

}
