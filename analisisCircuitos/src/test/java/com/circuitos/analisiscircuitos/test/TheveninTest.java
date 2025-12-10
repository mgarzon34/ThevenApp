package com.circuitos.analisiscircuitos.test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.circuitos.analisiscircuitos.analisis.Analizador;
import com.circuitos.analisiscircuitos.analisis.ResultadoNorton;
import com.circuitos.analisiscircuitos.analisis.ResultadoThevenin;
import com.circuitos.analisiscircuitos.dominio.*;
import com.circuitos.analisiscircuitos.dominio.FuenteDependiente.ControlType;

/**
 * Clase de pruebas unitarias para verificar el motor de cálculo de Thevenin (JUnit 5).
 */
class TheveninTest {

    // Margen de error aceptable para comparaciones de punto flotante
    private static final double DELTA = 0.01;

    @Test
    @DisplayName("Test 0: Divisor de Tensión")
    void testDivisorTension() {
        Circuito circuito=new Circuito();
        circuito.addComponente(new FuenteTensionInd(10.0, 0, 2));
        circuito.addComponente(new Resistencia(1000.0, 2, 1));
        circuito.addComponente(new Resistencia(1000.0, 1, 0));
        circuito.addComponente(new Tierra(0));
        
        Analizador analizador=new Analizador();
        ResultadoThevenin resultado=analizador.calculaThevenin(circuito, 1, 0);
        assertEquals(5.0, resultado.getVth(), DELTA, "La tensión de Thevenin debe ser 5V");
        assertEquals(500.0, resultado.getRth(), DELTA, "La resistencia de Thevenin debe ser 500Ω");
    }

