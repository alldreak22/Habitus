package com.habitus.api.dto.response;

import java.util.List;

public record HabitResponse(
    Long id,
    String name,
    String title,
    String icon,
    String color,
    String description,
    String targetFrequency,
    Integer timesPerDay,
    String suggestedTimes,
    Boolean active,
    Boolean reminder,
    String frequencyType,
    String status,
    List<String> reminderTimes,
    List<Integer> frequencyDays
) {
}
