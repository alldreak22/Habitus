package com.habitus.api.dto.response;

import java.util.List;

public record CalendarMonthResponse(
    Integer year,
    Integer month,
    List<CalendarDayResponse> days
) {
}
