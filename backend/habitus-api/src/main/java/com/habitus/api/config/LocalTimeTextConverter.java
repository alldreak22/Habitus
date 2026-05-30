package com.habitus.api.config;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalTimeTextConverter implements AttributeConverter<LocalTime, String> {

    private static final DateTimeFormatter DATABASE_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.withSecond(0).withNano(0).format(DATABASE_FORMAT);
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        try {
            return LocalTime.parse(value, DATABASE_FORMAT);
        } catch (DateTimeParseException ignored) {
            return LocalTime.parse(value).withSecond(0).withNano(0);
        }
    }
}
