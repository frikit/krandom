/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.games.dice;

/**
 * Standard polyhedral dice types used in tabletop games.
 */
public enum DiceType {
    D4(4),
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20);

    private final int sides;

    DiceType(int sides) {
        this.sides = sides;
    }

    /**
     * Number of faces on this die.
     */
    public int sides() {
        return sides;
    }
}
