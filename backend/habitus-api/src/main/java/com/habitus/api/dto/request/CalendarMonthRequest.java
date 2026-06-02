package com.habitus.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CalendarMonthRequest(
    @NotNull(message = "Ano e obrigatorio")
    @Min(value = 1900, message = "Ano invalido")
    Integer year,

    @NotNull(message = "Mes e obrigatorio")
    @Min(value = 1, message = "Mes deve estar entre 1 e 12")
    @Max(value = 12, message = "Mes deve estar entre 1 e 12")
    Integer month
) {
}
