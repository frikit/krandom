/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ConfigAccess")
class ConfigAccessTest {

    @Test
    @DisplayName("keeps the copy GeneratorConfig installs and refuses a second one")
    void refusesASecondInstall() {
        GeneratorConfig config = GeneratorConfig.builder().seed(5L).build();

        IllegalStateException error =
                assertThrows(IllegalStateException.class, () -> ConfigAccess.install((parent, seed) -> parent));

        assertEquals("ConfigAccess is installed already", error.getMessage());
        assertEquals(9L, ConfigAccess.withSeed(config, 9L).getSeed().getAsLong());
    }
}
