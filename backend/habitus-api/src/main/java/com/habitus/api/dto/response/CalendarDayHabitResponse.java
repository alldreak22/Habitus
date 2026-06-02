package com.habitus.api.dto.response;

public record CalendarDayHabitResponse(
    Long id,
    String name,
    String icon,
    String color,
    String detail,
    Boolean completed
) {
}
