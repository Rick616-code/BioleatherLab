package pe.bioleather.api;

import java.util.Locale;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
public final class ApiValidation {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private ApiValidation() {}

    public static String required(String value, int max) {
        if (StringUtils.isBlank(value) || value.strip().length() > max) throw bad("Dato obligatorio o demasiado largo");
        return StringUtils.strip(value);
    }
    public static String email(String input) {
        String value = required(input, 190).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(value).matches()) throw bad("Correo inválido");
        return value;
    }
    public static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
    public static ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No encontrado");
    }
}
