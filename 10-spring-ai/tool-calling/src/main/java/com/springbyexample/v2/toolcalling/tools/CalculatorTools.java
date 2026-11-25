package com.springbyexample.v2.toolcalling.tools;

import java.math.BigDecimal;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Provides calculator operations that can be invoked by an AI model.
 *
 * @author Mujuzi Moses
 */
@Component
public class CalculatorTools {

    @Tool(description = "Add two numbers and return their sum.")
    public BigDecimal add(@ToolParam(description = "The first number") BigDecimal firstNumber,
            @ToolParam(description = "The second number") BigDecimal secondNumber) {
        return firstNumber.add(secondNumber);
    }

    @Tool(description = "Subtract the second number from the first number.")
    public BigDecimal subtract(@ToolParam(description = "The first number") BigDecimal firstNumber,
            @ToolParam(description = "The number to subtract") BigDecimal secondNumber) {
        return firstNumber.subtract(secondNumber);
    }

    @Tool(description = "Multiply two numbers and return their product.")
    public BigDecimal multiply(@ToolParam(description = "The first number") BigDecimal firstNumber,
            @ToolParam(description = "The second number") BigDecimal secondNumber) {
        return firstNumber.multiply(secondNumber);
    }
}
