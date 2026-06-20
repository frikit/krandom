/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LocaleTextResourceLoader")
class LocaleTextResourceLoaderTest {

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

    @Test
    @DisplayName("load reads a known names file")
    void loadKnownNamesFile() {
        String[] names = LocaleTextResourceLoader.load("krandom/names/first_male/en_US.txt");
        assertTrue(names.length > 10);
    }

    @Test
    @DisplayName("load reads a known professions file")
    void loadKnownProfessionsFile() {
        String[] professions = LocaleTextResourceLoader.load("krandom/professions/en_US.txt");
        assertTrue(professions.length >= 40);
    }

    @Test
    @DisplayName("load skips blank lines and comment lines")
    void loadSkipsBlankAndCommentLines() {
        String content = "# header comment\nAlice\n\n  \nBob\n";
        String[] names = LocaleTextResourceLoader.load(
                new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), "synthetic-entries");
        assertArrayEquals(new String[] {"Alice", "Bob"}, names);
    }

    @Test
    @DisplayName("load throws for missing resource")
    void loadMissingFile() {
        assertThrows(IllegalStateException.class, () -> LocaleTextResourceLoader.load("krandom/names/does_not_exist.txt"));
    }

    @Test
    @DisplayName("load wraps IO failures")
    void loadIoFailure() {
        assertThrows(IllegalStateException.class, () -> LocaleTextResourceLoader.load(closeFailingStream(), "broken-entries"));
    }

    @Test
    @DisplayName("private constructor is callable by reflection")
    void constructorCoverage() throws Exception {
        Constructor<LocaleTextResourceLoader> ctor = LocaleTextResourceLoader.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertDoesNotThrow(() -> {
            ctor.newInstance();
        });
    }
}
