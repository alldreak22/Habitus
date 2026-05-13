package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record DailyHabitPlanRequest(
    @NotNull(message = "Hábito é obrigatório") Long habitId
) {
}
