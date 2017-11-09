package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.service.LearnerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class LearnerController {

    private final LearnerService learnerService;

    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @RequestMapping(value = "/learn/{value}", method = RequestMethod.POST)
    public void learn(@PathVariable String value) {
        learnerService.learn(value);
    }
}
