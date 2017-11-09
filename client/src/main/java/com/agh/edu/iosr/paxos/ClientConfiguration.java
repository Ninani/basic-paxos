package com.agh.edu.iosr.paxos;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ClientConfiguration {
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

    @Bean
    public RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(replicaCallTimeout);
        httpRequestFactory.setConnectTimeout(replicaCallTimeout);
        httpRequestFactory.setReadTimeout(replicaCallTimeout);

        return new RestTemplate(httpRequestFactory);
    }

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setIncludeClientInfo(true);

        return filter;
    }
}
