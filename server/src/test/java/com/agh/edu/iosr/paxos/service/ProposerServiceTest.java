package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.agh.edu.iosr.paxos.messages.accept.AcceptResponse;
import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.messages.prepare.PrepareResponse;
import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ProposerServiceTest {
    private Server server;
    private ProposerService proposerService;

    @Before
    public void setUp() throws Exception {
        this.server = Mockito.spy(new Server());
        when(server.getHalfReplicasCount()).thenReturn(1);
        this.proposerService = Mockito.spy(new ProposerService(server, null));
    }

    @Test
    public void test_updateValueIfNecessary_shouldFilterOutNullAndUpdateWithHighestNumberedValue() throws Exception {
        Stream<AcceptedProposal> acceptedProposals = Stream.of(null, new AcceptedProposal(4, "someValue"), new AcceptedProposal(5, "highestValue"));
        assertEquals("highestValue", proposerService.updateValueIfNecessary("value", acceptedProposals));
    }

    @Test
    public void test_updateValueIfNecessary_shouldNotUpdate() throws Exception {
        Stream<AcceptedProposal> acceptedProposals = Stream.of(null, null);
        assertEquals("test", proposerService.updateValueIfNecessary("test", acceptedProposals));
    }

    @Test
    public void test() throws Exception {
        server.setSequenceNumber(1);
        doReturn(ImmutableList.of(new PrepareResponse(true), new PrepareResponse(true))).when(proposerService).prepare(anyLong());
        doReturn(ImmutableList.of(new AcceptResponse(1), new AcceptResponse(1))).when(proposerService).accept(anyLong(), anyString());
        proposerService.propose("test");
        verify(proposerService, times(1)).propose(anyString());
    }
}
