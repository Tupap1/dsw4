package co.edu.udec.subastas.domain.valueobjects;

import co.edu.udec.subastas.domain.exceptions.SubastaException;
import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new SubastaException("El correo electrónico no puede estar vacío");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new SubastaException("El formato del correo electrónico es inválido");
        }
    }
}
