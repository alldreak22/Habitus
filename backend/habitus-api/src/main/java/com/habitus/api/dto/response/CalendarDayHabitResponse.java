package com.habitus.api.dto.response;

import java.util.List;

public record CalendarDayHabitResponse(
    Long id,
    String name,
    String icon,
    String color,
    String detail,
    Boolean completed,
    List<CalendarHabitTimeSlotResponse> timeSlots
) {
}
