/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.database.DatabaseGenerator;
import io.github.frikit.krandom.generator.file.DirPathGenerator;
import io.github.frikit.krandom.generator.finance.BankAccountGenerator;
import io.github.frikit.krandom.generator.finance.BankNameGenerator;
import io.github.frikit.krandom.generator.finance.BankTypeGenerator;
import io.github.frikit.krandom.generator.location.AddressPurposeGenerator;
import io.github.frikit.krandom.generator.location.AddressTypeGenerator;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.EducationalAttainmentGenerator;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobFieldGenerator;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.user.MaritalStatusGenerator;
import io.github.frikit.krandom.generator.user.PositionGenerator;
import io.github.frikit.krandom.generator.user.SeniorityGenerator;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Every locale-aware vocabulary generator resolves its data through the configuration's
 * {@link DataRegistryContext}, so scoped registrations apply and isolated contexts never read the
 * global registries.
 */
class ScopedVocabularyRegistryTest {

    private static final String MARKER = "zzscoped";

    /** Placeholders of every composed vocabulary format; each generator replaces its own. */
    private static final String FORMAT =
            "{prefix} {noun} {name} {suffix} {verb} {adjective} {tagline} {material} {product} {color}";

    private record Concept(Vocabulary<?> vocabulary, Function<GeneratorConfig, Generator<String>> generator) {

        @Override
        public String toString() {
            return vocabulary.name();
        }
    }

    static Stream<Concept> concepts() {
        return Stream.of(
                new Concept(Vocabulary.INDUSTRY, IndustryGenerator::new),
                new Concept(Vocabulary.JOB_FIELD, JobFieldGenerator::new),
                new Concept(Vocabulary.SENIORITY, SeniorityGenerator::new),
                new Concept(Vocabulary.POSITION, PositionGenerator::new),
                new Concept(Vocabulary.EDUCATIONAL_ATTAINMENT, EducationalAttainmentGenerator::new),
                new Concept(Vocabulary.MARITAL_STATUS, MaritalStatusGenerator::new),
                new Concept(Vocabulary.ADDRESS_TYPE, AddressTypeGenerator::new),
                new Concept(Vocabulary.ADDRESS_PURPOSE, AddressPurposeGenerator::new),
                new Concept(Vocabulary.JOB_TYPE, JobTypeGenerator::new),
                new Concept(Vocabulary.COMPANY_NAME, CompanyNameGenerator::new),
                new Concept(Vocabulary.COMPANY_BUZZWORD, CompanyBuzzwordGenerator::new),
                new Concept(Vocabulary.COMPANY_CATCH_PHRASE, CompanyCatchPhraseGenerator::new),
                new Concept(Vocabulary.TEXT_WORD, TextGenerator::new),
                new Concept(Vocabulary.COMMERCE, CommerceGenerator::new),
                new Concept(Vocabulary.DATABASE_COLUMN, DatabaseGenerator::new),
                new Concept(Vocabulary.DIRECTORY_NAME, DirPathGenerator::new),
                new Concept(Vocabulary.BANK_NAME, BankNameGenerator::new),
                new Concept(Vocabulary.BANK_TYPE, BankTypeGenerator::new),
                new Concept(Vocabulary.BANK_ACCOUNT, config -> new BankAccountGenerator(config)::generateAccountName));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("concepts")
    @DisplayName("a scoped registration drives the generator and stays out of other contexts")
    void scopedRegistrationDrivesTheGenerator(Concept concept) {
        Vocabulary<?> vocabulary = concept.vocabulary();
        DataRegistryContext scoped = registerStub(DataRegistryContext.builder().isolated(), vocabulary, Locale.US)
                .build();
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();

        String value = concept.generator()
                .apply(GeneratorConfig.builder()
                        .seed(1L)
                        .locale(Locale.US)
                        .registryContext(scoped)
                        .build())
                .generate();

        assertTrue(value.toLowerCase(Locale.ROOT).contains(MARKER), value);
        assertTrue(scoped.isRegistered(vocabulary, Locale.US));
        assertEquals(Set.of("en", "en_US"), scoped.registeredKeys(vocabulary));
        assertNull(empty.provider(vocabulary, Locale.US));
        assertFalse(empty.isRegistered(vocabulary, Locale.US));
        assertTrue(empty.registeredKeys(vocabulary).isEmpty());
        assertFalse(
                DataRegistryContext.globalDefault().registeredKeys(vocabulary).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("concepts")
    @DisplayName("an isolated context never reads the global registries")
    void isolatedContextIgnoresGlobalData(Concept concept) {
        GeneratorConfig global =
                GeneratorConfig.builder().seed(1L).locale(Locale.GERMANY).build();
        GeneratorConfig isolated = global.toBuilder()
                .registryContext(DataRegistryContext.builder().isolated().build())
                .build();

        assertNotEquals(
                samples(concept, global),
                samples(concept, isolated),
                "an isolated context must use the bundled English data, not the global German data");
    }

    private static List<String> samples(Concept concept, GeneratorConfig config) {
        Generator<String> generator = concept.generator().apply(config);
        return Stream.generate(generator::generate).limit(20).toList();
    }

    private static <P extends LocaleDataProvider> DataRegistryContext.Builder registerStub(
            DataRegistryContext.Builder builder, Vocabulary<P> vocabulary, Locale locale) {
        return builder.register(vocabulary, stub(vocabulary.providerType(), locale));
    }

    /** A provider whose lists hold only {@link #MARKER} and whose formats use every placeholder. */
    private static <T> T stub(Class<T> type, Locale locale) {
        Object stub = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
            if (method.getName().equals("getLocale")) {
                return locale;
            }
            if (method.getReturnType() == List.class) {
                return List.of(MARKER);
            }
            if (method.getReturnType() == String.class && method.getDeclaringClass() == type) {
                return FORMAT;
            }
            return switch (method.getName()) {
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> type.getSimpleName() + "(" + MARKER + ")";
            };
        });
        return type.cast(stub);
    }
}
