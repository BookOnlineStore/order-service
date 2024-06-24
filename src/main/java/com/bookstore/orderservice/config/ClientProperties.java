package com.bookstore.orderservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.net.URI;

@ConfigurationProperties(prefix = "bookstore")
public record ClientProperties (
        @NotNull
        URI catalogServiceUri,
        @NotNull
        VNPayProperties vnPay
) {
        record VNPayProperties(
                String apiUrl,
                String tmnCode,
                String secretKey,
                String returnUrl,
                String version,
                String command,
                String orderType
        ) {

        }
}
