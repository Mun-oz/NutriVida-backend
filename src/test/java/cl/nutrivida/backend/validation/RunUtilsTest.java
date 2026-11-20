package cl.nutrivida.backend.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunUtilsTest {

    @Test
    void debeNormalizarRunConPuntosYGuion() {

        String resultado = RunUtils.normalizar("12.345.678-5");

        assertEquals("123456785", resultado);
    }

    @Test
    void debeConvertirKMinusculaAMayuscula() {

        String resultado = RunUtils.normalizar("10.000.013-k");

        assertEquals("10000013K", resultado);
    }

    @Test
    void debeAceptarRunValidoConDigitoNumerico() {

        assertTrue(RunUtils.esValido("12.345.678-5"));
    }

    @Test
    void debeAceptarRunValidoConK() {

        assertTrue(RunUtils.esValido("10.000.013-K"));
    }

    @Test
    void debeRechazarRunConDigitoVerificadorIncorrecto() {

        assertFalse(RunUtils.esValido("12.345.678-4"));
    }

    @Test
    void debeRechazarRunConCantidadIncorrectaDeDigitos() {

        assertFalse(RunUtils.esValido("123456"));
    }

    @Test
    void debeRechazarRunConLetrasInvalidas() {

        assertFalse(RunUtils.esValido("1234567X"));
    }

    @Test
    void debeRechazarRunNulo() {

        assertFalse(RunUtils.esValido(null));
    }

    @Test
    void debeRechazarRunVacio() {

        assertFalse(RunUtils.esValido(""));
    }

    @Test
    void debeRetornarNullCuandoSeNormalizaUnValorVacio() {

        assertNull(RunUtils.normalizar("   "));
    }

}
