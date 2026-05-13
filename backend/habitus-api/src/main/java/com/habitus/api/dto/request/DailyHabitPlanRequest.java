package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record DailyHabitPlanRequest(
    @NotNull Long habitId
) {
}
