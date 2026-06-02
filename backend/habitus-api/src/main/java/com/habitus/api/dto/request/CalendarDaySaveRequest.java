package com.habitus.api.dto.request;

import java.util.List;

import jakarta.validation.Valid;

public record CalendarDaySaveRequest(
    String description,

    @Valid
    List<CalendarDayHabitSaveRequest> habits
) {
}
