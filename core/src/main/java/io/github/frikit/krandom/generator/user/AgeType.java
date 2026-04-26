/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

/**
 * Age categories used to constrain {@link AgeGenerator} and {@link BirthdayGenerator} output.
 *
 * <p>The ranges mirror those defined by Chance.js:
 * <ul>
 *   <li>{@link #CHILD}  — 1 to 12</li>
 *   <li>{@link #TEEN}   — 13 to 19</li>
 *   <li>{@link #ADULT}  — 18 to 65</li>
 *   <li>{@link #SENIOR} — 65 to 100</li>
 * </ul>
 */
public enum AgeType {

    CHILD(1, 12),
    TEEN(13, 19),
    ADULT(18, 65),
    SENIOR(65, 100);

    private final int minAge;
    private final int maxAge;

    AgeType(int minAge, int maxAge) {
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    /**
     * Minimum age (inclusive) for this category.
     */
    public int getMinAge() {
        return minAge;
    }

    /**
     * Maximum age (inclusive) for this category.
     */
    public int getMaxAge() {
        return maxAge;
    }
}
