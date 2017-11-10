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
import static org.junit.Assert.fail;
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
    public void test_simple() throws Exception {
        server.setSequenceNumber(0); // will send 1

        PrepareResponse promise = new PrepareResponse(true);
        doReturn(ImmutableList.of(promise, promise)).when(proposerService).prepare(anyLong());

        AcceptResponse acceptResponse = new AcceptResponse(1);
        doReturn(ImmutableList.of(acceptResponse, acceptResponse)).when(proposerService).accept(anyLong(), anyString());

        proposerService.propose("test");

        verify(proposerService, times(1)).propose(anyString());
    }

    @Test
    public void test_noMajorityFromAcceptors_onPrepare() throws Exception {
        server.setSequenceNumber(0);

        doReturn(ImmutableList.of(new PrepareResponse(true))).when(proposerService).prepare(anyLong());
        doCallRealMethod().doThrow(new RuntimeException()).when(proposerService).propose(anyString()); // breaking the loop on 2nd pass

        try {
            proposerService.propose("value");
        } catch (RuntimeException e) {
            verify(proposerService, times(2)).propose("value");
            return;
        }

        fail();
    }

    @Test
    public void test_proposerHasToUpdateValueAndIsAcceptedByMajorityOnAccept() throws Exception {
        server.setSequenceNumber(3); // will send 4

        doReturn(ImmutableList.of(new PrepareResponse(true, new AcceptedProposal(2, "two")), new PrepareResponse(true, new AcceptedProposal(3, "three")))).when(proposerService).prepare(anyLong());

        AcceptResponse acceptResponse = new AcceptResponse(4);
        doReturn(ImmutableList.of(acceptResponse, acceptResponse)).when(proposerService).accept(anyLong(), anyString());

        proposerService.propose("four");

        verify(proposerService, times(1)).propose("four");
        verify(proposerService, times(1)).accept(4, "three");
    }

    @Test
    public void test_noMajorityFromAcceptors_onAccept() throws Exception {
        server.setSequenceNumber(0); // will send 1

        PrepareResponse promise = new PrepareResponse(true);
        doReturn(ImmutableList.of(promise, promise)).when(proposerService).prepare(anyLong());

        AcceptResponse acceptResponse = new AcceptResponse(1);
        doReturn(ImmutableList.of(acceptResponse)).when(proposerService).accept(anyLong(), anyString());

        doCallRealMethod().doThrow(new RuntimeException()).when(proposerService).propose(anyString()); // breaking the loop on 2nd pass

        try {
            proposerService.propose("value");
        } catch (RuntimeException e) {
            verify(proposerService, times(2)).propose("value");
            return;
        }

        fail();
    }

    @Test
    public void test_majorityFromAcceptorsButOneAcceptorAcceptedAHigherNumberProposal() throws Exception {
        server.setSequenceNumber(0); // will send 1

        PrepareResponse promise = new PrepareResponse(true);
        doReturn(ImmutableList.of(promise, promise)).when(proposerService).prepare(anyLong());

        doReturn(ImmutableList.of(new AcceptResponse(1), new AcceptResponse(2))).when(proposerService).accept(anyLong(), anyString());

        doCallRealMethod().doThrow(new RuntimeException()).when(proposerService).propose(anyString()); // breaking the loop on 2nd pass

        try {
            proposerService.propose("value");
        } catch (RuntimeException e) {
            verify(proposerService, times(2)).propose("value");
            return;
        }

        fail();
    }
}
