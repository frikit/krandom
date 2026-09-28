/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.commerce.CommerceDataProvider;
import io.github.frikit.krandom.generator.commerce.CommerceDataRegistry;
import io.github.frikit.krandom.generator.commerce.CommerceGenerator;
import io.github.frikit.krandom.generator.database.DatabaseColumnDataRegistry;
import io.github.frikit.krandom.generator.database.DatabaseGenerator;
import io.github.frikit.krandom.generator.file.DirPathGenerator;
import io.github.frikit.krandom.generator.file.DirectoryNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankAccountDataProvider;
import io.github.frikit.krandom.generator.finance.BankAccountDataRegistry;
import io.github.frikit.krandom.generator.finance.BankAccountGenerator;
import io.github.frikit.krandom.generator.finance.BankNameDataRegistry;
import io.github.frikit.krandom.generator.finance.BankNameGenerator;
import io.github.frikit.krandom.generator.finance.BankTypeDataRegistry;
import io.github.frikit.krandom.generator.finance.BankTypeGenerator;
import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.text.TextGenerator;
import io.github.frikit.krandom.generator.text.TextWordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataProvider;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyBuzzwordGenerator;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataProvider;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyCatchPhraseGenerator;
import io.github.frikit.krandom.generator.user.CompanyNameDataProvider;
import io.github.frikit.krandom.generator.user.CompanyNameDataRegistry;
import io.github.frikit.krandom.generator.user.CompanyNameGenerator;
import io.github.frikit.krandom.generator.user.EducationalAttainmentDataRegistry;
import io.github.frikit.krandom.generator.user.EducationalAttainmentGenerator;
import io.github.frikit.krandom.generator.user.IndustryDataRegistry;
import io.github.frikit.krandom.generator.user.IndustryGenerator;
import io.github.frikit.krandom.generator.user.JobFieldDataRegistry;
import io.github.frikit.krandom.generator.user.JobFieldGenerator;
import io.github.frikit.krandom.generator.user.JobTypeDataRegistry;
import io.github.frikit.krandom.generator.user.JobTypeGenerator;
import io.github.frikit.krandom.generator.user.MaritalStatusDataRegistry;
import io.github.frikit.krandom.generator.user.MaritalStatusGenerator;
import io.github.frikit.krandom.generator.user.PositionDataRegistry;
import io.github.frikit.krandom.generator.user.PositionGenerator;
import io.github.frikit.krandom.generator.user.SeniorityDataRegistry;
import io.github.frikit.krandom.generator.user.SeniorityGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Vocabulary generators follow the locale-aware data pattern: every non-English native locale has
 * real translations resolved through a {@code XDataRegistry}, English keeps the exact seeded
 * output of the former hard-coded lists, and unsupported locales fall back to English.
 */
@DisplayName("Locale-aware vocabulary generators")
class LocalizedVocabularyTest {

    private static final long SEED = 20260927L;

    // ── English lists exactly as they were hard-coded before the migration ──────────────────

