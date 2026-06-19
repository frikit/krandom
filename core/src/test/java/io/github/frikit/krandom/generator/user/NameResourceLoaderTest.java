/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("NameResourceLoader")
class NameResourceLoaderTest {

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
    @DisplayName("load reads known names file")
    void loadKnownFile() {
        String[] names = NameResourceLoader.load("krandom/names/first_male/en_US.txt");
        assertTrue(names.length > 10);
    }

    @Test
    @DisplayName("load throws for missing resource")
    void loadMissingFile() {
        assertThrows(IllegalStateException.class, () -> NameResourceLoader.load("krandom/names/does_not_exist.txt"));
    }

    @Test
    @DisplayName("load wraps IO failures")
    void loadIoFailure() {
        assertThrows(IllegalStateException.class, () -> NameResourceLoader.load(closeFailingStream(), "broken-names"));
    }

    @Test
    @DisplayName("private constructor is callable by reflection")
    void constructorCoverage() throws Exception {
        Constructor<NameResourceLoader> ctor = NameResourceLoader.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertDoesNotThrow(() -> {
            ctor.newInstance();
        });
    }
}
