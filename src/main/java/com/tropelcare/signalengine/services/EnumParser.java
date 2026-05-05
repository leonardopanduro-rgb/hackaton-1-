package com.tropelcare.signalengine.services;

import com.tropelcare.signalengine.exceptions.BadRequestException;
import java.util.Locale;

public final class EnumParser {

    private EnumParser() {
    }

    public static <E extends Enum<E>> E parse(Class<E> enumClass, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException("El campo " + fieldName + " es obligatorio");
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return Enum.valueOf(enumClass, normalized);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Valor invalido para " + fieldName + ": " + value);
        }
    }
}
