/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("StreetAddressResourceLoader")
class StreetAddressResourceLoaderTest {

    @Test
    @DisplayName("load reads resource and skips comments/blank lines")
    void loadSkipsCommentsAndBlankLines() {
        String[] values = StreetAddressResourceLoader.load("krandom/streets/en_US_street_names.txt");
        assertTrue(values.length >= 20);
        for (String value : values) {
            assertFalse(value.isBlank());
            assertFalse(value.startsWith("#"));
        }
    }

    @Test
    @DisplayName("load throws when resource does not exist")
    void loadMissingResourceThrows() {
        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> StreetAddressResourceLoader.load("krandom/streets/does_not_exist.txt")
        );
        assertTrue(ex.getMessage().contains("not found"));
    }
}

