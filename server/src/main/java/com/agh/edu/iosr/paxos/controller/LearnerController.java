package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.service.LearnerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class LearnerController {

    private final LearnerService learnerService;

    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @PostMapping(value = "/learn")
    public void learn(@RequestBody AcceptedProposal acceptedProposal) {
        learnerService.learn(acceptedProposal);
    }
}
