package com.habitus.api.dto.request;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HabitRequest(
    @NotBlank(message = "Nome e obrigatorio") @Size(max = 255, message = "Nome deve ter no maximo 255 caracteres") String name,
    String title,
    String icon,
    String color,
    @Size(max = 1000, message = "Descricao deve ter no maximo 1000 caracteres") String description,
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
