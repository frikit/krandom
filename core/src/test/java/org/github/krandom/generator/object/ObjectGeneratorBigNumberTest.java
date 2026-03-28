/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.core.model.PersonWithBigNumbers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("ObjectGenerator — BigDecimal and BigInteger auto-population")
class ObjectGeneratorBigNumberTest {

    @Test
    @DisplayName("BigDecimal field is auto-populated")
    void bigDecimalPopulated() {
        PersonWithBigNumbers p = new ObjectGenerator<>(PersonWithBigNumbers.class).generate();
        assertNotNull(p.getPrice(), "BigDecimal field must not be null");
    }

    @Test
    @DisplayName("BigInteger field is auto-populated")
    void bigIntegerPopulated() {
        PersonWithBigNumbers p = new ObjectGenerator<>(PersonWithBigNumbers.class).generate();
        assertNotNull(p.getCount(), "BigInteger field must not be null");
    }

    @Test
    @DisplayName("BigDecimal field has scale 2")
    void bigDecimalScaleIsTwo() {
        for (int i = 0; i < 20; i++) {
            PersonWithBigNumbers p = new ObjectGenerator<>(PersonWithBigNumbers.class).generate();
            assertEquals(2, p.getPrice().scale(), "BigDecimal scale must be 2");
        }
    }
}
