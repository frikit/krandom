/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.core.model;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Test model with {@link BigDecimal} and {@link BigInteger} fields.
 */
public class PersonWithBigNumbers {

    private BigDecimal price;
    private BigInteger count;

    public PersonWithBigNumbers() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigInteger getCount() {
        return count;
    }
}
