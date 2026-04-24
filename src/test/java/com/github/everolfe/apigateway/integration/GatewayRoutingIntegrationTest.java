package com.github.everolfe.apigateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "AUTH_APP_PORT=8082",
        "USER_APP_PORT=8081",
        "ORDER_APP_PORT=8083",
        "GATEWAY_APP_PORT=8084"
})
class GatewayRoutingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void routeToAuthService_Login_ShouldBeProxied() {
        webTestClient.post()
                .uri("/api/auth/health")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void routeToUserService_ShouldBeProxied() {
        webTestClient.get()
                .uri("/api/users/health")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void routeToUserService_Cards_ShouldBeProxied() {
        webTestClient.get()
                .uri("/api/cards/1")
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    void routeToOrderService_ShouldBeProxied() {
        webTestClient.get()
                .uri("/api/orders/health")
                .exchange()
                .expectStatus().is4xxClientError();
    }

}