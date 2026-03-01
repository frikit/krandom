/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.system;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("System generators")
class SystemGeneratorsTest {

    @Test
    @DisplayName("version generator delegates semver variants")
    void version() {
        VersionGenerator generator = new VersionGenerator(GeneratorConfig.builder().seed(21L).build());
        assertTrue(generator.generate().matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue(generator.generateStable().matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue(generator.generatePrerelease().matches("\\d+\\.\\d+\\.\\d+-(alpha|beta|rc)\\.\\d+"));
        assertThrows(NullPointerException.class, () -> new VersionGenerator(null));
    }

    @Test
    @DisplayName("platform id generator returns expected families")
    void platformId() {
        PlatformIdGenerator generator = new PlatformIdGenerator(GeneratorConfig.builder().seed(5L).build());
        String platform = generator.generatePlatform();
        String arch = generator.generateArchitecture();
        String full = generator.generatePlatformId();
        assertTrue(platform.matches("windows|linux|macos|android|ios"));
        assertTrue(arch.matches("x86|x64|arm64"));
        assertTrue(full.matches("(windows|linux|macos|android|ios)-(x86|x64|arm64)"));
        assertThrows(NullPointerException.class, () -> new PlatformIdGenerator(null));
    }

    @Test
    @DisplayName("exception payload generator provides standard keys")
    void exceptionPayload() {
        ExceptionPayloadGenerator generator = new ExceptionPayloadGenerator(GeneratorConfig.builder().seed(9L).build());
        Map<String, String> payload = generator.generate();
        assertEquals(4, payload.size());
        assertTrue(payload.containsKey("type"));
        assertTrue(payload.containsKey("message"));
        assertTrue(payload.containsKey("code"));
        assertTrue(payload.containsKey("timestamp"));
        assertTrue(payload.get("code").matches("ERR-\\d{4}"));
        assertThrows(NullPointerException.class, () -> new ExceptionPayloadGenerator(null));
    }

    @Test
    @DisplayName("generators factory exposes system generators")
    void factories() {
        assertNotNull(Generators.ofVersion().generate());
        assertNotNull(Generators.ofPlatformId().generate());
        assertNotNull(Generators.ofExceptionPayload().generate());
    }
}
