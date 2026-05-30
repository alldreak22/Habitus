package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Login e obrigatorio") String login,
    @NotBlank(message = "Senha e obrigatoria") String password
) {
}
