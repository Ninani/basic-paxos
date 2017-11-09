package com.agh.edu.iosr.paxos.service;

import com.agh.edu.iosr.paxos.Server;
import com.google.common.util.concurrent.AtomicLongMap;
import org.springframework.stereotype.Service;

@Service
public class LearnerService {

    private final Server server;
    private final AtomicLongMap<String> map;

    public LearnerService(Server server) {
        this.server = server;
        this.map = AtomicLongMap.create();
    }

    public void learn(String value) {
        long halfReplicasCountPlusOne = server.getHalfReplicasCount() + 1;

        if (map.incrementAndGet(value) == halfReplicasCountPlusOne) {
            map.getAndAdd(value, -halfReplicasCountPlusOne);

            server.setValue(value);
        }
    }
}
