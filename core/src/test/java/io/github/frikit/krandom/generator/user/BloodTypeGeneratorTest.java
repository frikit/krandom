/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BloodTypeGenerator")
class BloodTypeGeneratorTest {

    private static final Set<String> ABO_RH = Set.of("O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-");

    private static List<String> draw(BloodTypeGenerator gen, int n) {
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(gen.generate());
        }
        return out;
    }

    @Nested
    @DisplayName("generate()")
    class Generate {

        @RepeatedTest(200)
        @DisplayName("default ctor returns a valid ABO/Rh type")
        void validDefault() {
            assertTrue(ABO_RH.contains(new BloodTypeGenerator().generate()));
        }

        @RepeatedTest(100)
        @DisplayName("US locale returns a valid type")
        void validUs() {
            assertTrue(ABO_RH.contains(new BloodTypeGenerator(Locale.US).generate()));
        }

        @Test
        @DisplayName("unmapped locale falls back to the global default and stays valid")
        void fallbackToDefault() {
            BloodTypeGenerator gen = new BloodTypeGenerator(Locale.GERMANY);
            for (int i = 0; i < 200; i++) {
                assertTrue(ABO_RH.contains(gen.generate()));
            }
        }

        @Test
        @DisplayName("Japan distribution surfaces its most common type (A+)")
        void japanCommon() {
            BloodTypeGenerator gen =
                new BloodTypeGenerator(GeneratorConfig.builder().locale(Locale.JAPAN).seed(1L).build());
            Set<String> seen = new HashSet<>();
            for (int i = 0; i < 500; i++) {
                seen.add(gen.generate());
            }
            assertTrue(seen.contains("A+"), "A+ (weight 398) should appear");
            assertTrue(ABO_RH.containsAll(seen));
        }

        @Test
        @DisplayName("same seed + locale is reproducible")
        void reproducible() {
            GeneratorConfig cfg = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
            List<String> first = draw(new BloodTypeGenerator(cfg), 50);
            List<String> second =
                draw(new BloodTypeGenerator(GeneratorConfig.builder().locale(Locale.US).seed(42L).build()), 50);
            assertEquals(first, second);
        }
    }

    @Nested
    @DisplayName("Generator<String> interface")
    class Iface {

        @Test
        @DisplayName("generateList(n) returns n valid types")
        void list() {
            List<String> list = new BloodTypeGenerator(Locale.US).generateList(8);
            assertEquals(8, list.size());
            assertTrue(list.stream().allMatch(ABO_RH::contains));
        }

        @Test
        @DisplayName("stream() produces valid types")
        void stream() {
            new BloodTypeGenerator(Locale.US).stream().limit(20).forEach(t -> assertTrue(ABO_RH.contains(t)));
        }
    }

    @Nested
    @DisplayName("BloodTypeDataRegistry")
    class Registry {

        @Test
        @DisplayName("isRegistered: built-ins yes, unknown/null no")
        void isRegistered() {
            assertTrue(BloodTypeDataRegistry.isRegistered(Locale.US));
            assertTrue(BloodTypeDataRegistry.isRegistered(Locale.JAPAN));
            assertTrue(BloodTypeDataRegistry.isRegistered(Locale.of("en", "CA")), "language fallback");
            assertFalse(BloodTypeDataRegistry.isRegistered(Locale.GERMANY));
            assertFalse(BloodTypeDataRegistry.isRegistered(Locale.of("xx", "YY")));
            assertFalse(BloodTypeDataRegistry.isRegistered(null));
        }

        @Test
        @DisplayName("forLocale: exact, language-fallback, and null")
        void forLocale() {
            assertNotNull(BloodTypeDataRegistry.forLocale(Locale.US));
            assertNotNull(BloodTypeDataRegistry.forLocale(Locale.of("ja", "XX")));
            assertNotNull(BloodTypeDataRegistry.forLocale(Locale.JAPANESE));
            assertNull(BloodTypeDataRegistry.forLocale(Locale.GERMAN));
            assertNull(BloodTypeDataRegistry.forLocale(null));
        }

        @Test
        @DisplayName("registeredKeys contains the built-in keys")
        void registeredKeys() {
            Set<String> keys = BloodTypeDataRegistry.registeredKeys();
            assertTrue(keys.contains("en_US"));
            assertTrue(keys.contains("ja_JP"));
        }
    }

    @Nested
    @DisplayName("Generators facade")
    class Facade {

        @Test
        @DisplayName("ofBloodType() / (Locale) / (GeneratorConfig) produce valid types")
        void facadeFactories() {
            assertTrue(ABO_RH.contains(Generators.ofBloodType().generate()));
            assertTrue(ABO_RH.contains(Generators.ofBloodType(Locale.US).generate()));
            assertTrue(ABO_RH.contains(
                Generators.ofBloodType(GeneratorConfig.builder().locale(Locale.US).seed(1L).build()).generate()));
        }
    }

    @Nested
    @DisplayName("BloodTypeDataProvider")
    class Provider {

        @Test
        @DisplayName("US provider exposes parallel, positive-weighted types")
        void parallelTypesAndWeights() {
            BloodTypeDataProvider p = BloodTypeDataRegistry.forLocale(Locale.US);
            assertNotNull(p);
            assertEquals(Locale.US, p.getLocale());
            assertEquals(p.getTypes().size(), p.getWeights().size());
            assertTrue(p.getTypes().contains("O+"));
            assertTrue(p.getWeights().stream().allMatch(w -> w > 0));
        }
    }
}
