/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The {@code nationality} name option must resolve {@code "uk"} / {@code "UK"} to the United
 * Kingdom, as documented by the explicit {@code uk -> Locale.UK} mapping, while Ukrainian names stay
 * reachable through the unambiguous {@code uk_UA} / {@code UA} tokens.
 */
@DisplayName("FullNameGenerator nationality tokens")
class FullNameNationalityTest {

    private static FullNameGenerator.NameOptions nationality(String token) {
        return new FullNameGenerator.NameOptions(false, false, false, false, false, Gender.FEMALE, token);
    }

    private static Set<String> firstNames(Locale locale) {
        return Set.copyOf(Arrays.asList(FirstNameDataRegistry.forLocale(locale).getFemaleFirstNames()));
    }

    @Test
    @DisplayName("\"uk\" and \"UK\" yield British names")
    void ukMeansUnitedKingdom() {
        FullNameGenerator generator = new FullNameGenerator(GeneratorConfig.builder().seed(12L).build());
        Set<String> british = firstNames(Locale.UK);
        Set<String> ukrainian = firstNames(Locale.of("uk", "UA"));
        for (String token : List.of("uk", "UK", "gb", "en_GB")) {
            for (int i = 0; i < 25; i++) {
                String first = generator.generate(nationality(token)).split(" ")[0];
                assertTrue(british.contains(first), token + " produced " + first);
                assertTrue(!ukrainian.contains(first), token + " produced a Ukrainian name " + first);
            }
        }
    }

    @Test
    @DisplayName("Ukrainian names remain reachable through unambiguous tokens")
    void ukrainianStillReachable() {
        FullNameGenerator generator = new FullNameGenerator(GeneratorConfig.builder().seed(13L).build());
        Set<String> ukrainian = firstNames(Locale.of("uk", "UA"));
        for (String token : List.of("uk_UA", "uk-UA", "UA", "UK_UA")) {
            for (int i = 0; i < 10; i++) {
                String first = generator.generate(nationality(token)).split(" ")[0];
                assertTrue(ukrainian.contains(first), token + " produced " + first);
            }
        }
    }
}
