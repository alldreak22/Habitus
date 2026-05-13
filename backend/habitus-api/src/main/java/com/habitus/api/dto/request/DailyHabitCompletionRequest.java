package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record DailyHabitCompletionRequest(
    @NotNull Long habitId,
    @NotNull Boolean completed,
    String notes
) {
}
