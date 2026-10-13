/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UniversityGenerator")
class UniversityGeneratorTest {

    private static final UniversityData FIRST =
            new UniversityData("Northbridge University", "BSc", "School of", "University", "Northbridge");
    private static final UniversityData SECOND =
            new UniversityData("Riverdale Institute", "MSc", "Institute of", "Institute", "Riverdale");

    @Test
    @DisplayName("generates complete local fixture data and exposes every component")
    void generatesCompleteLocalFixtureDataAndExposesEveryComponent() {
        GeneratorConfig config = configuration(Locale.US, 123L);
        UniversityGenerator generator = new UniversityGenerator(config);

        assertTrue(Set.of(FIRST, SECOND).contains(generator.generate()));
        assertTrue(Set.of(FIRST.name(), SECOND.name()).contains(generator.name()));
        assertTrue(Set.of(FIRST.degree(), SECOND.degree()).contains(generator.degree()));
        assertTrue(Set.of(FIRST.prefix(), SECOND.prefix()).contains(generator.prefix()));
        assertTrue(Set.of(FIRST.suffix(), SECOND.suffix()).contains(generator.suffix()));
        assertTrue(Set.of(FIRST.place(), SECOND.place()).contains(generator.place()));
        assertEquals(2, generator.getUniversityCount());
        assertEquals(Locale.US, generator.getLocale());
        assertTrue(generator.isLocaleExplicitlySupported());
        assertEquals(
                new UniversityGenerator(config).generate(),
                Generators.ofUniversity(config).generate());
        assertEquals(
                new UniversityGenerator(config).generate(),
                Generators.person(config).university().generate());
    }

    @Test
    @DisplayName("requires a configured local provider and validates registrations")
    void requiresAConfiguredLocalProviderAndValidatesRegistrations() {
        GeneratorConfig missing = GeneratorConfig.builder()
                .locale(Locale.US)
                .registryContext(DataRegistryContext.builder().isolated().build())
                .build();

        assertThrows(NullPointerException.class, () -> new UniversityGenerator(null));
        assertThrows(UnsupportedOperationException.class, () -> new UniversityGenerator(missing));
        assertFalse(DataRegistryContext.globalDefault().isRegistered(Vocabulary.UNIVERSITY, Locale.US));
        assertTrue(DataRegistryContext.globalDefault()
                .registeredKeys(Vocabulary.UNIVERSITY)
                .isEmpty());
        assertThrows(
                NullPointerException.class, () -> DataRegistryContext.builder().register(Vocabulary.UNIVERSITY, null));
        assertThrows(
                NullPointerException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.UNIVERSITY, provider(null, new UniversityData[] {FIRST})));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.UNIVERSITY, provider(Locale.US, new UniversityData[0])));
        assertThrows(
                IllegalArgumentException.class,
                () -> DataRegistryContext.builder()
                        .register(Vocabulary.UNIVERSITY, provider(Locale.US, new UniversityData[] {null})));
    }

    @Test
    @DisplayName("uses exact locale data before language fallback")
    void usesExactLocaleDataBeforeLanguageFallback() {
        UniversityDataProvider english = provider(Locale.ENGLISH, new UniversityData[] {FIRST});
        UniversityDataProvider american = provider(Locale.US, new UniversityData[] {SECOND});
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.UNIVERSITY, english)
                .register(Vocabulary.UNIVERSITY, american)
                .build();

        assertEquals(american, context.provider(Vocabulary.UNIVERSITY, Locale.US));
        assertEquals(english, context.provider(Vocabulary.UNIVERSITY, Locale.UK));
        assertEquals(Set.of("en", "en_US"), context.registeredKeys(Vocabulary.UNIVERSITY));
    }

    private static GeneratorConfig configuration(Locale locale, long seed) {
        return GeneratorConfig.builder()
                .locale(locale)
                .seed(seed)
                .registryContext(DataRegistryContext.builder()
                        .isolated()
                        .register(Vocabulary.UNIVERSITY, provider(locale, new UniversityData[] {FIRST, SECOND}))
                        .build())
                .build();
    }

    private static UniversityDataProvider provider(Locale locale, UniversityData[] data) {
        return new UniversityDataProvider() {
            @Override
            public Locale getLocale() {
                return locale;
            }

            @Override
            public UniversityData[] getUniversities() {
                return data;
            }
        };
    }
}
