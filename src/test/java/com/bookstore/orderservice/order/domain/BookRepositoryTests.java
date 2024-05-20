package com.bookstore.orderservice.order.domain;

import com.bookstore.orderservice.config.DataConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

@DataR2dbcTest
@Import(DataConfig.class)
@Testcontainers
public class BookRepositoryTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.10"));

    @Autowired
    OrderRepository orderRepository;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", BookRepositoryTests::r2dbcUrl);
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
    }

    private static String r2dbcUrl() {
        return String.format("r2dbc:postgresql://%s:%s/%s",
                postgres.getHost(), postgres.getMappedPort(5432), postgres.getDatabaseName());
    }

    @Test
    void setup() {

    }

    @Test
    void findOrderByIdWhenNotExisting() {
        StepVerifier.create(orderRepository.findById(100L))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void createRejectOrderWhenNotAuthenticatedThenReturnDefaultMetadata() {
        var rejectedOrder = OrderService.buildRejectedOrder("1234567890", 2);
        StepVerifier.create(orderRepository.save(rejectedOrder))
                .expectNextMatches(order ->
                        order.status().equals(OrderStatus.REJECTED) &&
                        order.createdBy().equals("unknown"))
                .verifyComplete();
    }

}
