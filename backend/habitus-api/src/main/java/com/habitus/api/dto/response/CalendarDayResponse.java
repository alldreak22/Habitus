package com.habitus.api.dto.response;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayResponse(
    LocalDate date,
    Boolean manual,
    Long entryId,
    String description,
    Boolean completed,
    List<CalendarHabitMarkerResponse> markers,
    List<CalendarDayHabitResponse> habits
) {
}
