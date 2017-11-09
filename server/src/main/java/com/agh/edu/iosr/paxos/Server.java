package com.agh.edu.iosr.paxos;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class Server {

    private final String port;
    private final List<String> replicasAddresses;
    private final int halfReplicasCount;
    private final AtomicReference<String> value;
    private final AtomicLong sequenceNumber;

    @Autowired
    public Server(@Value("#{'${server.port}'}") String port, @Value("#{'${server.replicas}'.split(',')}") List<String> replicasAddresses) {
        this.port = port;
        this.replicasAddresses = ImmutableList.copyOf(new LinkedHashSet<>(replicasAddresses));
        this.halfReplicasCount = this.replicasAddresses.size() / 2;
        this.value = new AtomicReference<>("Initial value");
        this.sequenceNumber = new AtomicLong(0);
    }

    // for testing
    public Server() {
        this("-1", ImmutableList.of());
    }

    public String getPort() {
        return port;
    }

    public List<String> getReplicasAddresses() {
        return replicasAddresses;
    }

    public int getReplicasCount() {
        return replicasAddresses.size();
    }

    public int getHalfReplicasCount() {
        return halfReplicasCount;
    }

    public AtomicReference<String> getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public void setSequenceNumber(long value) {
        sequenceNumber.set(value);
    }

    public long incrementAndGetSequenceNumber() {
        return sequenceNumber.incrementAndGet();
    }
}
