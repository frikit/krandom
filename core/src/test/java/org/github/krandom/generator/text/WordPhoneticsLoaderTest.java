/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    @DisplayName("load ignores blank and comment lines")
    void loadWithBlankAndCommentLines() {
        WordPhonetics data = WordPhoneticsLoader.load("krandom/text/phonetics/test_valid_with_blanks.txt");
        assertArrayEquals(new String[]{"br", "cl"}, data.onsets());
        assertArrayEquals(new String[]{"a", "e"}, data.nuclei());
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

    @Test
    @DisplayName("load wraps IO failures")
    void loadIoFailure() {
        assertThrows(IllegalStateException.class, () -> WordPhoneticsLoader.load(readFailingStream(), "broken-phonetics"));
    }

    @Test
    @DisplayName("private constructor is callable by reflection")
    void constructorCoverage() throws Exception {
        Constructor<WordPhoneticsLoader> ctor = WordPhoneticsLoader.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertDoesNotThrow(() -> {
            ctor.newInstance();
        });
    }

    private static InputStream closeFailingStream() {
        return new InputStream() {
            @Override
            public int read() {
                return -1;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("boom");
            }
        };
    }

    private static InputStream readFailingStream() {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };
    }
}
