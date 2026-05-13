package com.habitus.api.dto.response;

public record HabitResponse(
    Long id,
    String name,
    String description,
    String targetFrequency,
    Integer timesPerDay,
    String suggestedTimes,
    Boolean active
) {
}
