/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("WordPhoneticsLoader")
class WordPhoneticsLoaderTest {

    @Test
    @DisplayName("load parses valid profile")
    void loadValidProfile() {
        WordPhonetics data = WordPhoneticsLoader.load("krandom/text/phonetics/test_valid.txt");
        assertArrayEquals(new String[]{"a", "b"}, data.onsets());
        assertArrayEquals(new String[]{"e", "i"}, data.nuclei());
        assertArrayEquals(new String[]{"", "n"}, data.codas());
    }

    @Test
    @DisplayName("load throws for missing resource")
    void loadMissingResource() {
        assertThrows(
                IllegalStateException.class,
                () -> WordPhoneticsLoader.load("krandom/text/phonetics/does_not_exist.txt")
        );
    }

    @Test
    @DisplayName("load throws for malformed resource")
    void loadMalformedResource() {
        assertThrows(
                IllegalStateException.class,
                () -> WordPhoneticsLoader.load("krandom/text/phonetics/test_invalid_missing_codas.txt")
        );
    }
}
