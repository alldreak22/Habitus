package com.habitus.api.dto.response;

public record CalendarHabitMarkerResponse(
    Long habitId,
    String name,
    String color,
    String time,
    Boolean completed
) {
}
