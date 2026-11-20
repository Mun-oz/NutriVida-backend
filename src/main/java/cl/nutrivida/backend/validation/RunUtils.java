package cl.nutrivida.backend.validation;

import java.util.Locale;
import java.util.regex.Pattern;

public final class RunUtils {

    private static final Pattern RUN_PATTERN =
           Pattern.compile("^[0-9]{7,8}[0-9K]$");

    private RunUtils() {}

    public static String normalizar(String run) {

        if (run == null) {
            return null;
        }

        String normalizado = run.trim()
                .replace(".", "")
                .replace("-", "")
                .toUpperCase(Locale.ROOT);

        return normalizado.isEmpty() ? null : normalizado;
    }

    public static boolean esValido(String run) {

        String normalizado = normalizar(run);

        if (normalizado == null ||
                !RUN_PATTERN.matcher(normalizado).matches()) {
            return false;
        }

        String cuerpo = normalizado.substring(
                0,
                normalizado.length() - 1
        );

        char digitoVerificador =
                normalizado.charAt(normalizado.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {

            int digito = cuerpo.charAt(i) - '0';

            suma += digito * multiplicador;

            multiplicador = multiplicador < 7
                    ? multiplicador + 1
                    : 2;
        }

        int resultado = 11 - (suma % 11);

        char digitoEsperado;

        if (resultado == 11) {
            digitoEsperado = '0';
        } else if (resultado == 10) {
            digitoEsperado = 'K';
        } else {
            digitoEsperado = (char) ('0' + resultado);
        }

        return digitoEsperado == digitoVerificador;
    }
}
