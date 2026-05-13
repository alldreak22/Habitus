package com.habitus.api.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record DailyEntryRequest(
    @NotNull LocalDate entryDate,
    String markdownContent,
    String planningNotes
) {
}
