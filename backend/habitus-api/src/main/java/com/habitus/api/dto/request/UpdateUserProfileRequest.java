package com.habitus.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank(message = "Nome e obrigatorio")
    @Size(max = 255, message = "Nome deve ter no maximo 255 caracteres")
    String name,

    @NotBlank(message = "E-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    String email,

    @Size(max = 255, message = "Apelido deve ter no maximo 255 caracteres")
    String nick,

    String picture
) {
}
