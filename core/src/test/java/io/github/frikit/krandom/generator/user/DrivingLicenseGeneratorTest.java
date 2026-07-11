/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DrivingLicenseGenerator")
@SuppressWarnings("removal")
class DrivingLicenseGeneratorTest {

    @RepeatedTest(200)
    @DisplayName("deprecated no-argument constructor preserves the generic driving-license format")
    void formatMatches() {
        String license = new DrivingLicenseGenerator(GeneratorConfig.builder() .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED) .build()).generate();
        assertTrue(license.matches("[A-Z]{2}[0-9]{6}"), license);
    }

    @Test
    @DisplayName("legacy no-argument constructor is removed in v2")
    void legacyConstructorIsRemoved() {
        assertThrows(NoSuchMethodException.class, () -> DrivingLicenseGenerator.class.getConstructor());
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new DrivingLicenseGenerator(realisticConfig(77L)).generateList(25);
        List<String> b = new DrivingLicenseGenerator(realisticConfig(77L)).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("configured generation fails closed by default")
    void configuredGenerationFailsClosedByDefault() {
        assertThrows(IllegalStateException.class,
                     () -> new DrivingLicenseGenerator(GeneratorConfig.defaults()).generate());
        assertThrows(IllegalStateException.class, () -> Generators.ofDrivingLicense().generate());
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new DrivingLicenseGenerator(null));
    }

    @Test
    @DisplayName("facade requires an explicit realistic compatibility policy")
    void facade() {
        assertThrows(IllegalStateException.class, () -> Generators.ofDrivingLicense().generate());
        assertTrue(Generators.ofDrivingLicense(realisticConfig(1L)).generate().matches("[A-Z]{2}[0-9]{6}"));
        assertEquals(
            Generators.ofDrivingLicense(realisticConfig(1L)).generateList(10),
            Generators.ofDrivingLicense(realisticConfig(1L)).generateList(10));
    }

    private static GeneratorConfig realisticConfig(long seed) {
        return GeneratorConfig.builder()
                              .seed(seed)
                              .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                              .build();
    }
}
