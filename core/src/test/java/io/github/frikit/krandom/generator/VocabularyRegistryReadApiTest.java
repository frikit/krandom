/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.CommerceDataRegistry;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataRegistry;
import io.github.frikit.krandom.generator.file.DirectoryNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankAccountDataRegistry;
import io.github.frikit.krandom.generator.finance.BankNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankTypeDataRegistry;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.text.TextWordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyNameDataRegistry;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataRegistry;
import io.github.frikit.krandom.generator.user.IndustryDataRegistry;
import io.github.frikit.krandom.generator.user.JobFieldDataRegistry;
import io.github.frikit.krandom.generator.user.JobTypeDataRegistry;
import io.github.frikit.krandom.generator.user.MaritalStatusDataRegistry;
import io.github.frikit.krandom.generator.user.PositionDataRegistry;
import io.github.frikit.krandom.generator.user.SeniorityDataRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Read-only contract of the vocabulary registries added by the locale-pattern migration: built-in
 * data is seeded at class load for every supported locale that ships all of the concept's files,
 * lookups fall back from {@code language_COUNTRY} to {@code language}, and unknown or {@code null}
 * locales resolve to nothing. English is intentionally served by each generator's bundled default.
 */
@DisplayName("Vocabulary registry read APIs")
class VocabularyRegistryReadApiTest {

    private record ReadApi(String name,
                           Function<Locale, Boolean> isRegistered,
                           Function<Locale, Object> forLocale,
                           Set<String> registeredKeys,
                           Function<Object, Locale> providerLocale) {

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<ReadApi> readApis() {
        return Stream.of(
            new ReadApi("industry", IndustryDataRegistry::isRegistered, IndustryDataRegistry::forLocale,
                        IndustryDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.IndustryDataProvider) p).getLocale()),
            new ReadApi("jobField", JobFieldDataRegistry::isRegistered, JobFieldDataRegistry::forLocale,
                        JobFieldDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.JobFieldDataProvider) p).getLocale()),
            new ReadApi("seniority", SeniorityDataRegistry::isRegistered, SeniorityDataRegistry::forLocale,
                        SeniorityDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.SeniorityDataProvider) p).getLocale()),
            new ReadApi("position", PositionDataRegistry::isRegistered, PositionDataRegistry::forLocale,
                        PositionDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.PositionDataProvider) p).getLocale()),
            new ReadApi("educationalAttainment", EducationalAttainmentDataRegistry::isRegistered,
                        EducationalAttainmentDataRegistry::forLocale, EducationalAttainmentDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.EducationalAttainmentDataProvider) p).getLocale()),
            new ReadApi("maritalStatus", MaritalStatusDataRegistry::isRegistered, MaritalStatusDataRegistry::forLocale,
                        MaritalStatusDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.MaritalStatusDataProvider) p).getLocale()),
            new ReadApi("jobType", JobTypeDataRegistry::isRegistered, JobTypeDataRegistry::forLocale,
                        JobTypeDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.JobTypeDataProvider) p).getLocale()),
            new ReadApi("companyName", CompanyNameDataRegistry::isRegistered, CompanyNameDataRegistry::forLocale,
                        CompanyNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.CompanyNameDataProvider) p).getLocale()),
            new ReadApi("companyBuzzword", CompanyBuzzwordDataRegistry::isRegistered,
                        CompanyBuzzwordDataRegistry::forLocale, CompanyBuzzwordDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.CompanyBuzzwordDataProvider) p).getLocale()),
            new ReadApi("companyCatchPhrase", CompanyCatchPhraseDataRegistry::isRegistered,
                        CompanyCatchPhraseDataRegistry::forLocale, CompanyCatchPhraseDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataProvider) p).getLocale()),
            new ReadApi("textWord", TextWordDataRegistry::isRegistered, TextWordDataRegistry::forLocale,
                        TextWordDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.text.TextWordDataProvider) p).getLocale()),
            new ReadApi("commerce", CommerceDataRegistry::isRegistered, CommerceDataRegistry::forLocale,
                        CommerceDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.commerce.CommerceDataProvider) p).getLocale()),
            new ReadApi("databaseColumn", DatabaseColumnDataRegistry::isRegistered, DatabaseColumnDataRegistry::forLocale,
                        DatabaseColumnDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.database.DatabaseColumnDataProvider) p).getLocale()),
            new ReadApi("directoryName", DirectoryNameDataRegistry::isRegistered, DirectoryNameDataRegistry::forLocale,
                        DirectoryNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.file.DirectoryNameDataProvider) p).getLocale()),
            new ReadApi("bankName", BankNameDataRegistry::isRegistered, BankNameDataRegistry::forLocale,
                        BankNameDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.finance.BankNameDataProvider) p).getLocale()),
            new ReadApi("bankType", BankTypeDataRegistry::isRegistered, BankTypeDataRegistry::forLocale,
                        BankTypeDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.finance.BankTypeDataProvider) p).getLocale()),
            new ReadApi("bankAccount", BankAccountDataRegistry::isRegistered, BankAccountDataRegistry::forLocale,
                        BankAccountDataRegistry.registeredKeys(),
                        p -> ((io.github.frikit.krandom.generator.finance.BankAccountDataProvider) p).getLocale()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("seeded locales resolve exactly and through language and fallback-tier variants")
    void seededLocalesResolve(ReadApi api) {
        Locale german = Locale.GERMANY;
        assertTrue(api.isRegistered().apply(german), api.name());
        Object provider = api.forLocale().apply(german);
        assertNotNull(provider, api.name());
        assertEquals(german, api.providerLocale().apply(provider), api.name());

        assertTrue(api.isRegistered().apply(Locale.GERMAN), api.name());
        assertSame(provider, api.forLocale().apply(Locale.GERMAN), api.name());
        assertTrue(api.isRegistered().apply(Locale.of("de", "LU")), api.name());
        assertSame(provider, api.forLocale().apply(Locale.of("de", "LU")), api.name());

        Object swiss = api.forLocale().apply(SupportedLocale.DE_CH.locale());
        assertNotNull(swiss, api.name());
        assertEquals(SupportedLocale.DE_CH.locale(), api.providerLocale().apply(swiss), api.name());
        assertTrue(api.registeredKeys().containsAll(Set.of("de", "de_DE", "de_CH")), api.name());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("English, null and unknown locales resolve to nothing")
    void unregisteredLocalesResolveToNothing(ReadApi api) {
        for (Locale locale : new Locale[] {Locale.US, Locale.ENGLISH, Locale.of("xx", "YY"), Locale.of("is")}) {
            assertFalse(api.isRegistered().apply(locale), api.name() + " " + locale);
            assertNull(api.forLocale().apply(locale), api.name() + " " + locale);
        }
        assertFalse(api.isRegistered().apply(null), api.name());
        assertNull(api.forLocale().apply(null), api.name());
        assertFalse(api.registeredKeys().contains("en"), api.name());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("readApis")
    @DisplayName("key snapshots are immutable")
    void registeredKeysAreImmutable(ReadApi api) {
        assertThrows(UnsupportedOperationException.class, () -> api.registeredKeys().add("xx"));
    }
}
