package com.habitus.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.habitus.api.config.LocalTimeTextConverter;

class LocalTimeTextConverterTests {

    private final LocalTimeTextConverter converter = new LocalTimeTextConverter();

    @Test
    void gravaLocalTimeComoHoraEMinuto() {
        LocalTime horario = LocalTime.of(8, 0, 35);

        assertEquals("08:00", converter.convertToDatabaseColumn(horario));
    }

    @Test
    void leHorarioTextualSemSegundos() {
        LocalTime horario = converter.convertToEntityAttribute("18:30");

        assertEquals(LocalTime.of(18, 30), horario);
    }

}