    private static final List<String> INDUSTRIES = List.of(
        "Technology", "Healthcare", "Finance", "Education", "Retail", "Manufacturing",
        "Telecommunications", "Energy", "Transportation", "Construction", "Real Estate",
        "Hospitality", "Media", "Entertainment", "Agriculture", "Aerospace", "Automotive",
        "Biotechnology", "Consulting", "Cybersecurity");
    private static final List<String> JOB_FIELDS = List.of(
        "Engineering", "Marketing", "Sales", "Finance", "Operations", "Product",
        "Human Resources", "Legal", "Design", "Customer Support", "Research",
        "Data Science", "Security", "Procurement", "Logistics");
    private static final List<String> SENIORITIES = List.of(
        "Intern", "Junior", "Associate", "Mid-level", "Senior", "Lead",
        "Principal", "Staff", "Director", "Vice President");
    private static final List<String> POSITIONS = List.of(
        "Software Engineer", "Product Manager", "Data Analyst", "UX Designer",
        "Account Executive", "Marketing Specialist", "Business Analyst",
        "DevOps Engineer", "Site Reliability Engineer", "Engineering Manager",
        "Solutions Architect", "Technical Writer", "QA Engineer", "Project Manager",
        "Security Engineer", "Database Administrator");
    private static final List<String> EDUCATION = List.of(
        "High School", "Associate Degree", "Bachelor's Degree", "Master's Degree",
        "Doctorate", "Professional Degree", "Vocational Certificate");
    private static final List<String> MARITAL = List.of(
        "Single", "Married", "Divorced", "Widowed", "Separated", "Domestic Partnership");
    private static final List<String> JOB_TYPES = List.of(
        "Full-time", "Part-time", "Contract", "Temporary", "Internship", "Freelance",
        "Apprenticeship", "Seasonal", "Volunteer", "Remote");
    private static final List<String> COMPANY_PREFIXES = List.of(
        "Global", "National", "American", "United", "Premier", "Advanced",
        "Strategic", "Dynamic", "Innovative", "Professional", "Digital", "Next",
        "Alpha", "Summit", "Apex");
    private static final List<String> COMPANY_NOUNS = List.of(
        "Solutions", "Systems", "Technologies", "Industries", "Services",
        "Group", "Partners", "Ventures", "Associates", "Consulting",
        "Networks", "Dynamics", "Resources", "Management", "Analytics");
    private static final List<String> COMPANY_SUFFIXES = List.of(
        "Inc.", "LLC", "Corp.", "Ltd.", "Co.", "Group", "Holdings", "Enterprises");
    private static final List<String> BUZZ_VERBS = List.of(
        "streamline", "empower", "leverage", "optimize", "synergize", "scale", "deliver", "enable");
    private static final List<String> BUZZ_ADJECTIVES = List.of(
        "cross-platform", "end-to-end", "best-in-class", "frictionless", "cloud-native", "data-driven");
    private static final List<String> BUZZ_NOUNS = List.of(
        "solutions", "workflows", "infrastructure", "platforms", "experiences", "capabilities");
    private static final List<String> CATCH_ADJECTIVES = List.of(
        "Adaptive", "Unified", "Trusted", "Intelligent", "Future-ready", "Effortless", "Secure");
    private static final List<String> CATCH_NOUNS = List.of(
        "Platform", "Network", "Experience", "Engine", "Ecosystem", "Suite", "Framework");
    private static final List<String> CATCH_TAGLINES = List.of(
        "for modern teams", "for digital growth", "for global scale", "for measurable impact");
    private static final List<String> TEXT_WORDS = List.of(
        "alpha", "beta", "gamma", "delta", "vector", "signal", "stream", "token",
        "cloud", "matrix", "engine", "system", "future", "global", "local", "secure");
    private static final List<String> COMMERCE_ADJECTIVES = List.of(
        "Small", "Sleek", "Rustic", "Practical", "Premium", "Ergonomic");
    private static final List<String> COMMERCE_MATERIALS = List.of(
        "Steel", "Wooden", "Concrete", "Plastic", "Granite", "Cotton");
    private static final List<String> COMMERCE_PRODUCTS = List.of(
        "Chair", "Table", "Computer", "Keyboard", "Shoes", "Watch");
    private static final List<String> COMMERCE_DEPARTMENTS = List.of(
        "Books", "Electronics", "Outdoors", "Home", "Toys", "Garden");
    private static final List<String> COMMERCE_COLORS = List.of(
        "red", "blue", "green", "black", "white", "silver");
    private static final List<String> DB_COLUMNS = List.of(
        "id", "created_at", "updated_at", "user_id", "order_id", "status", "name", "description",
        "amount", "metadata");
    private static final List<String> DIRECTORIES = List.of(
        "home", "users", "projects", "data", "logs", "tmp", "docs", "assets");
    private static final List<String> BANK_NAMES = List.of(
        "First National Bank", "Global Trust Bank", "Pioneer Credit Union", "Summit Financial", "Civic Savings");
    private static final List<String> BANK_TYPES = List.of(
        "Retail Bank", "Commercial Bank", "Investment Bank", "Credit Union", "Online Bank");
    private static final List<String> ACCOUNT_NAMES = List.of(
        "Checking Account", "Savings Account", "Business Account", "Joint Account", "Payroll Account");
    private static final List<String> TRANSACTION_TYPES = List.of(
        "deposit", "withdrawal", "payment", "transfer", "refund", "fee", "interest", "chargeback");

    /**
     * Legacy German employment types; the migration must keep non-English legacy tables verbatim.
     */
    private static final List<String> JOB_TYPES_DE = List.of(
        "Vollzeit", "Teilzeit", "Vertrag", "Befristet", "Praktikum",
        "Freelance", "Ausbildung", "Saisonal", "Ehrenamt", "Remote");

