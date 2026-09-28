/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.user.AgeType;
import io.github.frikit.krandom.generator.user.GenderGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Facade names must not differ only by letter case while returning different types, and the
 * facade must expose the person helpers that the fluent namespaces already offer.
 */
@DisplayName("Generators facade naming")
class GeneratorsNamingTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).clock(FIXED_CLOCK).build();
    }

    private static <T> List<String> render(Generator<T> generator, Function<T, String> renderer, int count) {
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(renderer.apply(generator.generate()));
        }
        return values;
    }

    @Nested
    @DisplayName("value-object factories")
    class ValueObjectFactories {

        @Test
        @DisplayName("ofUrlObject returns java.net.URL values and honours configuration")
        void urlObject() {
            URL url = Generators.ofUrlObject().generate();
            assertEquals("https", url.getProtocol());
            assertEquals(render(Generators.ofUrlObject(seeded(5L)), URL::toString, 10),
                         render(Generators.ofUrlObject(seeded(5L)), URL::toString, 10));
        }

        @Test
        @DisplayName("ofUriObject returns java.net.URI values and honours configuration")
        void uriObject() {
            assertNotNull(Generators.ofUriObject().generate().getScheme());
            assertEquals(render(Generators.ofUriObject(seeded(6L)), URI::toString, 10),
                         render(Generators.ofUriObject(seeded(6L)), URI::toString, 10));
        }

        @Test
        @DisplayName("ofTimeZoneObject returns legacy TimeZone values and honours configuration")
        void timeZoneObject() {
            assertInstanceOf(TimeZone.class, Generators.ofTimeZoneObject().generate());
            assertEquals(render(Generators.ofTimeZoneObject(seeded(7L)), TimeZone::getID, 10),
                         render(Generators.ofTimeZoneObject(seeded(7L)), TimeZone::getID, 10));
        }

        @Test
        @DisplayName("forType resolves URL and URI through the value-object factories")
        void forTypeStillResolves() {
            assertInstanceOf(URL.class, Generators.forType(URL.class).generate());
            assertInstanceOf(URI.class, Generators.forType(URI.class).generate());
        }

        @Test
        @DisplayName("date-time namespace exposes the same legacy time-zone generator as the facade")
        void namespaceTimeZoneObject() {
            assertEquals(render(Generators.ofTimeZoneObject(seeded(3L)), TimeZone::getID, 5),
                         render(Generators.datetime(seeded(3L)).timeZoneObject(), TimeZone::getID, 5));
        }
    }

    @Nested
    @DisplayName("case-insensitive uniqueness")
    class CaseInsensitiveUniqueness {

        @Test
        @DisplayName("facade methods never differ only by letter case")
        void facadeHasNoCaseTwins() {
            assertNoCaseTwins(Generators.class, true);
        }

        @Test
        @DisplayName("namespace methods never differ only by letter case")
        void namespacesHaveNoCaseTwins() {
            for (Class<?> namespace : FacadeNamespaceParityTest.namespaceTypes()) {
                assertNoCaseTwins(namespace, false);
            }
        }

        private void assertNoCaseTwins(Class<?> type, boolean staticMethods) {
            Map<String, TreeSet<String>> byLowerCase = Arrays.stream(type.getMethods())
                .filter(method -> method.getDeclaringClass() == type)
                .filter(method -> Modifier.isStatic(method.getModifiers()) == staticMethods)
                .map(Method::getName)
                .collect(Collectors.groupingBy(name -> name.toLowerCase(Locale.ROOT), TreeMap::new,
                                               Collectors.toCollection(TreeSet::new)));
            byLowerCase.forEach((lower, names) ->
                assertEquals(1, names.size(), type.getSimpleName() + " has case twins " + names));
        }
    }

    @Nested
    @DisplayName("person helpers exposed by the namespaces")
    class PersonHelpers {

        @Test
        @DisplayName("ofFirstName default, locale and config forms")
        void firstName() {
            assertFalse(Generators.ofFirstName().generate().isBlank());
            assertEquals(Generators.person(seeded(1L).toBuilder().locale(Locale.GERMANY).build()).firstName().generateList(10),
                         Generators.ofFirstName(seeded(1L).toBuilder().locale(Locale.GERMANY).build()).generateList(10));
            assertEquals(Locale.JAPAN, Generators.ofFirstName(Locale.JAPAN).getLocale());
        }

        @Test
        @DisplayName("ofLastName default, locale and config forms")
        void lastName() {
            assertFalse(Generators.ofLastName().generate().isBlank());
            assertEquals(Generators.person(seeded(2L)).lastName().generateList(10),
                         Generators.ofLastName(seeded(2L)).generateList(10));
            assertEquals(Locale.FRANCE, Generators.ofLastName(Locale.FRANCE).getLocale());
        }

        @Test
        @DisplayName("ofGender default, locale and config forms")
        void gender() {
            assertFalse(Generators.ofGender().generate().isBlank());
            assertEquals(new GenderGenerator(Locale.GERMANY).getMaleLabel(),
                         Generators.ofGender(Locale.GERMANY).getMaleLabel());
            assertEquals(Generators.person(seeded(3L)).gender().generateList(10),
                         Generators.ofGender(seeded(3L)).generateList(10));
        }

        @Test
        @DisplayName("ofAge default, type and config forms")
        void age() {
            assertTrue(Generators.ofAge().generate() >= 0);
            assertEquals(Generators.person(seeded(4L)).age().generateList(10),
                         Generators.ofAge(seeded(4L)).generateList(10));
            assertEquals(Generators.person(seeded(4L)).age(AgeType.SENIOR).generateList(10),
                         Generators.ofAge(AgeType.SENIOR, seeded(4L)).generateList(10));
            int senior = Generators.ofAge(AgeType.SENIOR).generate();
            assertTrue(senior >= 65, "senior age " + senior);
        }

        @Test
        @DisplayName("ofBirthday default, locale and config forms")
        void birthday() {
            assertInstanceOf(LocalDate.class, Generators.ofBirthday().generate());
            assertEquals(Generators.person(seeded(5L)).birthday().generateList(10),
                         Generators.ofBirthday(seeded(5L)).generateList(10));
            assertInstanceOf(LocalDate.class, Generators.ofBirthday(Locale.GERMANY).generate());
        }

        @Test
        @DisplayName("ofCoordinates default, locale and config forms")
        void coordinates() {
            assertNotNull(Generators.ofCoordinates().generate());
            assertEquals(render(Generators.location(seeded(6L)).coordinates(), String::valueOf, 5),
                         render(Generators.ofCoordinates(seeded(6L)), String::valueOf, 5));
            assertNotNull(Generators.ofCoordinates(Locale.JAPAN).generate());
        }
    }
}
