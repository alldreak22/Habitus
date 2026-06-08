package com.habitus.api.dto.response;

public record CalendarHabitTimeSlotResponse(
    String time,
    Boolean completed
) {
}
