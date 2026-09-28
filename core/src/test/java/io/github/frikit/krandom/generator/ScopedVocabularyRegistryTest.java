/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.CommerceDataProvider;
import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataProvider;
import io.github.frikit.krandom.generator.database.DatabaseGenerator;
import io.github.frikit.krandom.generator.file.DirPathGenerator;
import io.github.frikit.krandom.generator.file.DirectoryNameDataProvider;
import io.github.frikit.krandom.generator.finance.BankAccountDataProvider;
import io.github.frikit.krandom.generator.finance.BankAccountGenerator;
import io.github.frikit.krandom.generator.finance.BankNameDataProvider;
import io.github.frikit.krandom.generator.finance.BankNameGenerator;
import io.github.frikit.krandom.generator.finance.BankTypeDataProvider;
import io.github.frikit.krandom.generator.finance.BankTypeGenerator;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.text.TextWordDataProvider;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataProvider;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataProvider;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameDataProvider;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataProvider;
import io.github.frikit.krandom.generator.user.EducationalAttainmentGenerator;
import io.github.frikit.krandom.generator.user.IndustryDataProvider;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobFieldDataProvider;
import io.github.frikit.krandom.generator.user.JobFieldGenerator;
import io.github.frikit.krandom.generator.user.JobTypeDataProvider;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.user.MaritalStatusDataProvider;
import io.github.frikit.krandom.generator.user.MaritalStatusGenerator;
import io.github.frikit.krandom.generator.user.PositionDataProvider;
import io.github.frikit.krandom.generator.user.PositionGenerator;
import io.github.frikit.krandom.generator.user.SeniorityDataProvider;
import io.github.frikit.krandom.generator.user.SeniorityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private record Concept(String name,
                           BiFunction<DataRegistryContext.Builder, Locale, DataRegistryContext.Builder> register,
                           Function<GeneratorConfig, Generator<String>> generator,
                           BiFunction<DataRegistryContext, Locale, Object> provider,
                           BiPredicate<DataRegistryContext, Locale> registered,
                           Function<DataRegistryContext, Set<String>> keys) {

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<Concept> concepts() {
        return Stream.of(
            new Concept("industry",
                        (b, l) -> b.registerIndustryProvider(stub(IndustryDataProvider.class, l)),
                        IndustryGenerator::new,
                        DataRegistryContext::industryProvider, DataRegistryContext::isIndustryRegistered,
                        DataRegistryContext::industryRegisteredKeys),
            new Concept("job field",
                        (b, l) -> b.registerJobFieldProvider(stub(JobFieldDataProvider.class, l)),
                        JobFieldGenerator::new,
                        DataRegistryContext::jobFieldProvider, DataRegistryContext::isJobFieldRegistered,
                        DataRegistryContext::jobFieldRegisteredKeys),
            new Concept("seniority",
                        (b, l) -> b.registerSeniorityProvider(stub(SeniorityDataProvider.class, l)),
                        SeniorityGenerator::new,
                        DataRegistryContext::seniorityProvider, DataRegistryContext::isSeniorityRegistered,
                        DataRegistryContext::seniorityRegisteredKeys),
            new Concept("position",
                        (b, l) -> b.registerPositionProvider(stub(PositionDataProvider.class, l)),
                        PositionGenerator::new,
                        DataRegistryContext::positionProvider, DataRegistryContext::isPositionRegistered,
                        DataRegistryContext::positionRegisteredKeys),
            new Concept("educational attainment",
                        (b, l) -> b.registerEducationalAttainmentProvider(
                            stub(EducationalAttainmentDataProvider.class, l)),
                        EducationalAttainmentGenerator::new,
                        DataRegistryContext::educationalAttainmentProvider,
                        DataRegistryContext::isEducationalAttainmentRegistered,
                        DataRegistryContext::educationalAttainmentRegisteredKeys),
            new Concept("marital status",
                        (b, l) -> b.registerMaritalStatusProvider(stub(MaritalStatusDataProvider.class, l)),
                        MaritalStatusGenerator::new,
                        DataRegistryContext::maritalStatusProvider, DataRegistryContext::isMaritalStatusRegistered,
                        DataRegistryContext::maritalStatusRegisteredKeys),
            new Concept("job type",
                        (b, l) -> b.registerJobTypeProvider(stub(JobTypeDataProvider.class, l)),
                        JobTypeGenerator::new,
                        DataRegistryContext::jobTypeProvider, DataRegistryContext::isJobTypeRegistered,
                        DataRegistryContext::jobTypeRegisteredKeys),
            new Concept("company name",
                        (b, l) -> b.registerCompanyNameProvider(stub(CompanyNameDataProvider.class, l)),
                        CompanyNameGenerator::new,
                        DataRegistryContext::companyNameProvider, DataRegistryContext::isCompanyNameRegistered,
                        DataRegistryContext::companyNameRegisteredKeys),
            new Concept("company buzzword",
                        (b, l) -> b.registerCompanyBuzzwordProvider(stub(CompanyBuzzwordDataProvider.class, l)),
                        CompanyBuzzwordGenerator::new,
                        DataRegistryContext::companyBuzzwordProvider,
                        DataRegistryContext::isCompanyBuzzwordRegistered,
                        DataRegistryContext::companyBuzzwordRegisteredKeys),
            new Concept("company catch phrase",
                        (b, l) -> b.registerCompanyCatchPhraseProvider(
                            stub(CompanyCatchPhraseDataProvider.class, l)),
                        CompanyCatchPhraseGenerator::new,
                        DataRegistryContext::companyCatchPhraseProvider,
                        DataRegistryContext::isCompanyCatchPhraseRegistered,
                        DataRegistryContext::companyCatchPhraseRegisteredKeys),
            new Concept("text word",
                        (b, l) -> b.registerTextWordProvider(stub(TextWordDataProvider.class, l)),
                        TextGenerator::new,
                        DataRegistryContext::textWordProvider, DataRegistryContext::isTextWordRegistered,
                        DataRegistryContext::textWordRegisteredKeys),
            new Concept("commerce",
                        (b, l) -> b.registerCommerceProvider(stub(CommerceDataProvider.class, l)),
                        CommerceGenerator::new,
                        DataRegistryContext::commerceProvider, DataRegistryContext::isCommerceRegistered,
                        DataRegistryContext::commerceRegisteredKeys),
            new Concept("database column",
                        (b, l) -> b.registerDatabaseColumnProvider(stub(DatabaseColumnDataProvider.class, l)),
                        DatabaseGenerator::new,
                        DataRegistryContext::databaseColumnProvider,
                        DataRegistryContext::isDatabaseColumnRegistered,
                        DataRegistryContext::databaseColumnRegisteredKeys),
            new Concept("directory name",
                        (b, l) -> b.registerDirectoryNameProvider(stub(DirectoryNameDataProvider.class, l)),
                        DirPathGenerator::new,
                        DataRegistryContext::directoryNameProvider, DataRegistryContext::isDirectoryNameRegistered,
                        DataRegistryContext::directoryNameRegisteredKeys),
            new Concept("bank name",
                        (b, l) -> b.registerBankNameProvider(stub(BankNameDataProvider.class, l)),
                        BankNameGenerator::new,
                        DataRegistryContext::bankNameProvider, DataRegistryContext::isBankNameRegistered,
                        DataRegistryContext::bankNameRegisteredKeys),
            new Concept("bank type",
                        (b, l) -> b.registerBankTypeProvider(stub(BankTypeDataProvider.class, l)),
                        BankTypeGenerator::new,
                        DataRegistryContext::bankTypeProvider, DataRegistryContext::isBankTypeRegistered,
                        DataRegistryContext::bankTypeRegisteredKeys),
            new Concept("bank account",
                        (b, l) -> b.registerBankAccountProvider(stub(BankAccountDataProvider.class, l)),
                        config -> new BankAccountGenerator(config)::generateAccountName,
                        DataRegistryContext::bankAccountProvider, DataRegistryContext::isBankAccountRegistered,
                        DataRegistryContext::bankAccountRegisteredKeys));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("concepts")
    @DisplayName("a scoped registration drives the generator and stays out of other contexts")
    void scopedRegistrationDrivesTheGenerator(Concept concept) {
        DataRegistryContext scoped = concept.register().apply(DataRegistryContext.builder().isolated(), Locale.US)
                                            .build();
        DataRegistryContext empty = DataRegistryContext.builder().isolated().build();

        String value = concept.generator().apply(
            GeneratorConfig.builder().seed(1L).locale(Locale.US).registryContext(scoped).build()).generate();

        assertTrue(value.toLowerCase(Locale.ROOT).contains(MARKER), value);
        assertTrue(concept.registered().test(scoped, Locale.US));
        assertEquals(Set.of("en", "en_US"), concept.keys().apply(scoped));
        assertNull(concept.provider().apply(empty, Locale.US));
        assertFalse(concept.registered().test(empty, Locale.US));
        assertTrue(concept.keys().apply(empty).isEmpty());
        assertFalse(concept.keys().apply(DataRegistryContext.globalDefault()).isEmpty());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("concepts")
    @DisplayName("an isolated context never reads the global registries")
    void isolatedContextIgnoresGlobalData(Concept concept) {
        GeneratorConfig global = GeneratorConfig.builder().seed(1L).locale(Locale.GERMANY).build();
        GeneratorConfig isolated = global.toBuilder()
                                         .registryContext(DataRegistryContext.builder().isolated().build())
                                         .build();

        assertNotEquals(samples(concept, global), samples(concept, isolated),
                        "an isolated context must use the bundled English data, not the global German data");
    }

    private static List<String> samples(Concept concept, GeneratorConfig config) {
        Generator<String> generator = concept.generator().apply(config);
        return Stream.generate(generator::generate).limit(20).toList();
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
