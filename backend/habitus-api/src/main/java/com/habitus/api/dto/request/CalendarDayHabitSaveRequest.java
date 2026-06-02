package com.habitus.api.dto.request;

import jakarta.validation.constraints.NotNull;

public record CalendarDayHabitSaveRequest(
    @NotNull(message = "Habito e obrigatorio")
    Long habitId,

    @NotNull(message = "Status de conclusao e obrigatorio")
    Boolean completed
) {
}
