package com.agh.edu.iosr.paxos;

import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.service.AcceptorService;
import com.agh.edu.iosr.paxos.service.LearnerService;
import com.google.common.util.concurrent.AtomicLongMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Service;
import org.springframework.web.client.AsyncRestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class LearnerServiceTest {
    Server server;
    AcceptorService acceptorService;
    LearnerService learnerService;
    List<String> replicaAddressList;

    @Before
    public void setUp() {
        replicaAddressList = new ArrayList<>();
        replicaAddressList.add("address1");
        replicaAddressList.add("address2");
        replicaAddressList.add("address3");

        server = new Server("1234", replicaAddressList);
        acceptorService = new AcceptorService(server, new AsyncRestTemplate(), 1, new AcceptedProposal(1, "val"));
        learnerService = new LearnerService(server, acceptorService);
    }

    @Test
    public void saveValueOnlyIfQuorum() {
        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(server.getValue().get()).isEqualToIgnoringCase("value");
    }

    @Test
    public void dontSaveIfQuorumButDifferentValues() {
        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
        learnerService.learn(new AcceptedProposal(90, "value"));
        learnerService.learn(new AcceptedProposal(90, "value2"));
        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
    }

//    @Test
//    public void dontSaveIfQuorumButDifferentSeqNumbers() {
//        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
//        learnerService.learn(new AcceptedProposal(70, "value"));
//        learnerService.learn(new AcceptedProposal(90, "value"));
//        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
//    }

    @Test
    public void worksForEvenNumberOfNodes() {
//        test for 4 nodes
        replicaAddressList.add("address4");
        Server evenNodeServer = new Server("1234", replicaAddressList);
        acceptorService = new AcceptorService(evenNodeServer, new AsyncRestTemplate(), 1, new AcceptedProposal(1, "val"));
        learnerService = new LearnerService(evenNodeServer, acceptorService);

        learnerService.learn(new AcceptedProposal(100, "value"));
        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(evenNodeServer.getValue().get()).isEqualToIgnoringCase("Initial value");

        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(evenNodeServer.getValue().get()).isEqualToIgnoringCase("value");
    }

    @Test
    public void worksForOddNumberOfNodes() {
//        test for 5 nodes
        replicaAddressList.add("address4");
        replicaAddressList.add("address5");

        Server oddNodeServer = new Server("1234", replicaAddressList);
        acceptorService = new AcceptorService(oddNodeServer, new AsyncRestTemplate(), 1, new AcceptedProposal(1, "val"));
        learnerService = new LearnerService(oddNodeServer, acceptorService);

        learnerService.learn(new AcceptedProposal(100, "value"));
        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(oddNodeServer.getValue().get()).isEqualToIgnoringCase("Initial value");

        learnerService.learn(new AcceptedProposal(100, "value"));
        assertThat(oddNodeServer.getValue().get()).isEqualToIgnoringCase("value");
    }

    @Test
    public void clearsAcceptedProposalInAcceptorService() {
        assertThat(server.getValue().get()).isEqualToIgnoringCase("Initial value");
        assertThat(acceptorService.getAcceptedProposal()).hasNoNullFieldsOrProperties();
        learnerService.learn(new AcceptedProposal(90, "value"));
        learnerService.learn(new AcceptedProposal(90, "value"));
        assertThat(server.getValue().get()).isEqualToIgnoringCase("value");
        assertThat(acceptorService.getAcceptedProposal()).isEqualTo(null);
    }

    @Test
    public void subtractForEqualOccurencesAndReplicasCount() {

    }
}
