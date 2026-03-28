/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.core.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Test model that covers all Bean Validation constraint paths in
 * {@code BeanValidationSupport}, including boxed types and every
 * sign-constraint variant for both {@code int}/{@code Integer} and
 * {@code long}/{@code Long}.
 */
public class PersonWithAllConstraints {

    // ── int / Integer sign constraints ───────────────────────────────────────

    @Positive
    int positiveInt;

    @PositiveOrZero
    int posOrZeroInt;

    @Negative
    int negativeInt;

    @NegativeOrZero
    int negOrZeroInt;

    /**
     * @Min only — no @Max on int
     */
    @Min(5)
    int minOnlyInt;

    /**
     * @Max only — no @Min on int
     */
    @Max(100)
    int maxOnlyInt;

    /**
     * Boxed Integer with @Min + @Max to exercise the Integer branch
     */
    @Min(1)
    @Max(50)
    Integer boxedInt;

    // ── long / Long sign constraints ──────────────────────────────────────────

    @PositiveOrZero
    long posOrZeroLong;

    @Negative
    long negativeLong;

    @NegativeOrZero
    long negOrZeroLong;

    /**
     * @Min + @Max on long
     */
    @Min(10)
    @Max(1000)
    long minMaxLong;

    /**
     * @Min only on long
     */
    @Min(5)
    long minOnlyLong;

    /**
     * @Max only on long
     */
    @Max(200)
    long maxOnlyLong;

    /**
     * Boxed Long with @Positive to exercise the Long branch
     */
    @Positive
    Long boxedLong;

    public PersonWithAllConstraints() {
    }

    public int getPositiveInt() {
        return positiveInt;
    }

    public int getPosOrZeroInt() {
        return posOrZeroInt;
    }

    public int getNegativeInt() {
        return negativeInt;
    }

    public int getNegOrZeroInt() {
        return negOrZeroInt;
    }

    public int getMinOnlyInt() {
        return minOnlyInt;
    }

    public int getMaxOnlyInt() {
        return maxOnlyInt;
    }

    public Integer getBoxedInt() {
        return boxedInt;
    }

    public long getPosOrZeroLong() {
        return posOrZeroLong;
    }

    public long getNegativeLong() {
        return negativeLong;
    }

    public long getNegOrZeroLong() {
        return negOrZeroLong;
    }

    public long getMinMaxLong() {
        return minMaxLong;
    }

    public long getMinOnlyLong() {
        return minOnlyLong;
    }

    public long getMaxOnlyLong() {
        return maxOnlyLong;
    }

    public Long getBoxedLong() {
        return boxedLong;
    }
}
