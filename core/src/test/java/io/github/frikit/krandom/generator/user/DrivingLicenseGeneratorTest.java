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
class DrivingLicenseGeneratorTest {

    @RepeatedTest(200)
    @DisplayName("output matches the generic driving-license format")
    void formatMatches() {
        String license = new DrivingLicenseGenerator().generate();
        assertTrue(license.matches("[A-Z]{2}[0-9]{6}"), license);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new DrivingLicenseGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new DrivingLicenseGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new DrivingLicenseGenerator(null));
    }

    @Test
    @DisplayName("facade ofDrivingLicense produces valid license numbers and is seed-reproducible")
    void facade() {
        assertTrue(Generators.ofDrivingLicense().generate().matches("[A-Z]{2}[0-9]{6}"));
        assertEquals(
            Generators.ofDrivingLicense(GeneratorConfig.builder().seed(1L).build()).generateList(10),
            Generators.ofDrivingLicense(GeneratorConfig.builder().seed(1L).build()).generateList(10));
    }
}
