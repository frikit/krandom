/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

/**
 * The FNV-1a 64 hash that {@code GenerationRecipe.deriveChildSeed(long, String)} computes, in steps, so that a
 * caller who derives many seeds that share a prefix folds the prefix once and builds no identity string.
 *
 * <p>{@code deriveChildSeed(seed, identity)} equals {@code fold(start(seed), identity.getBytes(UTF_8))} for an
 * identity that it accepts: one that is not blank and holds no line break. Folding the UTF-8 bytes of two
 * strings one after the other equals folding the bytes of their concatenation when the first ends and the
 * second starts with an ASCII character, as no surrogate pair then spans the join.
 *
 * <p>This package is not exported, so this is not public API.
 */
public final class StreamSeeds {

    private static final long FNV1A_64_OFFSET_BASIS = 0xcbf29ce484222325L;
    private static final long FNV1A_64_PRIME = 0x100000001b3L;

    private StreamSeeds() {}

    /**
     * The hash after a parent seed's eight bytes, lowest first, which every derived seed starts with.
     *
     * @param parentSeed seed of the parent stream
     * @return hash state
     */
    public static long start(long parentSeed) {
        long hash = FNV1A_64_OFFSET_BASIS;
        for (int shift = 0; shift < Long.SIZE; shift += Byte.SIZE) {
            hash ^= (parentSeed >>> shift) & 0xffL;
            hash *= FNV1A_64_PRIME;
        }
        return hash;
    }

    /**
     * Folds bytes into a hash state.
     *
     * @param hash  hash state
     * @param bytes bytes to fold, in order
     * @return hash state
     */
    public static long fold(long hash, byte[] bytes) {
        for (byte value : bytes) {
            hash ^= value & 0xffL;
            hash *= FNV1A_64_PRIME;
        }
        return hash;
    }

    /**
     * Folds the characters of {@code Integer.toString(value)}, which are ASCII and so their own UTF-8 bytes.
     *
     * @param hash  hash state
     * @param value integer whose decimal text to fold
     * @return hash state
     */
    public static long foldDecimal(long hash, int value) {
        if (value < 0) {
            hash ^= '-';
            hash *= FNV1A_64_PRIME;
        }
        // The digits of the magnitude, most significant first; the negative range is one longer than the positive.
        long magnitude = Math.abs((long) value);
        long divisor = 1;
        while (divisor * 10 <= magnitude) {
            divisor *= 10;
        }
        for (; divisor > 0; divisor /= 10) {
            hash ^= '0' + (magnitude / divisor) % 10;
            hash *= FNV1A_64_PRIME;
        }
        return hash;
    }
}
