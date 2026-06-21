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

@DisplayName("AzureGenerator")
class AzureGeneratorTest {

    private static final Set<String> REGIONS = Set.of(
        "eastus", "westus2", "westeurope", "northeurope",
        "uksouth", "southeastasia", "japaneast", "australiaeast");

    @RepeatedTest(200)
    @DisplayName("generate and region return known Azure regions")
    void regionMembership() {
        AzureGenerator azure = new AzureGenerator();
        assertTrue(REGIONS.contains(azure.generate()));
        assertTrue(REGIONS.contains(azure.region()));
    }

    @RepeatedTest(200)
    @DisplayName("resourceGroup matches the resource-group format and length bounds")
    void resourceGroupFormat() {
        String group = new AzureGenerator().resourceGroup();
        assertTrue(group.matches("rg-[a-z0-9]{4,12}"), group);
    }

    @Test
    @DisplayName("region output shows variety over many draws")
    void regionVariety() {
        AzureGenerator azure = new AzureGenerator();
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 200; i++) {
            seen.add(azure.region());
        }
        assertTrue(seen.size() > 1, "expected more than one distinct region");
    }

    @Test
    @DisplayName("resourceGroup reaches both its minimum and maximum body length over many draws")
    void resourceGroupLengthExtremes() {
        AzureGenerator azure = new AzureGenerator(GeneratorConfig.builder().seed(42L).build());
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < 5000; i++) {
            int bodyLen = azure.resourceGroup().length() - 3; // strip "rg-"
            min = Math.min(min, bodyLen);
            max = Math.max(max, bodyLen);
        }
        assertEquals(4, min);
        assertEquals(12, max);
    }

    @Test
    @DisplayName("same seed is reproducible")
    void reproducible() {
        List<String> a = new AzureGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        List<String> b = new AzureGenerator(GeneratorConfig.builder().seed(77L).build()).generateList(25);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("null config is rejected")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new AzureGenerator(null));
    }

    @Test
    @DisplayName("facade ofAzure produces valid regions and is seed-reproducible")
    void facade() {
        assertTrue(REGIONS.contains(Generators.ofAzure().generate()));
        assertEquals(
            Generators.ofAzure(GeneratorConfig.builder().seed(1L).build()).generateList(10),
            Generators.ofAzure(GeneratorConfig.builder().seed(1L).build()).generateList(10));
    }
}
