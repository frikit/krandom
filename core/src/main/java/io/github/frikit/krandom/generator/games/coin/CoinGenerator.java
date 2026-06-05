/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.games.coin;

import io.github.frikit.krandom.generator.Generator;

import java.util.List;
import java.util.Random;

/**
 * Generates random coin-flip results.
 *
 * <p>Each call to {@link #generate()} returns either {@link CoinResult#HEAD} or
 * {@link CoinResult#TAIL} with equal probability.
 *
 * <pre>{@code
 *   CoinGenerator coin = new CoinGenerator();
 *   CoinResult    side = coin.generate();           // HEAD or TAIL
 *   List<CoinResult> flips = coin.flip(10);         // 10 results
 *
 *   // Via the Generators factory
 *   CoinResult r = Generators.ofCoin().generate();
 * }</pre>
 */
public final class CoinGenerator implements Generator<CoinResult> {

    /**
     * Maximum number of results returned by {@link #flip(int)}.
     */
    public static final int MAX_ALLOWED_SIZE = Short.MAX_VALUE;

    private final Random random = new Random();

    @Override
    public CoinResult generate() {
        return random.nextBoolean() ? CoinResult.HEAD : CoinResult.TAIL;
    }

    /**
     * Flip the coin {@code nrTimes} times and return the results.
     *
     * <p>If {@code nrTimes} exceeds {@link #MAX_ALLOWED_SIZE} it is silently capped.
     *
     * @param nrTimes desired number of flips; values above {@link #MAX_ALLOWED_SIZE} are capped
     * @return immutable list of results, size = {@code min(nrTimes, MAX_ALLOWED_SIZE)}
     */
    public List<CoinResult> flip(int nrTimes) {
        int times = Math.min(nrTimes, MAX_ALLOWED_SIZE);
        return generateList(times);
    }
}
