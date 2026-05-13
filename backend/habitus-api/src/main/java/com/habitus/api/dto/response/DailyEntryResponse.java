package com.habitus.api.dto.response;

import java.time.LocalDate;
import java.util.List;

public record DailyEntryResponse(
    Long id,
    LocalDate entryDate,
    String markdownContent,
    String planningNotes,
    List<DailyHabitPlanResponse> plannedHabits,
    List<DailyHabitCompletionResponse> completedHabits
) {
}
