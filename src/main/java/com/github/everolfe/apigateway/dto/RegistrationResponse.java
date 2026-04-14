package com.github.everolfe.apigateway.dto;

import java.util.UUID;

public record RegistrationResponse(
        UUID outboxId,
        String message
) {
}
