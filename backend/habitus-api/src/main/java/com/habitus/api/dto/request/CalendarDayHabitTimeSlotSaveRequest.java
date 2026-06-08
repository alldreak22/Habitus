package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CalendarDayHabitTimeSlotSaveRequest(
    @NotBlank(message = "Horario e obrigatorio")
    String time,

    @NotNull(message = "Status de conclusao do horario e obrigatorio")
    Boolean completed
) {
}
