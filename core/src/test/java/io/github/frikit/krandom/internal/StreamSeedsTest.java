/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.GenerationRecipe;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("StreamSeeds folds what GenerationRecipe.deriveChildSeed hashes")
class StreamSeedsTest {

    private static final List<String> NAMES =
            List.of("", "x", "orderId", "straße", "名前", "a|b:c", "😀", "\uD800", "lone \uDC00 low", "trailing \uD83D");

    @Test
    @DisplayName("a schema value's identity folded in steps gives the seed that deriveChildSeed gives")
    void foldedIdentityEqualsDerivedSeed() {
        Random random = new Random(11L);
        int[] indexes = {0, 1, 9, 10, 99, 100, 249, 1_000_000, Integer.MAX_VALUE, -1, -10, Integer.MIN_VALUE};
        for (long seed : new long[] {0L, 7L, -1L, Long.MIN_VALUE, random.nextLong()}) {
            long prefix = StreamSeeds.fold(StreamSeeds.start(seed), "schema|record=".getBytes(StandardCharsets.UTF_8));
            for (String name : NAMES) {
                byte[] suffix = ("|field=" + name.length() + ':' + name).getBytes(StandardCharsets.UTF_8);
                for (int index : indexes) {
                    assertFolds(seed, prefix, suffix, index, name);
                }
                for (int i = 0; i < 200; i++) {
                    assertFolds(seed, prefix, suffix, random.nextInt(), name);
                }
            }
        }
    }

    private static void assertFolds(long seed, long prefix, byte[] suffix, int index, String name) {
        String identity = "schema|record=" + index + "|field=" + name.length() + ':' + name;
        assertEquals(
                GenerationRecipe.deriveChildSeed(seed, identity),
                StreamSeeds.fold(StreamSeeds.foldDecimal(prefix, index), suffix),
                identity);
    }
}
