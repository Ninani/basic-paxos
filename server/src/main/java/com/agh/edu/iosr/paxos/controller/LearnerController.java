package com.agh.edu.iosr.paxos.controller;

import com.agh.edu.iosr.paxos.Server;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public class LearnerController {

    private final Server server;

    public LearnerController(Server server) {
        this.server = server;
    }

    @RequestMapping(value = "/learn/{value}", method = RequestMethod.POST)
    public void learn(@PathVariable String value) {
        server.setValue(value);
    }
}
