package com.bookstore.orderservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    RestTemplate restTemplate(ClientProperties properties) {
        return new RestTemplateBuilder().rootUri(properties.catalogServiceUri().toString())
                .build();
    }

}
