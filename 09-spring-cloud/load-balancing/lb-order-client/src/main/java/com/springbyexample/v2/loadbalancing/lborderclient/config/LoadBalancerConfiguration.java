package com.springbyexample.v2.loadbalancing.lborderclient.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configures an HTTP client that supports client-side load balancing.
 *
 * @author Mujuzi Moses
 */
@Configuration(proxyBeanMethods = false)
public class LoadBalancerConfiguration {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

}
