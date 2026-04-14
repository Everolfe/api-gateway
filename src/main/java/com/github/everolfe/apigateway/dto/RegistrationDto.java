package com.github.everolfe.apigateway.dto;

import java.time.LocalDate;

public record RegistrationDto(
    String email,
    String password,
    String name,
    String surname,
    LocalDate birthday
) {
}
