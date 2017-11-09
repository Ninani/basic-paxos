package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.messages.prepare.AcceptedProposal;
import com.agh.edu.iosr.paxos.service.LearnerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class LearnerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LearnerController.class);

    private final LearnerService learnerService;

    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @PostMapping(value = "/learn")
    public void learn(@RequestBody AcceptedProposal acceptedProposal, HttpServletRequest request) {
        LOGGER.info(ControllerUtils.describeRequest(request, acceptedProposal));

        learnerService.learn(acceptedProposal);

        ResponseEntity<String> response = ResponseEntity.ok("OK");

        LOGGER.info(ControllerUtils.describeResponse(request, acceptedProposal, response));

    }
}
