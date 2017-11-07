package com.agh.edu.iosr.paxos;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

public class ServerConfiguration {
    @Value("${replica.call.timeout}")
    private int replicaCallTimeout;

    @Bean
    public AsyncRestTemplate getAsyncRestTemplate() {
        HttpComponentsAsyncClientHttpRequestFactory asyncRequestFactory = new HttpComponentsAsyncClientHttpRequestFactory();
        asyncRequestFactory.setHttpAsyncClient(asyncHttpClient());
        return new AsyncRestTemplate(asyncRequestFactory);
    }

    @Bean
    public CloseableHttpAsyncClient asyncHttpClient() {
        IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setConnectTimeout(replicaCallTimeout).setSoTimeout(replicaCallTimeout).build();
        return HttpAsyncClients.custom().setDefaultIOReactorConfig(ioReactorConfig).build();
    }
}
