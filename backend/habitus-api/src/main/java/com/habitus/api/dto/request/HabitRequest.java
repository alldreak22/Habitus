package com.habitus.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HabitRequest(
    @NotBlank(message = "Nome é obrigatório") String name,
    String description,
    @NotBlank(message = "Frequência alvo é obrigatória") String targetFrequency,
    @NotNull(message = "Quantidade por dia é obrigatória") @Min(value = 1, message = "Quantidade por dia deve ser no mínimo 1") Integer timesPerDay,
    String suggestedTimes
) {
}
