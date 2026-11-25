package com.springbyexample.v2.toolcalling.tools;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the calculator tools exposed to the AI model.
 *
 * @author Mujuzi Moses
 */
public class CalculatorToolsTest {

    private CalculatorTools calculatorTools;

    @BeforeEach
    void setUp() {
        calculatorTools = new CalculatorTools();
    }

    @Test
    void shouldAddTwoNumbers() {
        BigDecimal result = calculatorTools.add(new BigDecimal("125"), new BigDecimal("8"));

        assertThat(result).isEqualByComparingTo("133");
    }

    @Test
    void shouldSubtractTwoNumbers() {
        BigDecimal result = calculatorTools.subtract(new BigDecimal("125"), new BigDecimal("8"));

        assertThat(result).isEqualByComparingTo("117");
    }

    @Test
    void shouldMultiplyTwoNumbers() {
        BigDecimal result = calculatorTools.multiply(new BigDecimal("125"), new BigDecimal("8"));

        assertThat(result).isEqualByComparingTo("1000");
    }

    @Test
    void shouldPreserveDecimalPrecisionWhenMultiplying() {
        BigDecimal result = calculatorTools.multiply(new BigDecimal("12.5"), new BigDecimal("2.4"));

        assertThat(result).isEqualByComparingTo("30.00");
    }
}
