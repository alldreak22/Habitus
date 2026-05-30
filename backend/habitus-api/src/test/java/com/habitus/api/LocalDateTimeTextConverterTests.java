package com.habitus.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.habitus.api.config.LocalDateTimeTextConverter;

class LocalDateTimeTextConverterTests {

    private final LocalDateTimeTextConverter converter = new LocalDateTimeTextConverter();

    @Test
    void gravaLocalDateTimeComoTextoCompativelComSqlite() {
        LocalDateTime data = LocalDateTime.of(2026, 5, 25, 15, 3, 53);

        assertEquals("2026-05-25 15:03:53", converter.convertToDatabaseColumn(data));
    }

    @Test
    void leTimestampTextualDoSqlite() {
        LocalDateTime data = converter.convertToEntityAttribute("2026-05-25 15:03:53");

        assertEquals(LocalDateTime.of(2026, 5, 25, 15, 3, 53), data);
    }

}
