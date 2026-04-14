package com.github.everolfe.apigateway.unit;

import com.github.everolfe.apigateway.dto.RegistrationDto;
import com.github.everolfe.apigateway.dto.RegistrationResponse;
import com.github.everolfe.apigateway.service.RegistrationService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

class RegistrationServiceTest {

    private MockWebServer mockWebServer;
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("").toString().replace("/$", "");
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        registrationService = new RegistrationService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void registerUser_Success() {
        UUID expectedOutboxId = UUID.randomUUID();

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(202)
                .addHeader("X-Registration-Id", expectedOutboxId.toString())
                .setBody("Registration initiated")
        );

        RegistrationDto registrationDto = new RegistrationDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1)
        );

        StepVerifier.create(registrationService.registerUser(registrationDto))
                .expectNextMatches(response ->
                        response.outboxId().equals(expectedOutboxId) &&
                                response.message().equals("REGISTRATION_INITIATED")
                )
                .verifyComplete();
    }

    @Test
    void registerUser_MissingHeader_Error() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(202)
                .setBody("Registration initiated")
        );

        RegistrationDto registrationDto = new RegistrationDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1)
        );

        StepVerifier.create(registrationService.registerUser(registrationDto))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Missing X-Registration-Id header")
                )
                .verify();
    }

    @Test
    void registerUser_AuthServiceError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(503)
                .setBody("Auth service unavailable")
        );

        RegistrationDto registrationDto = new RegistrationDto(
                "test@example.com",
                "password123",
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1)
        );

        StepVerifier.create(registrationService.registerUser(registrationDto))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Auth service error")
                )
                .verify();
    }
}