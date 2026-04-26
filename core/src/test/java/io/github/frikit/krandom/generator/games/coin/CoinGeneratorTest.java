/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.games.coin;

import io.github.frikit.krandom.generator.Generator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CoinGenerator")
class CoinGeneratorTest {

    private static final EnumSet<CoinResult> VALID = EnumSet.of(CoinResult.HEAD, CoinResult.TAIL);


    @Nested
    @DisplayName("generate()")
    class SingleFlip {

        @RepeatedTest(200)
        @DisplayName("returns HEAD or TAIL")
        void returnsValidResult() {
            assertTrue(VALID.contains(new CoinGenerator().generate()));
        }

        @Test
        @DisplayName("produces both HEAD and TAIL over many flips")
        void producesVariety() {
            CoinGenerator gen = new CoinGenerator();
            EnumSet<CoinResult> seen = EnumSet.noneOf(CoinResult.class);
            for (int i = 0; i < 500; i++) seen.add(gen.generate());
            assertEquals(VALID, seen, "Expected both HEAD and TAIL to appear");
        }
    }


    @Nested
    @DisplayName("flip(n)")
    class MultiFlip {

        @Test
        @DisplayName("returns exactly n results")
        void exactCount() {
            assertEquals(10, new CoinGenerator().flip(10).size());
        }

        @Test
        @DisplayName("all results are valid")
        void allValid() {
            assertTrue(new CoinGenerator().flip(100).stream().allMatch(VALID::contains));
        }

        @Test
        @DisplayName("input above MAX_ALLOWED_SIZE is capped")
        void cappedAtMax() {
            List<CoinResult> results = new CoinGenerator().flip(CoinGenerator.MAX_ALLOWED_SIZE + 1);
            assertEquals(CoinGenerator.MAX_ALLOWED_SIZE, results.size());
        }

        @Test
        @DisplayName("flip(MAX_ALLOWED_SIZE) returns exactly MAX_ALLOWED_SIZE results")
        void exactlyMax() {
            assertEquals(CoinGenerator.MAX_ALLOWED_SIZE, new CoinGenerator().flip(CoinGenerator.MAX_ALLOWED_SIZE).size());
        }
    }


    @Nested
    @DisplayName("Generator<CoinResult> interface")
    class GeneratorInterface {

        @Test
        @DisplayName("generateList(5) returns 5 valid results")
        void generateList() {
            List<CoinResult> list = new CoinGenerator().generateList(5);
            assertEquals(5, list.size());
            assertTrue(list.stream().allMatch(VALID::contains));
        }

        @Test
        @DisplayName("stream() produces valid results")
        void stream() {
            new CoinGenerator().stream().limit(50).forEach(r -> assertTrue(VALID.contains(r)));
        }

        @Test
        @DisplayName("map() transforms CoinResult to its string representation")
        void map() {
            Generator<String> mapped = new CoinGenerator().map(CoinResult::toString);
            String value = mapped.generate();
            assertTrue(value.equals("head") || value.equals("tail"));
        }
    }


    @Nested
    @DisplayName("CoinResult enum")
    class CoinResultEnum {

        @Test
        @DisplayName("HEAD toString is 'head'")
        void headValue() {
            assertEquals("head", CoinResult.HEAD.toString());
        }

        @Test
        @DisplayName("TAIL toString is 'tail'")
        void tailValue() {
            assertEquals("tail", CoinResult.TAIL.toString());
        }

        @Test
        @DisplayName("exactly two constants")
        void twoConstants() {
            assertEquals(2, CoinResult.values().length);
        }
    }
}
