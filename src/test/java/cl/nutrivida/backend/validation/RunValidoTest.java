package cl.nutrivida.backend.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunValidoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidator() {

        factory = Validation.buildDefaultValidatorFactory();

        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidator() {

        factory.close();
    }

    @Test
    void debeAceptarRunValido() {

        RunObligatorio bean = new RunObligatorio("12.345.678-5");

        assertTrue(validator.validate(bean).isEmpty());
    }

    @Test
    void debeRechazarRunInvalido() {

        RunObligatorio bean = new RunObligatorio("12.345.678-4");

        assertFalse(validator.validate(bean).isEmpty());
    }

    @Test
    void debeRechazarRunNuloCuandoEsObligatorio() {

        RunObligatorio bean = new RunObligatorio(null);

        assertFalse(validator.validate(bean).isEmpty());
    }

    @Test
    void debeAceptarRunNuloCuandoEsOpcional() {

        RunOpcional bean = new RunOpcional(null);

        assertTrue(validator.validate(bean).isEmpty());
    }

    @Test
    void debeAceptarRunVacioCuandoEsOpcional() {

        RunOpcional bean = new RunOpcional("   ");

        assertTrue(validator.validate(bean).isEmpty());
    }

    @Test
    void debeRechazarRunInvalidoAunqueSeaOpcional() {

        RunOpcional bean = new RunOpcional("12.345.678-4");

        assertFalse(validator.validate(bean).isEmpty());
    }

    static class RunObligatorio {

        @RunValido
        private final String run;

        RunObligatorio(String run) {
            this.run = run;
        }
    }

    static class RunOpcional {

        @RunValido(required = false)
        private final String run;

        RunOpcional(String run) {
            this.run = run;
        }
    }
}
