package com.habitus.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HabitRequest(
    @NotBlank String name,
    String description,
    @NotBlank String targetFrequency,
    @NotNull @Min(1) Integer timesPerDay,
    String suggestedTimes
) {
}
