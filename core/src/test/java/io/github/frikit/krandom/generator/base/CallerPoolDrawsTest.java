/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.PasswordGenerator;
import io.github.frikit.krandom.generator.user.PasswordPolicy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Draws from character pools that the caller supplies to {@link CharGenerator}, {@link StringGenerator} and
 * {@link PasswordPolicy}. The values were recorded before these generators drew whole code points, so a pool
 * of characters from the Basic Multilingual Plane, including one that holds an unpaired surrogate, keeps the
 * draws it gave in 3.1.0.
 */
@DisplayName("Draws from caller-supplied character pools")
class CallerPoolDrawsTest {

    private static final String BMP_POOL = "aeioué←↑€";

    private static String chars(CharGenerator generator, int count) {
        StringBuilder drawn = new StringBuilder();
        for (int i = 0; i < count; i++) {
            drawn.append(generator.generate());
        }
        return drawn.toString();
    }

    private static List<String> strings(StringGenerator generator, int count) {
        List<String> drawn = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            drawn.add(generator.generate());
        }
        return drawn;
    }

    private static List<String> passwords(PasswordGenerator generator, PasswordPolicy policy, int count) {
        List<String> drawn = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            drawn.add(generator.generate(policy));
        }
        return drawn;
    }

    @Test
    @DisplayName("a seeded CharGenerator pool of BMP characters keeps its 3.1.0 draws")
    void charGeneratorBmpPoolsKeepTheirDraws() {
        assertEquals("€oa€a↑éi↑ii€←a€é←←e€", chars(CharGenerator.pool(42L, BMP_POOL), 20));
        assertEquals("中中中xßßx中中中ßxxßx中x中xx", chars(CharGenerator.pool(7L, 'x', 'ß', '中', 'x'), 20));
        assertEquals("açaaaabçaççaaçbbçbça", chars(CharGenerator.pool("abç").withSeed(11L), 20));
    }

    @Test
    @DisplayName("a seeded StringGenerator pool of BMP characters keeps its 3.1.0 draws")
    void stringGeneratorBmpPoolsKeepTheirDraws() {
        assertEquals(
                List.of("€oa€a↑", "éi↑ii€", "←a€é←←"),
                strings(
                        StringGenerator.builder()
                                .charGenerator(CharGenerator.pool(BMP_POOL))
                                .length(6)
                                .seed(42L)
                                .build(),
                        3));
        assertEquals(
                List.of("€oa€a↑é", "i↑ii€←a€é", "←←e"),
                strings(
                        StringGenerator.builder()
                                .charGenerator(CharGenerator.pool(BMP_POOL))
                                .minLength(2)
                                .maxLength(9)
                                .seed(42L)
                                .build(),
                        3));
        assertEquals(
                List.of("zxzxy", "ñyzzxñ", "yyy"),
                strings(
                        StringGenerator.builder()
                                .charGenerator(CharGenerator.pool("xyzñ"))
                                .minLength(3)
                                .maxLength(8)
                                .config(GeneratorConfig.builder().seed(42L).build())
                                .build(),
                        3));
    }

    @Test
    @DisplayName("a password policy of BMP characters keeps its 3.1.0 draws")
    void passwordPolicyBmpPoolsKeepTheirDraws() {
        PasswordPolicy custom = PasswordPolicy.builder()
                .length(10, 14)
                .require("äöü", 2)
                .requireDigits(1)
                .require("ößß", 1)
                .build();
        assertEquals(
                List.of("äö8ß07äö74", "69üö8äß937", "äü27ü0ä6üßßß5ö"),
                passwords(
                        new PasswordGenerator(
                                GeneratorConfig.builder().seed(42L).build()),
                        custom,
                        3));
        assertEquals(
                List.of("&8,8*a[6@mDC", "cd3hFQbzT%B+", "z&_OR[ux*_Fm"),
                passwords(
                        new PasswordGenerator(
                                GeneratorConfig.builder().seed(42L).build()),
                        PasswordPolicy.builder().length(12).build(),
                        3));
    }

    @Test
    @DisplayName("a pool that holds an unpaired surrogate keeps its 3.1.0 draws")
    void poolsWithUnpairedSurrogatesKeepTheirDraws() {
        assertEquals("baab𐀀𐀀\ud800\ud800b\udc00", chars(CharGenerator.pool(5L, "a\uD800b\uDC00"), 12));
        assertEquals(
                List.of("z\ud800zz\udc00", "z\ud800zz\udc00", "z\udc00𐀀z", "𐀀\ud800z\udc00"),
                strings(
                        StringGenerator.builder()
                                .charGenerator(CharGenerator.pool("\uDC00\uD800z"))
                                .length(5)
                                .seed(5L)
                                .build(),
                        4));
        assertEquals(
                "\udc00\udc00q\udc00q\ud800",
                new PasswordGenerator(GeneratorConfig.builder().seed(5L).build())
                        .generate(PasswordPolicy.builder()
                                .length(6)
                                .require("\uDC00\uD800", 2)
                                .require("q", 1)
                                .build()));
        // Two sets that each hold one half of a pair stay two characters of the alphabet.
        PasswordGenerator halves =
                new PasswordGenerator(GeneratorConfig.builder().seed(5L).build());
        PasswordPolicy split = PasswordPolicy.builder()
                .length(6)
                .require("\uD800", 0)
                .require("\uDC00", 0)
                .build();
        assertEquals(List.of("\ud800𐀀𐀀\ud800", "𐀀𐀀\udc00\ud800", "𐀀\ud800𐀀\ud800"), passwords(halves, split, 3));
    }
}
