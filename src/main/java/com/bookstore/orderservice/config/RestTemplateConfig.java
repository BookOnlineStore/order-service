package com.bookstore.orderservice.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public RestTemplateConfig(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Bean
    RestTemplate restTemplate(ClientProperties properties) {
        return new RestTemplateBuilder()
                .interceptors((request, body, execution) -> {
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("keycloak")
                            .principal("order-service")
                            .build();
                    OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
                    assert authorizedClient != null;
                    String accessToken = authorizedClient.getAccessToken().getTokenValue();
                    request.getHeaders().setBearerAuth(accessToken);
                    return execution.execute(request, body);
                })
                .rootUri(properties.catalogServiceUri().toString())
                .build();
    }

}
