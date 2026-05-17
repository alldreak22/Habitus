package com.habitus.api.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HabitRequest(
    @NotBlank(message = "Nome e obrigatorio") String name,
    String title,
    String icon,
    String color,
    String description,
    @NotBlank(message = "Frequencia alvo e obrigatoria") String targetFrequency,
    @NotNull(message = "Quantidade por dia e obrigatoria") @Min(value = 1, message = "Quantidade por dia deve ser no minimo 1") Integer timesPerDay,
    String suggestedTimes,
    Boolean reminder,
    String frequencyType,
    String status,
    List<String> reminderTimes,
    List<Integer> frequencyDays
) {
    public HabitRequest {
        if ((name == null || name.isBlank()) && title != null && !title.isBlank()) {
            name = title;
        }
        if ((title == null || title.isBlank()) && name != null && !name.isBlank()) {
            title = name;
        }
        if ((targetFrequency == null || targetFrequency.isBlank()) && frequencyType != null && !frequencyType.isBlank()) {
            targetFrequency = frequencyType;
        }
        if (timesPerDay == null && reminderTimes != null && !reminderTimes.isEmpty()) {
            timesPerDay = reminderTimes.size();
        }
        if (timesPerDay == null) {
            timesPerDay = 1;
        }
    }
}
