package com.github.everolfe.apigateway.service;

import com.github.everolfe.apigateway.dto.RegistrationDto;
import com.github.everolfe.apigateway.dto.RegistrationResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final WebClient authWebClient;
    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    public Mono<RegistrationResponse> registerUser(RegistrationDto request) {
        log.info("Starting registration for email={}", request.email());

        return authWebClient.post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        this::handleError)
                .toEntity(String.class)
                .flatMap(responseEntity -> {
                    if (responseEntity.getStatusCode().is2xxSuccessful()) {
                        String xRegistrationId = responseEntity.getHeaders()
                                .getFirst("X-Registration-Id");
                        if (xRegistrationId != null) {
                            log.info("Registration initiated. outboxId={}", xRegistrationId);
                            return Mono.just(new RegistrationResponse(
                                    UUID.fromString(xRegistrationId),
                                    "REGISTRATION_INITIATED"
                            ));
                        } else {
                            return Mono.error(new RuntimeException("Missing X-Registration-Id header"));
                        }
                    }
                    return Mono.error(new RuntimeException("Registration failed with status: "
                            + responseEntity.getStatusCode()));
                });
    }

    private Mono<? extends Throwable> handleError(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("Unknown error")
                .flatMap(errorBody -> {
                    log.error("Auth service error: status={}, body={}",
                            response.statusCode(), errorBody);
                    return Mono.error(new RuntimeException("Auth service error: " + errorBody));
                });
    }
}