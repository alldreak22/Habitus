package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record DailyHabitCompletionRequest(
    @NotNull(message = "Hábito é obrigatório") Long habitId,
    @NotNull(message = "Status de conclusão é obrigatório") Boolean completed,
    String notes
) {
}
