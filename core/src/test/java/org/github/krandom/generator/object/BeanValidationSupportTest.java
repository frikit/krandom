/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import org.github.krandom.generator.Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("BeanValidationSupport")
class BeanValidationSupportTest {

    static final class Sample {
        @Size(min = 2, max = 4)
        String sizedText;

        @DecimalMin("1.5")
        BigDecimal decimalMinOnly;
    }

    @Test
    @DisplayName("returns generator for sized string")
    void sizedStringConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("sizedText");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, String.class);
        assertNotNull(generator);
    }

    @Test
    @DisplayName("returns null for partial decimal constraints")
    void partialDecimalConstraint() throws Exception {
        Field field = Sample.class.getDeclaredField("decimalMinOnly");
        Generator<?> generator = BeanValidationSupport.constraintGeneratorFor(field, BigDecimal.class);
        assertNull(generator);
    }
}
