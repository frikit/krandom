/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ComputerGenerator")
class ComputerGeneratorTest {

    private static final Set<String> OPERATING_SYSTEMS = Set.of(
        "Windows 11", "Windows 10", "macOS Sonoma", "macOS Ventura", "Ubuntu 22.04 LTS",
        "Fedora 39", "Debian 12", "Android 14", "iOS 17", "ChromeOS");

    private static final Set<String> PLATFORMS = Set.of(
        "Windows", "macOS", "Linux", "Android", "iOS");

    private static final Set<String> DEVICE_TYPES = Set.of(
        "Laptop", "Desktop", "Tablet", "Phone", "Server");

    @RepeatedTest(200)
    @DisplayName("generate, operatingSystem, platform and deviceType return known values")
    void membership() {
        ComputerGenerator computer = new ComputerGenerator();
        assertTrue(OPERATING_SYSTEMS.contains(computer.generate()));
        assertTrue(OPERATING_SYSTEMS.contains(computer.operatingSystem()));
        assertTrue(PLATFORMS.contains(computer.platform()));
        assertTrue(DEVICE_TYPES.contains(computer.deviceType()));
    }

    @Test
    @DisplayName("each method shows variety over many draws")
    void variety() {
        ComputerGenerator computer = new ComputerGenerator();
        Set<String> oses = new HashSet<>();
        Set<String> platforms = new HashSet<>();
        Set<String> devices = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            oses.add(computer.operatingSystem());
            platforms.add(computer.platform());
            devices.add(computer.deviceType());
        }
        assertTrue(oses.size() > 1, "expected more than one distinct OS");
        assertTrue(platforms.size() > 1, "expected more than one distinct platform");
        assertTrue(devices.size() > 1, "expected more than one distinct device type");
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new ComputerGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new ComputerGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new ComputerGenerator(null));
    }

    @Test
    @DisplayName("facade ofComputer produces valid operating systems and is seed-reproducible")
    void facade() {
        assertTrue(OPERATING_SYSTEMS.contains(Generators.ofComputer().generate()));
        assertEquals(
            Generators.ofComputer(GeneratorConfig.builder().seed(1L).build()).generateList(10),
            Generators.ofComputer(GeneratorConfig.builder().seed(1L).build()).generateList(10));
    }
}
