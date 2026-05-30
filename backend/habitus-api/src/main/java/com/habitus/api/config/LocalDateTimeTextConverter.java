package com.habitus.api.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocalDateTimeTextConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter DATABASE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.format(DATABASE_FORMAT);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();
        try {
            return LocalDateTime.parse(value, DATABASE_FORMAT);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(value);
        }
    }
}
