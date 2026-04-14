package com.github.everolfe.apigateway.controller;

import com.github.everolfe.apigateway.dto.RegistrationDto;
import com.github.everolfe.apigateway.dto.RegistrationResponse;
import com.github.everolfe.apigateway.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/gateway")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;
    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponse>> registerUser(
            @Valid @RequestBody RegistrationDto registrationDto) {

        log.info("Gateway received registration request for email: {}", registrationDto.email());

        return registrationService.registerUser(registrationDto)
                .map(response -> ResponseEntity
                        .accepted()
                        .body(response))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.error("Validation error: {}", e.getMessage());
                    return Mono.just(ResponseEntity
                            .badRequest()
                            .body(new RegistrationResponse(null, e.getMessage())));
                })
                .onErrorResume(RuntimeException.class, e -> {
                    log.error("Registration error: {}", e.getMessage());
                    if (e.getMessage().contains("Auth service error")) {
                        return Mono.just(ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(new RegistrationResponse(null, "Auth service unavailable")));
                    }
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new RegistrationResponse(null, "Internal error")));
                });
    }
}