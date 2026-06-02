package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Senha atual e obrigatoria") String currentPassword,
    @NotBlank(message = "Nova senha e obrigatoria") @Size(min = 8, message = "Nova senha deve ter pelo menos 8 caracteres") String newPassword
) {
}