    @Test
    @DisplayName("Test 1: Circuito Mixto Simple")
    void testLanzador1() {
        Circuito circuito=new Circuito();
        // Vth=4V, Rth=4333.333Ω
        circuito.addComponente(new FuenteTensionInd(12, 0, 1));
        circuito.addComponente(new FuenteCorrienteInd("4m", 1, 2));
        circuito.addComponente(new Resistencia("3k", 0, 2));
        circuito.addComponente(new Resistencia("6k", 2, 3, true)); // Carga
        circuito.addComponente(new Resistencia("4k", 0, 3));
        circuito.addComponente(new Resistencia("2k", 1, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 2, 3);
        assertEquals(4.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 1");
        assertEquals(4333.33, resTh.getRth(), 1.0, "Rth incorrecto para Test 1"); // Delta mayor por decimales
    }

    @Test
    @DisplayName("Test 2: Fuentes Mixtas")
    void testLanzador2() {
        Circuito circuito=new Circuito();
        // Vth=1.2V, Rth=7200Ω
        circuito.addComponente(new Resistencia(12000, 0, 1));
        circuito.addComponente(new FuenteCorrienteInd(0.002, 1, 2));
        circuito.addComponente(new Resistencia(12000, 0, 2));
        circuito.addComponente(new Resistencia(6000, 2, 3));
        circuito.addComponente(new Resistencia(4000, 0, 3, true)); // Carga
        circuito.addComponente(new FuenteTensionInd(6, 1, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 3);
        assertEquals(1.2, resTh.getVth(), DELTA, "Vth incorrecto para Test 2");
        assertEquals(7200.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 2");
    }

    @Test
    @DisplayName("Test 3: Fuentes Mixtas")
    void testLanzador3() {
        Circuito circuito=new Circuito();
        // Vth=-10V, Rth=2000Ω
        circuito.addComponente(new Resistencia(4000, 0, 1));
        circuito.addComponente(new FuenteTensionInd(12, 1, 2));
        circuito.addComponente(new Resistencia(6000, 0, 2, true)); // Carga
        circuito.addComponente(new FuenteCorrienteInd(0.002, 2, 3));
        circuito.addComponente(new Resistencia(2000, 0, 3));
        circuito.addComponente(new Resistencia(2000, 1, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 2);
        assertEquals(-10.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 3");
        assertEquals(2000.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 3");
    }

    @Test
    @DisplayName("Test 4: Múltiples Fuentes de Corriente")
    void testLanzador4() {
        Circuito circuito=new Circuito();
        // Vth=10V, Rth=3000Ω
        circuito.addComponente(new Resistencia(2000, 0, 1));
        circuito.addComponente(new FuenteCorrienteInd(0.002, 2, 1));
        circuito.addComponente(new Resistencia(1000, 1, 3, true)); // Carga
        circuito.addComponente(new FuenteCorrienteInd(0.004, 0, 3));
        circuito.addComponente(new Resistencia(1000, 3, 4));
        circuito.addComponente(new Resistencia(2000, 2, 3));
        circuito.addComponente(new FuenteCorrienteInd(0.004, 4, 2));
        circuito.addComponente(new FuenteTensionInd(12, 4, 0));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 1, 3);
        assertEquals(10.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 4");
        assertEquals(3000.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 4");
    }

    @Test
    @DisplayName("Test 5: Norton")
    void testLanzador5() {
        Circuito circuito=new Circuito();
        // Datos Norton: In=2mA, Rn=3000Ω
        circuito.addComponente(new FuenteTensionInd(6, 0, 1));
        circuito.addComponente(new Resistencia(6000, 1, 2));
        circuito.addComponente(new Resistencia(2000, 0, 2, true)); // Carga
        circuito.addComponente(new Resistencia(3000, 2, 3));
        circuito.addComponente(new FuenteCorrienteInd(0.002, 0, 3));
        circuito.addComponente(new Resistencia(3000, 0, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoNorton resNo=analizador.calculaNorton(circuito, 0, 2);
        assertEquals(-0.002, resNo.getIn(), DELTA, "In incorrecto para Test 5");
        assertEquals(3000.0, resNo.getRn(), DELTA, "Rn incorrecto para Test 5");
    }

    @Test
    @DisplayName("Test 6: Circuito complejo Norton")
    void testLanzador6() {
        Circuito circuito = new Circuito();
        //Datos Norton: In=-1.333mA, Rn=3000Ω
        circuito.addComponente(new Resistencia(1000, 0, 1));
        circuito.addComponente(new FuenteCorrienteInd(0.004, 1, 2));
        circuito.addComponente(new Resistencia(1000, 1, 3));
        circuito.addComponente(new Resistencia(2000, 3, 2));
        circuito.addComponente(new Resistencia(1000, 3, 4));
        circuito.addComponente(new FuenteCorrienteInd(0.002, 4, 2));
        circuito.addComponente(new Resistencia(2000, 0, 4, true)); // Carga
        circuito.addComponente(new FuenteCorrienteInd(0.001, 0, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoNorton resNo=analizador.calculaNorton(circuito, 0, 4);
        assertEquals(-0.00133, resNo.getIn(), 0.05, "Vth incorrecto para Test 6");
        assertEquals(3000.0, resNo.getRn(), DELTA, "Rth incorrecto para Test 6");
    }

    @Test
    @DisplayName("Test 7: Valores con String")
    void testLanzador7() {
        Circuito circuito=new Circuito();
        // In=5mA, Rn=2117.6Ω
        circuito.addComponente(new FuenteTensionInd(12, 0, 1));
        circuito.addComponente(new Resistencia("6k", 1, 2));
        circuito.addComponente(new Resistencia("2k", 0, 2));
        circuito.addComponente(new Resistencia("3k", 2, 3));
        circuito.addComponente(new Resistencia("4k", 0, 3));
        circuito.addComponente(new Resistencia("2k", 1, 3, true)); // Carga
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoNorton resNo=analizador.calculaNorton(circuito, 1, 3);
        assertEquals(0.005, resNo.getIn(), 0.1, "Vth incorrecto para Test 7");
        assertEquals(2117.64, resNo.getRn(), 1.0, "Rth incorrecto para Test 7");
    }

    @Test
    @DisplayName("Test 8: Fuente Dependiente CCVS")
    void testLanzador8() {
        Circuito circuito=new Circuito();
        // Vth=-24V, Rth=6000Ω
        circuito.addComponente(new FuenteTensionDependiente(4000, 1, 0, ControlType.CORRIENTE, 2, 1));
        circuito.addComponente(new Resistencia("10k", 0, 1));
        circuito.addComponente(new Resistencia("10k", 1, 2));
        circuito.addComponente(new FuenteCorrienteInd("4m", 0, 2));
        circuito.addComponente(new Resistencia("10k", 0, 2, true)); // Carga
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 2);
        assertEquals(-24.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 8");
        assertEquals(6000.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 8");
    }

    @Test
    @DisplayName("Test 9: Fuente Dependiente VCCS")
    void testLanzador9() {
        Circuito circuito=new Circuito();
        // Vth=-24V, Rth=14000Ω
        circuito.addComponente(new FuenteCorrienteInd("2m", 0, 1));
        circuito.addComponente(new FuenteCorrienteDependiente(1.0/2000, 1, 2, ControlType.TENSION, 0, 3));
        circuito.addComponente(new Resistencia("4k", 1, 3));
        circuito.addComponente(new Resistencia("2k", 3, 2));
        circuito.addComponente(new Resistencia("6k", 0, 3));
        circuito.addComponente(new Resistencia("6k", 0, 2, true)); // Carga
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 2);
        assertEquals(-24.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 9");
        assertEquals(14000.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 9");
    }

    @Test
    @DisplayName("Test 10: Fuente Dependiente VCVS")
    void testLanzador10() {
        Circuito circuito=new Circuito();
        // Vth=-20V, Rth=6Ω
        circuito.addComponente(new FuenteCorrienteInd(5, 0, 1));
        circuito.addComponente(new Resistencia(4, 0, 1));
        circuito.addComponente(new Resistencia(2, 1, 2));
        circuito.addComponente(new FuenteTensionDependiente(2, 1, 2, ControlType.TENSION, 0, 1));
        circuito.addComponente(new Resistencia(6, 0, 2));
        circuito.addComponente(new Resistencia(2, 2, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 3);
        assertEquals(-20.0, resTh.getVth(), DELTA, "Vth incorrecto para Test 10");
        assertEquals(6.0, resTh.getRth(), DELTA, "Rth incorrecto para Test 10");
    }

    @Test
    @DisplayName("Test 11: Circuito Complejo Thevenin")
    void testLanzador11() {
        Circuito circuito=new Circuito();
        // Vth=-40.64V, Rth=83.93Ω
        circuito.addComponente(new Resistencia("2.2k", 0, 1));
        circuito.addComponente(new Resistencia(750, 1, 2));
        circuito.addComponente(new FuenteTensionInd(5, 0, 2));
        circuito.addComponente(new Resistencia(330, 2, 3));
        circuito.addComponente(new FuenteCorrienteInd("500m", 3, 4));
        circuito.addComponente(new Resistencia(500, 2, 4));
        circuito.addComponente(new Resistencia(82, 4, 5));
        circuito.addComponente(new Resistencia(47, 5, 6));
        circuito.addComponente(new FuenteTensionInd(12, 7, 6));
        circuito.addComponente(new Resistencia(27, 7, 4));
        circuito.addComponente(new Resistencia(100, 0, 7));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 7);
        assertEquals(-40.64, resTh.getVth(), 0.1, "Vth incorrecto para Test 11");
        assertEquals(83.93, resTh.getRth(), 0.1, "Rth incorrecto para Test 11");
    }

    @Test
    @DisplayName("Test 12: Fuente Dependiente CCCS")
    void testLanzador12() {
        Circuito circuito=new Circuito();
        // Vth=-5.053, Rth=9.211Ω
        circuito.addComponente(new FuenteCorrienteDependiente(4.5, 0, 1, ControlType.CORRIENTE, 2, 0));
        circuito.addComponente(new Resistencia(10, 0, 1));
        circuito.addComponente(new FuenteTensionInd(12, 1, 2));
        circuito.addComponente(new Resistencia(40, 0, 2));
        circuito.addComponente(new Resistencia(5, 2, 3));
        circuito.addComponente(new Tierra(0));

        Analizador analizador=new Analizador();
        ResultadoThevenin resTh=analizador.calculaThevenin(circuito, 0, 3);
        assertEquals(-5.053, resTh.getVth(), DELTA, "Vth incorrecto para Test 12");
        assertEquals(9.211, resTh.getRth(), DELTA, "Rth incorrecto para Test 12");
    }
}