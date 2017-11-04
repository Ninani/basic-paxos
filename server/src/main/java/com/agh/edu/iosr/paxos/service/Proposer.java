package com.agh.edu.iosr.paxos.service;

import org.springframework.stereotype.Service;

@Service
public class Proposer {

    public void processSaveRequest(String value) {
//        1. send request to acceptors (Prepare Request)
//        2. process received values (accepted/not accepted) -> HttpStatus or PrepareResponse (java object) message
//        3. check the value for the majority of Prepare Responses (received from acceptors)
//        4. send Accept Request to all acceptors with the value selected in a previous stage
//        ...
    }
}
