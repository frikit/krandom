/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.games.coin;

/**
 * The two possible outcomes of a coin flip.
 */
public enum CoinResult {

    HEAD("head"),
    TAIL("tail");

    private final String value;

    CoinResult(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