    private static GeneratorConfig seeded() {
        return GeneratorConfig.builder().seed(SEED).build();
    }

    private static GeneratorConfig seeded(Locale locale) {
        return GeneratorConfig.builder().seed(SEED).locale(locale).build();
    }

    private static List<String> legacyDraws(List<String> values, int count) {
        Random random = new Random(SEED);
        List<String> draws = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            draws.add(values.get(random.nextInt(values.size())));
        }
        return draws;
    }

    private static List<String> draw(Supplier<String> supplier, int count) {
        List<String> values = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            values.add(supplier.get());
        }
        return values;
    }

    /**
     * Non-English native locales whose display vocabularies (industries, job fields, seniorities,
     * positions, education, marital statuses, job types, company names, buzzwords, catch phrases,
     * text words, commerce and banking vocabulary) must ship complete translations: every native
     * {@link SupportedLocale} except English, which is served by the bundled defaults.
     */
    static final List<SupportedLocale> TRANSLATED_LOCALES = allNonEnglishNativeLocales().toList();

    static Stream<SupportedLocale> nonEnglishNativeLocales() {
        return TRANSLATED_LOCALES.stream();
    }

    /**
     * Every non-English native locale (tier 1 and tier 2 of {@link SupportedLocale}).
     */
    static Stream<SupportedLocale> allNonEnglishNativeLocales() {
        return Arrays.stream(SupportedLocale.values())
            .filter(value -> !value.resourceDataTier().isFallback())
            .filter(value -> !"en".equals(value.locale().getLanguage()));
    }

    @Test
    @DisplayName("display vocabularies are registered exactly for the translated locales")
    void translatedLocalesAreRegistered() {
        List<Function<Locale, Boolean>> registries = List.of(
            IndustryDataRegistry::isRegistered, JobFieldDataRegistry::isRegistered, SeniorityDataRegistry::isRegistered,
            PositionDataRegistry::isRegistered, EducationalAttainmentDataRegistry::isRegistered,
            MaritalStatusDataRegistry::isRegistered, JobTypeDataRegistry::isRegistered,
            CompanyNameDataRegistry::isRegistered, CompanyBuzzwordDataRegistry::isRegistered,
            CompanyCatchPhraseDataRegistry::isRegistered, TextWordDataRegistry::isRegistered,
            CommerceDataRegistry::isRegistered, BankNameDataRegistry::isRegistered, BankTypeDataRegistry::isRegistered,
            BankAccountDataRegistry::isRegistered);
        allNonEnglishNativeLocales().forEach(supportedLocale -> {
            boolean translated = TRANSLATED_LOCALES.contains(supportedLocale);
            for (int i = 0; i < registries.size(); i++) {
                assertEquals(translated, registries.get(i).apply(supportedLocale.locale()),
                             "registry #" + i + " for " + supportedLocale);
            }
        });
    }

    // ── English output is unchanged ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("English seeded output is unchanged")
    class EnglishOutputUnchanged {

        @Test
        @DisplayName("single-list vocabularies draw exactly as the former hard-coded arrays")
        void singleLists() {
            assertEquals(legacyDraws(INDUSTRIES, 60), new IndustryGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(JOB_FIELDS, 60), new JobFieldGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(SENIORITIES, 60), new SeniorityGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(POSITIONS, 60), new PositionGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(EDUCATION, 60), new EducationalAttainmentGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(MARITAL, 60), new MaritalStatusGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(JOB_TYPES, 60), new JobTypeGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(BANK_NAMES, 60), new BankNameGenerator(seeded()).generateList(60));
            assertEquals(legacyDraws(BANK_TYPES, 60), new BankTypeGenerator(seeded()).generateList(60));
            DatabaseGenerator database = new DatabaseGenerator(seeded());
            assertEquals(legacyDraws(DB_COLUMNS, 60), draw(database::generateColumn, 60));
        }

        @Test
        @DisplayName("English locales other than en_US also keep the English lists")
        void otherEnglishLocales() {
            for (Locale locale : List.of(Locale.UK, Locale.of("en", "AU"), Locale.CANADA, Locale.ENGLISH)) {
                assertEquals(legacyDraws(INDUSTRIES, 30), new IndustryGenerator(seeded(locale)).generateList(30));
                assertEquals(legacyDraws(MARITAL, 30), new MaritalStatusGenerator(seeded(locale)).generateList(30));
            }
        }

        @Test
        @DisplayName("company names compose prefix, noun and suffix exactly as before")
        void companyNames() {
            Random random = new Random(SEED);
            List<String> expected = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                String name = COMPANY_PREFIXES.get(random.nextInt(COMPANY_PREFIXES.size())) + " "
                              + COMPANY_NOUNS.get(random.nextInt(COMPANY_NOUNS.size()));
                expected.add(i % 2 == 0
                             ? name + " " + COMPANY_SUFFIXES.get(random.nextInt(COMPANY_SUFFIXES.size()))
                             : name);
            }
            CompanyNameGenerator generator = new CompanyNameGenerator(seeded());
            List<String> actual = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                actual.add(generator.generate(i % 2 == 0));
            }
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("buzzwords and catch phrases compose exactly as before")
        void phrases() {
            Random buzz = new Random(SEED);
            Random catchy = new Random(SEED);
            List<String> expectedBuzz = new ArrayList<>();
            List<String> expectedCatch = new ArrayList<>();
            for (int i = 0; i < 30; i++) {
                expectedBuzz.add(BUZZ_VERBS.get(buzz.nextInt(BUZZ_VERBS.size())) + " "
                                 + BUZZ_ADJECTIVES.get(buzz.nextInt(BUZZ_ADJECTIVES.size())) + " "
                                 + BUZZ_NOUNS.get(buzz.nextInt(BUZZ_NOUNS.size())));
                expectedCatch.add(CATCH_ADJECTIVES.get(catchy.nextInt(CATCH_ADJECTIVES.size())) + " "
                                  + CATCH_NOUNS.get(catchy.nextInt(CATCH_NOUNS.size())) + " "
                                  + CATCH_TAGLINES.get(catchy.nextInt(CATCH_TAGLINES.size())));
            }
            assertEquals(expectedBuzz, new CompanyBuzzwordGenerator(seeded()).generateList(30));
            assertEquals(expectedCatch, new CompanyCatchPhraseGenerator(seeded()).generateList(30));
        }

        @Test
        @DisplayName("commerce names, descriptions and departments compose exactly as before")
        void commerce() {
            Random random = new Random(SEED);
            List<String> expected = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                expected.add(COMMERCE_ADJECTIVES.get(random.nextInt(6)) + " " + COMMERCE_MATERIALS.get(random.nextInt(6))
                             + " " + COMMERCE_PRODUCTS.get(random.nextInt(6)));
                expected.add("A " + COMMERCE_ADJECTIVES.get(random.nextInt(6)) + " " + COMMERCE_PRODUCTS.get(random.nextInt(6))
                             + " in " + COMMERCE_COLORS.get(random.nextInt(6)) + ".");
                expected.add(COMMERCE_DEPARTMENTS.get(random.nextInt(6)));
            }
            CommerceGenerator generator = new CommerceGenerator(seeded());
            List<String> actual = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                actual.add(generator.generateProductName());
                actual.add(generator.generateProductDescription());
                actual.add(generator.generateDepartment());
            }
            assertEquals(expected, actual);
        }

        @Test
        @DisplayName("text blocks, directory paths and bank-account vocabulary draw exactly as before")
        void textPathsAndAccounts() {
            Random random = new Random(SEED);
            List<String> expectedText = new ArrayList<>();
            for (int block = 0; block < 10; block++) {
                int words = 4 + random.nextInt(10);
                StringBuilder text = new StringBuilder();
                for (int i = 0; i < words; i++) {
                    if (!text.isEmpty()) {
                        text.append(' ');
                    }
                    text.append(TEXT_WORDS.get(random.nextInt(TEXT_WORDS.size())));
                }
                expectedText.add(text.append('.').toString());
            }
            assertEquals(expectedText, new TextGenerator(seeded()).generateList(10));

            Random pathRandom = new Random(SEED);
            List<String> expectedPaths = new ArrayList<>();
            for (int path = 0; path < 10; path++) {
                int depth = pathRandom.nextInt(2, 5);
                StringBuilder value = new StringBuilder("/");
                for (int i = 0; i < depth; i++) {
                    if (i > 0) {
                        value.append('/');
                    }
                    value.append(DIRECTORIES.get(pathRandom.nextInt(DIRECTORIES.size())));
                }
                expectedPaths.add(value.toString());
            }
            assertEquals(expectedPaths, new DirPathGenerator(seeded()).generateList(10));

            Random accountRandom = new Random(SEED);
            List<String> expectedAccounts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                expectedAccounts.add(ACCOUNT_NAMES.get(accountRandom.nextInt(ACCOUNT_NAMES.size())));
                expectedAccounts.add(TRANSACTION_TYPES.get(accountRandom.nextInt(TRANSACTION_TYPES.size())));
            }
            BankAccountGenerator accounts = new BankAccountGenerator(seeded());
            List<String> actualAccounts = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                actualAccounts.add(accounts.generateAccountName());
                actualAccounts.add(accounts.generateTransactionType());
            }
            assertEquals(expectedAccounts, actualAccounts);
        }

        @Test
        @DisplayName("legacy non-English tables are migrated verbatim")
        void legacyTablesKept() {
            assertEquals(legacyDraws(JOB_TYPES_DE, 40), new JobTypeGenerator(seeded(Locale.GERMANY)).generateList(40));
            assertEquals(legacyDraws(JOB_TYPES_DE, 40), new JobTypeGenerator(seeded(Locale.of("de", "AT"))).generateList(40));
        }
    }

    // ── Every non-English native locale has real localized data ─────────────────────────────

    private record SingleList(String name,
                              Function<Locale, List<String>> registryValues,
                              Function<GeneratorConfig, Generator<String>> generator,
                              Function<Locale, Boolean> registered) {

        @Override
        public String toString() {
            return name;
        }
    }

    static Stream<SingleList> singleLists() {
        return Stream.of(
            new SingleList("industries", locale -> IndustryDataRegistry.forLocale(locale).getIndustries(),
                           IndustryGenerator::new, null),
            new SingleList("jobFields", locale -> JobFieldDataRegistry.forLocale(locale).getJobFields(),
                           JobFieldGenerator::new, null),
            new SingleList("seniorities", locale -> SeniorityDataRegistry.forLocale(locale).getSeniorities(),
                           SeniorityGenerator::new, null),
            new SingleList("positions", locale -> PositionDataRegistry.forLocale(locale).getPositions(),
                           PositionGenerator::new, null),
            new SingleList("educationalAttainments",
                           locale -> EducationalAttainmentDataRegistry.forLocale(locale).getEducationalAttainments(),
                           EducationalAttainmentGenerator::new, null),
            new SingleList("maritalStatuses", locale -> MaritalStatusDataRegistry.forLocale(locale).getMaritalStatuses(),
                           MaritalStatusGenerator::new, null),
            new SingleList("jobTypes", locale -> JobTypeDataRegistry.forLocale(locale).getJobTypes(),
                           JobTypeGenerator::new, null),
            new SingleList("bankNames", locale -> BankNameDataRegistry.forLocale(locale).getBankNames(),
                           BankNameGenerator::new, null),
            new SingleList("bankTypes", locale -> BankTypeDataRegistry.forLocale(locale).getBankTypes(),
                           BankTypeGenerator::new, null),
            // Identifier vocabulary: English column names are the realistic default, so only the
            // legacy localized tables ship; every shipped locale must still be used by the generator.
            new SingleList("databaseColumns", locale -> DatabaseColumnDataRegistry.forLocale(locale).getColumns(),
                           config -> new DatabaseGenerator(config)::generateColumn, DatabaseColumnDataRegistry::isRegistered));
    }

    static Stream<Object[]> singleListsByLocale() {
        return singleLists().flatMap(list -> (list.registered() == null
                                              ? nonEnglishNativeLocales()
                                              : allNonEnglishNativeLocales()
                                                  .filter(locale -> list.registered().apply(locale.locale())))
            .map(locale -> new Object[] {list, locale}));
    }

    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("singleListsByLocale")
    @DisplayName("single-list generators draw only from the locale's own data")
    void singleListGeneratorsUseLocaleData(SingleList list, SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        List<String> localized = list.registryValues().apply(locale);
        assertFalse(localized.isEmpty(), list + " " + locale);
        Generator<String> generator = list.generator().apply(seeded(locale));
        for (String value : generator.generateList(40)) {
            assertTrue(localized.contains(value), list + " " + locale + " produced " + value);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonEnglishNativeLocales")
    @DisplayName("composite generators use the locale's vocabulary and formats")
    void compositeGeneratorsUseLocaleData(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();

        CompanyNameDataProvider names = CompanyNameDataRegistry.forLocale(locale);
        CompanyNameGenerator companies = new CompanyNameGenerator(seeded(locale));
        for (int i = 0; i < 20; i++) {
            String suffix = companies.generateSuffix();
            assertTrue(names.getSuffixes().contains(suffix), locale + " suffix " + suffix);
            String name = companies.generate(false);
            assertTrue(names.getNouns().stream().anyMatch(name::contains), locale + " name " + name);
            String legal = companies.generate();
            assertTrue(names.getSuffixes().stream().anyMatch(legal::contains), locale + " legal name " + legal);
        }

        CompanyBuzzwordDataProvider buzz = CompanyBuzzwordDataRegistry.forLocale(locale);
        for (String phrase : new CompanyBuzzwordGenerator(seeded(locale)).generateList(20)) {
            assertTrue(buzz.getNouns().stream().anyMatch(phrase::contains), locale + " buzzword " + phrase);
        }
        CompanyCatchPhraseDataProvider catchy = CompanyCatchPhraseDataRegistry.forLocale(locale);
        for (String phrase : new CompanyCatchPhraseGenerator(seeded(locale)).generateList(20)) {
            assertTrue(catchy.getTaglines().stream().anyMatch(phrase::contains), locale + " catch phrase " + phrase);
        }

        CommerceDataProvider commerce = CommerceDataRegistry.forLocale(locale);
        CommerceGenerator shop = new CommerceGenerator(seeded(locale));
        for (int i = 0; i < 20; i++) {
            String product = shop.generateProductName();
            assertTrue(commerce.getProducts().stream().anyMatch(product::contains), locale + " product " + product);
            String description = shop.generateProductDescription();
            assertTrue(commerce.getColors().stream().anyMatch(description::contains), locale + " description " + description);
            assertTrue(commerce.getDepartments().contains(shop.generateDepartment()), locale + " department");
            assertTrue(commerce.getMaterials().contains(shop.generateMaterial()), locale + " material");
            assertTrue(commerce.getAdjectives().contains(shop.generateAdjective()), locale + " adjective");
            assertTrue(commerce.getColors().contains(shop.generateColor()), locale + " color");
            assertTrue(commerce.getProducts().contains(shop.generateProduct()), locale + " product noun");
        }

        BankAccountDataProvider accounts = BankAccountDataRegistry.forLocale(locale);
        BankAccountGenerator bank = new BankAccountGenerator(seeded(locale));
        for (int i = 0; i < 20; i++) {
            assertTrue(accounts.getAccountNames().contains(bank.generateAccountName()), locale + " account name");
            assertTrue(accounts.getTransactionTypes().contains(bank.generateTransactionType()), locale + " transaction");
        }

        List<String> words = TextWordDataRegistry.forLocale(locale).getWords();
        for (String block : new TextGenerator(seeded(locale)).generateList(10)) {
            assertTrue(words.stream().anyMatch(block::contains), locale + " text " + block);
        }
    }

    static Stream<SupportedLocale> directoryLocales() {
        return allNonEnglishNativeLocales().filter(locale -> DirectoryNameDataRegistry.isRegistered(locale.locale()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directoryLocales")
    @DisplayName("directory paths use the shipped localized directory names")
    void directoryPathsUseLocaleData(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        List<String> directories = DirectoryNameDataRegistry.forLocale(locale).getDirectoryNames();
        for (String path : new DirPathGenerator(seeded(locale)).generateList(10)) {
            for (String segment : path.substring(1).split("/")) {
                assertTrue(directories.contains(segment), locale + " path " + path);
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonEnglishNativeLocales")
    @DisplayName("localized data is translated, unique, parallel to English and in the locale's script")
    void datasetsAreTranslated(SupportedLocale supportedLocale) {
        Locale locale = supportedLocale.locale();
        assertTranslated(locale, "industries", IndustryDataRegistry.forLocale(locale).getIndustries(), INDUSTRIES, true);
        assertTranslated(locale, "jobFields", JobFieldDataRegistry.forLocale(locale).getJobFields(), JOB_FIELDS, true);
        assertTranslated(locale, "seniorities", SeniorityDataRegistry.forLocale(locale).getSeniorities(), SENIORITIES, true);
        assertTranslated(locale, "positions", PositionDataRegistry.forLocale(locale).getPositions(), POSITIONS, true);
        assertTranslated(locale, "educationalAttainments",
                         EducationalAttainmentDataRegistry.forLocale(locale).getEducationalAttainments(), EDUCATION, true);
        assertTranslated(locale, "maritalStatuses", MaritalStatusDataRegistry.forLocale(locale).getMaritalStatuses(),
                         MARITAL, true);
        assertTranslated(locale, "jobTypes", JobTypeDataRegistry.forLocale(locale).getJobTypes(), JOB_TYPES, true);
        CompanyNameDataProvider names = CompanyNameDataRegistry.forLocale(locale);
        assertTranslated(locale, "companyPrefixes", names.getPrefixes(), COMPANY_PREFIXES, true);
        assertTranslated(locale, "companyNouns", names.getNouns(), COMPANY_NOUNS, true);
        assertTranslated(locale, "companySuffixes", names.getSuffixes(), COMPANY_SUFFIXES, false);
        assertTrue(names.getNameFormat().contains("{prefix}") && names.getNameFormat().contains("{noun}"), locale.toString());
        assertTrue(names.getLegalNameFormat().contains("{name}") && names.getLegalNameFormat().contains("{suffix}"),
                   locale.toString());
        assertTranslated(locale, "bankNames", BankNameDataRegistry.forLocale(locale).getBankNames(), BANK_NAMES, true);
        assertTranslated(locale, "bankTypes", BankTypeDataRegistry.forLocale(locale).getBankTypes(), BANK_TYPES, true);
        CommerceDataProvider commerce = CommerceDataRegistry.forLocale(locale);
        assertTranslated(locale, "commerceProducts", commerce.getProducts(), COMMERCE_PRODUCTS, true);
        assertTranslated(locale, "commerceDepartments", commerce.getDepartments(), COMMERCE_DEPARTMENTS, true);
        assertFalse(commerce.getProductDescriptionFormat().startsWith("A {adjective}"),
                    locale + " product descriptions must not use the English sentence template");
    }

    private static void assertTranslated(Locale locale, String name, List<String> values, List<String> english,
                                         boolean parallel) {
        String label = locale + " " + name;
        assertFalse(values.isEmpty(), label);
        assertEquals(values.size(), new HashSet<>(values).size(), label + " has duplicates");
        if (parallel) {
            assertEquals(english.size(), values.size(), label + " is not parallel to English");
        }
        long untranslated = values.stream().filter(english::contains).count();
        assertTrue(untranslated <= values.size() / 3, label + " looks untranslated: " + values);
        Set<Character.UnicodeScript> scripts = expectedScripts(locale);
        long native_ = values.stream().filter(value -> value.codePoints().filter(Character::isLetter)
            .mapToObj(Character.UnicodeScript::of).anyMatch(scripts::contains)).count();
        assertTrue(native_ >= Math.ceil(values.size() * 0.9), label + " script coverage " + native_ + "/" + values.size());
    }

    private static Set<Character.UnicodeScript> expectedScripts(Locale locale) {
        return switch (locale.getLanguage()) {
            case "ar" -> Set.of(Character.UnicodeScript.ARABIC);
            case "hi" -> Set.of(Character.UnicodeScript.DEVANAGARI);
            case "ja" -> Set.of(Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA,
                                Character.UnicodeScript.KATAKANA);
            case "ko" -> Set.of(Character.UnicodeScript.HANGUL);
            case "ru", "uk", "bg" -> Set.of(Character.UnicodeScript.CYRILLIC);
            case "zh" -> Set.of(Character.UnicodeScript.HAN);
            case "el" -> Set.of(Character.UnicodeScript.GREEK);
            case "th" -> Set.of(Character.UnicodeScript.THAI);
            case "he" -> Set.of(Character.UnicodeScript.HEBREW);
            default -> Set.of(Character.UnicodeScript.LATIN);
        };
    }

    // ── Specific regressions from the review ─────────────────────────────────────────────────

    @Nested
    @DisplayName("review regressions")
    class ReviewRegressions {

        @Test
        @DisplayName("ofCompanyName(Locale) no longer ignores the locale")
        void companyNameHonoursLocale() {
            CompanyNameDataProvider german = CompanyNameDataRegistry.forLocale(Locale.GERMANY);
            for (int i = 0; i < 30; i++) {
                String name = Generators.ofCompanyName(Locale.GERMANY).generate();
                assertTrue(german.getSuffixes().stream().anyMatch(name::endsWith), name);
                assertFalse(name.contains("American") || name.endsWith("Inc."), name);
            }
        }

        @Test
        @DisplayName("Japanese and Chinese text blocks use real Japanese and Chinese words")
        void cjkTextUsesRealWords() {
            for (Locale locale : List.of(Locale.JAPAN, Locale.CHINA)) {
                for (String block : new TextGenerator(seeded(locale)).generateList(20)) {
                    assertTrue(block.chars().noneMatch(ch -> (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')),
                               locale + " text still contains Latin words: " + block);
                }
            }
            assertTrue(TextWordDataRegistry.forLocale(Locale.JAPAN).getWords().contains("データ"));
            assertTrue(TextWordDataRegistry.forLocale(Locale.CHINA).getWords().contains("数据"));
        }

        @Test
        @DisplayName("unsupported locales fall back to English vocabulary")
        void unsupportedLocaleFallsBack() {
            Locale icelandic = Locale.of("is", "IS");
            assertNull(IndustryDataRegistry.forLocale(icelandic));
            assertEquals(legacyDraws(INDUSTRIES, 20), new IndustryGenerator(seeded(icelandic)).generateList(20));
            assertTrue(COMPANY_SUFFIXES.contains(new CompanyNameGenerator(seeded(icelandic)).generateSuffix()));
        }

        @Test
        @DisplayName("fallback-tier locales resolve to their native dataset")
        void fallbackTierLocalesResolve() {
            assertEquals(IndustryDataRegistry.forLocale(Locale.GERMANY).getIndustries(),
                         IndustryDataRegistry.forLocale(Locale.of("de", "CH")).getIndustries());
            assertEquals(CommerceDataRegistry.forLocale(Locale.FRANCE).getProducts(),
                         CommerceDataRegistry.forLocale(Locale.CANADA_FRENCH).getProducts());
            assertTrue(IndustryDataRegistry.isRegistered(Locale.of("pt", "PT")));
        }

        @Test
        @DisplayName("locale constructors reject null")
        void localeConstructorsRejectNull() {
            Locale none = null;
            List<Runnable> constructors = List.of(
                () -> new IndustryGenerator(none), () -> new JobFieldGenerator(none), () -> new SeniorityGenerator(none),
                () -> new PositionGenerator(none), () -> new EducationalAttainmentGenerator(none),
                () -> new MaritalStatusGenerator(none), () -> new CompanyNameGenerator(none));
            for (Runnable constructor : constructors) {
                assertThrows(NullPointerException.class, constructor::run);
            }
        }
    }

    // ── Facade overloads ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("facade exposes Locale overloads for every migrated vocabulary generator")
    void facadeLocaleOverloads() {
        Locale german = Locale.GERMANY;
        List<Supplier<? extends Generator<?>>> generators = List.of(
            () -> Generators.ofIndustry(german), () -> Generators.ofJobField(german), () -> Generators.ofSeniority(german),
            () -> Generators.ofPosition(german), () -> Generators.ofEducationalAttainment(german),
            () -> Generators.ofMaritalStatus(german), () -> Generators.ofJobType(german), () -> Generators.ofCompanyName(german),
            () -> Generators.ofCompanyBuzzword(german), () -> Generators.ofCompanyCatchPhrase(german),
            () -> Generators.ofText(german), () -> Generators.ofCommerce(german), () -> Generators.ofDatabase(german),
            () -> Generators.ofDirPath(german), () -> Generators.ofBankName(german), () -> Generators.ofBankType(german));
        for (Supplier<? extends Generator<?>> supplier : generators) {
            Object value = Objects.requireNonNull(supplier.get().generate());
            assertNotNull(value.toString());
        }
        assertTrue(IndustryDataRegistry.forLocale(german).getIndustries().contains(Generators.ofIndustry(german).generate()));
        assertTrue(BankNameDataRegistry.forLocale(german).getBankNames().contains(Generators.ofBankName(german).generate()));
    }
}
