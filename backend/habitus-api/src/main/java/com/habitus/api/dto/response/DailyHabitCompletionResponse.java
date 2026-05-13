package com.habitus.api.dto.response;

import java.time.LocalDateTime;

public record DailyHabitCompletionResponse(
    Long habitId,
    String habitName,
    Boolean completed,
    LocalDateTime completedAt,
    String notes
) {
}
