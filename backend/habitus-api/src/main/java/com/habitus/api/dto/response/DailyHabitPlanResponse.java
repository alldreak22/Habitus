package com.habitus.api.dto.response;

public record DailyHabitPlanResponse(
    Long habitId,
    String habitName,
    Boolean planned
) {
}
