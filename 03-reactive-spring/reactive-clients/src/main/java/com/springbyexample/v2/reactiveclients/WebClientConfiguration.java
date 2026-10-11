package com.springbyexample.v2.reactiveclients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author Mujuzi Moses
 */
@Configuration
public class WebClientConfiguration {

    @Bean
    WebClient postWebClient(WebClient.Builder builder,
            @Value("${clients.posts.base-url:https://jsonplaceholder.typicode.com}") String baseUrl) {

        return builder.baseUrl(baseUrl).build();
    }
}
