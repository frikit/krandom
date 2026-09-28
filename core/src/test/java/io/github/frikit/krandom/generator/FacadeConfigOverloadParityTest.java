/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every no-arg {@code Generators.ofX()} factory must have an {@code ofX(GeneratorConfig)}
 * counterpart so that seeds, locales, clocks and safety policies can be supplied through a
 * replayable configuration instead of a raw {@code long} seed.
 */
@DisplayName("Generators facade configuration overloads")
class FacadeConfigOverloadParityTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    private static List<Method> publicStaticFactories() {
        return Arrays.stream(Generators.class.getMethods())
            .filter(method -> method.getDeclaringClass() == Generators.class)
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getName().startsWith("of"))
            .toList();
    }

    @Test
    @DisplayName("every non-deprecated no-arg factory has an ofX(GeneratorConfig) overload")
    void everyNoArgFactoryHasConfigOverload() {
        Set<String> withConfig = publicStaticFactories().stream()
            .filter(method -> Arrays.equals(method.getParameterTypes(), new Class<?>[] {GeneratorConfig.class}))
            .map(Method::getName)
            .collect(Collectors.toSet());
        List<String> missing = publicStaticFactories().stream()
            .filter(method -> method.getParameterCount() == 0)
            .filter(method -> !method.isAnnotationPresent(Deprecated.class))
            .map(Method::getName)
            .filter(name -> !withConfig.contains(name))
            .sorted()
            .toList();
        assertTrue(missing.isEmpty(), "missing ofX(GeneratorConfig) overloads: " + missing);
    }

    @Test
    @DisplayName("deprecated no-arg factories name a replacement that has a configuration overload")
    void deprecatedFactoriesPointToConfigurableReplacements() {
        Set<String> withConfig = publicStaticFactories().stream()
            .filter(method -> Arrays.equals(method.getParameterTypes(), new Class<?>[] {GeneratorConfig.class}))
            .filter(method -> !method.isAnnotationPresent(Deprecated.class))
            .map(Method::getName)
            .collect(Collectors.toSet());
        Map<String, String> replacements = Map.of(
            "ofURL", "ofUrlObject",
            "ofURI", "ofUriObject",
            "ofTimeZone", "ofTimeZoneObject");
        publicStaticFactories().stream()
            .filter(method -> method.getParameterCount() == 0)
            .filter(method -> method.isAnnotationPresent(Deprecated.class))
            .forEach(method -> {
                String replacement = replacements.get(method.getName());
                assertTrue(replacement != null, "unexpected deprecated no-arg factory " + method.getName());
                assertTrue(withConfig.contains(replacement), replacement + " lacks a configuration overload");
            });
    }

    @Test
    @DisplayName("every ofX(GeneratorConfig) overload is reproducible for the same seeded configuration")
    void configOverloadsAreReproducible() throws ReflectiveOperationException {
        List<String> irreproducible = new ArrayList<>();
        int checked = 0;
        for (Method method : publicStaticFactories()) {
            if (!Arrays.equals(method.getParameterTypes(), new Class<?>[] {GeneratorConfig.class})) {
                continue;
            }
            List<String> first = sample(method);
            List<String> second = sample(method);
            checked++;
            if (!first.equals(second)) {
                irreproducible.add(method.getName() + ": " + first + " vs " + second);
            }
        }
        assertTrue(checked > 150, "expected every configuration overload to be exercised, saw " + checked);
        assertEquals(List.of(), irreproducible);
    }

    @Test
    @DisplayName("parameterised configuration overloads reproduce their deprecated long-seed twins")
    @SuppressWarnings("deprecation")
    void parameterisedOverloadsMatchLegacySeeds() {
        long seed = 77L;
        GeneratorConfig config = GeneratorConfig.builder().seed(seed).build();
        List<String> items = List.of("a", "b", "c", "d", "e");
        assertEquals(Generators.ofByte((byte) 1, (byte) 90, seed).generateList(20),
                     Generators.ofByte((byte) 1, (byte) 90, config).generateList(20));
        assertEquals(Generators.ofShort((short) 1, (short) 900, seed).generateList(20),
                     Generators.ofShort((short) 1, (short) 900, config).generateList(20));
        assertEquals(Generators.ofInt(1, 9000, seed).generateList(20), Generators.ofInt(1, 9000, config).generateList(20));
        assertEquals(Generators.ofNaturalNumber(0, 500, seed).generateList(20),
                     Generators.ofNaturalNumber(0, 500, config).generateList(20));
        assertEquals(Generators.ofLong(1L, 90_000L, seed).generateList(20),
                     Generators.ofLong(1L, 90_000L, config).generateList(20));
        assertEquals(Generators.ofAtomicInteger(1, 50, seed).generateList(20).stream().map(Object::toString).toList(),
                     Generators.ofAtomicInteger(1, 50, config).generateList(20).stream().map(Object::toString).toList());
        assertEquals(Generators.ofAtomicLong(1L, 50L, seed).generateList(20).stream().map(Object::toString).toList(),
                     Generators.ofAtomicLong(1L, 50L, config).generateList(20).stream().map(Object::toString).toList());
        assertEquals(Generators.ofNormal(5.0, 2.0, seed).generateList(20), Generators.ofNormal(5.0, 2.0, config).generateList(20));
        assertEquals(Generators.ofPrime(2, 500, seed).generateList(20), Generators.ofPrime(2, 500, config).generateList(20));
        assertEquals(Generators.ofRegex("[A-F]{4}", seed).generateList(20),
                     Generators.ofRegex("[A-F]{4}", config).generateList(20));
        assertEquals(Generators.ofTemplate("##-??", seed).generateList(20),
                     Generators.ofTemplate("##-??", config).generateList(20));
        assertEquals(Generators.text(config).loremIpsum(io.github.frikit.krandom.generator.text.LoremIpsumGenerator.Mode.WORD)
                         .generateList(10),
                     Generators.ofLoremIpsum(io.github.frikit.krandom.generator.text.LoremIpsumGenerator.Mode.WORD, config)
                         .generateList(10));
        assertEquals(Generators.identifier(config).isbn(io.github.frikit.krandom.generator.identifier.IsbnGenerator.IsbnType.ISBN_10)
                         .generateList(10),
                     Generators.ofIsbn(io.github.frikit.krandom.generator.identifier.IsbnGenerator.IsbnType.ISBN_10, config)
                         .generateList(10));
        assertEquals(new io.github.frikit.krandom.generator.selection.PickGenerator<>(items, seed).generateList(20),
                     Generators.pick(items, config).generateList(20));
        assertEquals(new io.github.frikit.krandom.generator.selection.PickSetGenerator<>(items, 2, seed).generateList(10),
                     Generators.pickSet(items, 2, config).generateList(10));
        assertEquals(new io.github.frikit.krandom.generator.selection.FinitePoolGenerator<>(items, seed).generateList(5),
                     Generators.pool(items, config).generateList(5));
        assertEquals(new io.github.frikit.krandom.generator.selection.ShuffleGenerator<>(items, seed).generateList(5),
                     Generators.shuffle(items, config).generateList(5));
        assertEquals(new io.github.frikit.krandom.generator.selection.WeightedGenerator<>(items, List.of(1, 2, 3, 4, 5), seed)
                         .generateList(20),
                     Generators.weighted(items, List.of(1, 2, 3, 4, 5), config).generateList(20));
        assertTrue(new io.github.frikit.krandom.generator.base.NormalDistributionGenerator(0.0, 1.0, (Long) null)
                       .generateList(5).stream().allMatch(Double::isFinite));
    }

    private static List<String> sample(Method method) throws ReflectiveOperationException {
        GeneratorConfig config = GeneratorConfig.builder().seed(20260915L).clock(FIXED_CLOCK).build();
        Object created;
        try {
            created = method.invoke(null, config);
        } catch (InvocationTargetException ex) {
            return List.of("!" + ex.getCause().getClass().getName());
        }
        List<String> values = new ArrayList<>();
        if (!(created instanceof Generator<?> generator)) {
            values.add(created.getClass().getName());
            return values;
        }
        for (int i = 0; i < 5; i++) {
            try {
                values.add(render(generator.generate()));
            } catch (RuntimeException ex) {
                values.add("!" + ex.getClass().getName());
            }
        }
        return values;
    }

    private static String render(Object value) {
        if (value instanceof Calendar calendar) {
            return calendar.toInstant() + "@" + calendar.getTimeZone().getID();
        }
        if (value instanceof TimeZone zone) {
            return zone.getID();
        }
        if (value instanceof Object[] array) {
            return Arrays.deepToString(array);
        }
        return String.valueOf(value);
    }
}
