/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import jakarta.validation.constraints.Email;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Sweeps every generator factory, so that a new generator is covered without a test of its own: a
 * seeded value must not depend on the JVM's default locale or time zone. One seed cannot reach every
 * branch, so {@link EnvironmentDeterminismTest} keeps its targeted cases.
 */
@Isolated
@DisplayName("Seeded output of the whole generator catalog")
class GeneratorCatalogEnvironmentTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2025-06-30T12:00:00Z"), ZoneOffset.UTC);
    private static final List<Locale> GENERATION_LOCALES = Stream.of(
                    "en-US", "en-GB", "de-DE", "tr-TR", "ar-SA", "hi-IN")
            .map(Locale::forLanguageTag)
            .toList();
    private static final int DRAWS = 3;
    private static final String REFUSED = "refused: ";
    private static final List<Locale> LOCALES_WITHOUT_MIDDLE_NAMES =
            Stream.of("tr-TR", "ar-SA", "hi-IN").map(Locale::forLanguageTag).toList();

    private static final List<Method> FACTORIES = Arrays.stream(Generators.class.getMethods())
            .filter(method -> Modifier.isStatic(method.getModifiers()))
            .filter(method -> method.getParameterCount() == 1 && method.getParameterTypes()[0] == GeneratorConfig.class)
            .filter(method -> Generator.class.isAssignableFrom(method.getReturnType()))
            .sorted(Comparator.comparing(Method::getName))
            .toList();

    private Locale originalLocale;
    private TimeZone originalTimeZone;

    @BeforeEach
    void rememberDefaults() {
        originalLocale = Locale.getDefault();
        originalTimeZone = TimeZone.getDefault();
    }

    @AfterEach
    void restoreDefaults() {
        Locale.setDefault(originalLocale);
        TimeZone.setDefault(originalTimeZone);
    }

    @ParameterizedTest(name = "default locale {0}")
    @ValueSource(strings = {"tr-TR", "ar-SA", "hi-IN", "th-TH", "ja-JP-u-ca-japanese", "de-DE"})
    @DisplayName("does not depend on the JVM's default locale")
    void doesNotDependOnTheJvmDefaultLocale(String languageTag) throws Exception {
        Map<String, String> expected = sweepUnderNeutralDefaults();

        Locale.setDefault(Locale.forLanguageTag(languageTag));

        assertEquals(List.of(), differences(expected, sweep()));
    }

    @ParameterizedTest(name = "default time zone {0}")
    @ValueSource(strings = {"Pacific/Kiritimati", "Etc/GMT+12", "America/New_York"})
    @DisplayName("does not depend on the JVM's default time zone")
    void doesNotDependOnTheJvmDefaultTimeZone(String zoneId) throws Exception {
        Map<String, String> expected = sweepUnderNeutralDefaults();

        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));

        assertEquals(List.of(), differences(expected, sweep()));
    }

    @Test
    @DisplayName("covers the catalog, and only the known factories refuse")
    void coversTheCatalogAndOnlyTheKnownFactoriesRefuse() throws Exception {
        Map<String, String> outcomes = sweepUnderNeutralDefaults();

        Set<String> refusals = new TreeSet<>();
        outcomes.forEach((key, outcome) -> {
            if (outcome.startsWith(REFUSED)) {
                refusals.add(key.replaceFirst("\\[\\d+]$", "") + " " + outcome.substring(REFUSED.length()));
            }
        });

        assertTrue(FACTORIES.size() > 150, "only " + FACTORIES.size() + " factories found");
        assertEquals(expectedRefusals(), refusals);
    }

    /** What the configuration or the bundled data rules out: nothing else may refuse. */
    private static Set<String> expectedRefusals() {
        Set<String> refusals = new TreeSet<>();
        for (Locale locale : GENERATION_LOCALES) {
            refusals.add(locale + "/ofEmail.generateFreeEmail IllegalStateException");
            refusals.add(locale + "/ofEmail.generateFreeEmailDomain IllegalStateException");
            refusals.add(locale + "/ofUniversity.factory UnsupportedOperationException");
        }
        for (Locale locale : LOCALES_WITHOUT_MIDDLE_NAMES) {
            refusals.add(locale + "/ofFullName.generateWithMiddleInitial UnsupportedOperationException");
            refusals.add(locale + "/ofFullName.generateWithMiddleName UnsupportedOperationException");
            refusals.add(locale + "/ofMiddleName.factory UnsupportedOperationException");
        }
        return refusals;
    }

    private Map<String, String> sweepUnderNeutralDefaults() throws Exception {
        Locale.setDefault(Locale.ROOT);
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        return sweep();
    }

    private static Map<String, String> sweep() throws Exception {
        Map<String, String> outcomes = new TreeMap<>();
        for (Locale locale : GENERATION_LOCALES) {
            for (Method factory : FACTORIES) {
                sweepFactory(outcomes, locale, factory);
            }
            Object shape =
                    Generators.ofObject(FixtureShape.class, config(locale)).generate();
            outcomes.put(locale + "/object.generate[0]", describe(shape));
        }
        return outcomes;
    }

    private static void sweepFactory(Map<String, String> outcomes, Locale locale, Method factory) throws Exception {
        String prefix = locale + "/" + factory.getName() + ".";
        Object generator;
        try {
            generator = factory.invoke(null, config(locale));
        } catch (InvocationTargetException refused) {
            outcomes.put(
                    prefix + "factory", REFUSED + refused.getCause().getClass().getSimpleName());
            return;
        }
        for (Method method : generateMethods(generator)) {
            method.setAccessible(true);
            for (int draw = 0; draw < DRAWS; draw++) {
                outcomes.put(prefix + method.getName() + "[" + draw + "]", outcome(method, generator));
            }
        }
    }

    private static List<Method> generateMethods(Object generator) {
        return Arrays.stream(generator.getClass().getMethods())
                .filter(method -> method.getName().startsWith("generate"))
                .filter(method -> method.getParameterCount() == 0 && !Modifier.isStatic(method.getModifiers()))
                .filter(method -> !method.isBridge())
                .sorted(Comparator.comparing(Method::getName))
                .toList();
    }

    private static String outcome(Method method, Object generator) throws IllegalAccessException {
        try {
            return describe(method.invoke(generator));
        } catch (InvocationTargetException refused) {
            return REFUSED + refused.getCause().getClass().getSimpleName();
        }
    }

    private static GeneratorConfig config(Locale locale) {
        return GeneratorConfig.builder()
                .seed(42L)
                .locale(locale)
                .clock(CLOCK)
                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .identityDocumentSafetyPolicy(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .businessTaxIdentifierSafetyPolicy(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .cryptoAddressSafetyPolicy(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .securitiesIdentifierSafetyPolicy(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                .build();
    }

    /** A value's own content: a date, for example, without the zone its {@code toString} prints it in. */
    private static String describe(@Nullable Object value) {
        if (value instanceof java.sql.Date date) {
            return date.toLocalDate().toString();
        }
        if (value instanceof java.sql.Time time) {
            return time.toLocalTime().toString();
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().toString();
        }
        if (value instanceof Date date) {
            return Long.toString(date.getTime());
        }
        if (value instanceof Calendar calendar) {
            return calendar.getClass().getSimpleName() + ":" + calendar.getTimeInMillis() + ":"
                    + calendar.getTimeZone().getID();
        }
        if (value instanceof Object[] array) {
            return Arrays.deepToString(array);
        }
        if (value != null && value.getClass().isArray()) {
            return Arrays.deepToString(new Object[] {value});
        }
        return String.valueOf(value);
    }

    private static List<String> differences(Map<String, String> expected, Map<String, String> actual) {
        List<String> differences = new ArrayList<>();
        expected.forEach((key, value) -> {
            if (!value.equals(actual.get(key))) {
                differences.add(key + ": " + value + " -> " + actual.get(key));
            }
        });
        return differences.stream().limit(20).toList();
    }

    record FixtureShape(
            String firstName,
            String lastName,
            @Email String email,
            String companyName,
            String companyEmail,
            UUID id,
            int age,
            BigDecimal amount,
            LocalDate birthDate,
            Instant createdAt,
            List<String> tags,
            Set<String> labels,
            Map<String, Integer> scores) {}
}
