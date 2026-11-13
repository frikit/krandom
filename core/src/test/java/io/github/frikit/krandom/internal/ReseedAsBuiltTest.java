/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.DataRegistryContext;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Seedable;
import io.github.frikit.krandom.generator.Vocabulary;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.location.AddressInfoGenerator;
import io.github.frikit.krandom.generator.location.CityGenerator;
import io.github.frikit.krandom.generator.location.CountryGenerator;
import io.github.frikit.krandom.generator.location.PhoneNumberGenerator;
import io.github.frikit.krandom.generator.location.PostalCodeGenerator;
import io.github.frikit.krandom.generator.location.StateGenerator;
import io.github.frikit.krandom.generator.location.StreetAddressDataProvider;
import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.text.ParagraphGenerator;
import io.github.frikit.krandom.generator.text.SentenceGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.LastNameGenerator;
import io.github.frikit.krandom.generator.user.UsernameGenerator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The oracle of the reseed route: a generator built from a configuration with one seed, used, and then
 * reseeded, gives in every public method without arguments what a new build from the configuration with the
 * new seed gives.
 */
@DisplayName("a reseeded generator gives what a new build with that seed gives")
class ReseedAsBuiltTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static final Map<Class<?>, Function<GeneratorConfig, Object>> FACTORIES = factories();

    private static Map<Class<?>, Function<GeneratorConfig, Object>> factories() {
        Map<Class<?>, Function<GeneratorConfig, Object>> factories = new LinkedHashMap<>();
        factories.put(UUIDGenerator.class, UUIDGenerator::new);
        factories.put(FullNameGenerator.class, FullNameGenerator::new);
        factories.put(FirstNameGenerator.class, FirstNameGenerator::new);
        factories.put(LastNameGenerator.class, LastNameGenerator::new);
        factories.put(EmailGenerator.class, EmailGenerator::new);
        factories.put(PhoneNumberGenerator.class, PhoneNumberGenerator::new);
        factories.put(CurrencyGenerator.class, CurrencyGenerator::new);
        factories.put(MoneyGenerator.class, MoneyGenerator::new);
        factories.put(StreetAddressGenerator.class, StreetAddressGenerator::new);
        factories.put(CityGenerator.class, CityGenerator::new);
        factories.put(StateGenerator.class, StateGenerator::new);
        factories.put(PostalCodeGenerator.class, PostalCodeGenerator::new);
        factories.put(CountryGenerator.class, CountryGenerator::new);
        factories.put(DateGenerator.class, DateGenerator::new);
        factories.put(SentenceGenerator.class, SentenceGenerator::new);
        factories.put(WordGenerator.class, WordGenerator::new);
        factories.put(UsernameGenerator.class, UsernameGenerator::new);
        factories.put(AddressInfoGenerator.class, AddressInfoGenerator::new);
        factories.put(ParagraphGenerator.class, ParagraphGenerator::new);
        return factories;
    }

    @Test
    @DisplayName("a class registers its reseed once, and a class without one has none")
    void aClassRegistersItsReseedOnce() {
        UUIDGenerator registered =
                new UUIDGenerator(GeneratorConfig.builder().seed(1L).build());

        IllegalStateException error = assertThrows(
                IllegalStateException.class, () -> Reseeding.register(UUIDGenerator.class, (generator, seed) -> {}));

        assertEquals("A reseed is registered for " + UUIDGenerator.class.getName() + " already", error.getMessage());
        assertNotNull(Reseeding.reseederOf(registered));
        assertNull(Reseeding.reseederOf("not a generator"));
    }

    @Test
    @DisplayName("every class that a Schema reseeds is checked here, and none of them became Seedable")
    void everyReseededClassIsChecked() {
        GeneratorConfig config = GeneratorConfig.builder().seed(1L).clock(CLOCK).build();
        // Building every schema built-in initializes its class, which registers any reseed it has.
        for (ProviderDescriptor<?> descriptor : ProviderCatalog.schemaBuiltIns()) {
            descriptor.create(config);
        }
        assertTrue(FACTORIES.keySet().containsAll(Reseeding.registered()), "unchecked: " + Reseeding.registered());
        for (Map.Entry<Class<?>, Function<GeneratorConfig, Object>> factory : FACTORIES.entrySet()) {
            assertTrue(
                    Reseeding.reseederOf(factory.getValue().apply(config)) != null,
                    factory.getKey().getName());
            // Object generation reseeds a Seedable @Randomizer, so a class must not become Seedable to be here;
            // DateGenerator was Seedable before.
            assertEquals(
                    factory.getKey() == DateGenerator.class,
                    Seedable.class.isAssignableFrom(factory.getKey()),
                    factory.getKey().getName());
        }
    }

    @Test
    @DisplayName("each public method without arguments, and calls that reach the caches, give after a reseed what a"
            + " new build gives")
    void reseededGivesWhatANewBuildGives() throws ReflectiveOperationException {
        List<GeneratorConfig> configs = new ArrayList<>();
        // Spanish without a country, and so without postal places, reaches the country streams, which draw a
        // random country only there.
        for (Locale locale : List.of(
                Locale.US, Locale.GERMANY, Locale.JAPAN, Locale.forLanguageTag("pt-BR"), Locale.forLanguageTag("es"))) {
            for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
                configs.add(GeneratorConfig.builder()
                        .locale(locale)
                        .clock(CLOCK)
                        .childStreamPolicy(policy)
                        .seed(1L)
                        .build());
            }
            // Advancing convenience streams keep a child, which a reseed must drop.
            configs.add(GeneratorConfig.builder()
                    .locale(locale)
                    .clock(CLOCK)
                    .advancingConvenienceStreams(true)
                    .seed(1L)
                    .build());
        }
        int compared = 0;
        for (Map.Entry<Class<?>, Function<GeneratorConfig, Object>> factory : FACTORIES.entrySet()) {
            List<Method> methods = noArgumentMethods(factory.getKey());
            for (GeneratorConfig config : configs) {
                for (long seed : new long[] {0L, 7L, -3L, Long.MAX_VALUE}) {
                    Object fresh;
                    try {
                        fresh = factory.getValue()
                                .apply(config.toBuilder().seed(seed).build());
                    } catch (UnsupportedOperationException unsupportedLocale) {
                        continue;
                    }
                    Object reused = factory.getValue()
                            .apply(config.toBuilder().seed(seed + 101).build());
                    // Use it first, so that whatever it keeps (caches, lazy children, issued values) is filled.
                    calls(reused, methods);
                    java.util.Objects.requireNonNull(Reseeding.reseederOf(reused))
                            .reseed(reused, seed);
                    assertEquals(
                            calls(fresh, methods),
                            calls(reused, methods),
                            factory.getKey().getSimpleName() + " " + config.getLocale() + " "
                                    + config.getChildStreamPolicy() + " seed " + seed);
                    // Reseeded with the same seed after giving those values, it must give them again: what it
                    // kept from them (issued unique values, say) must not count against the new build.
                    Object again = factory.getValue()
                            .apply(config.toBuilder().seed(seed).build());
                    java.util.Objects.requireNonNull(Reseeding.reseederOf(reused))
                            .reseed(reused, seed);
                    assertEquals(
                            calls(again, methods),
                            calls(reused, methods),
                            factory.getKey().getSimpleName() + " " + config.getLocale() + " "
                                    + config.getChildStreamPolicy() + " seed " + seed + ", reseeded twice");
                    compared++;
                }
            }
        }
        assertTrue(compared > 100, "compared " + compared);
    }

    @Test
    @DisplayName("a generator built without a seed, as a @Randomizer is, gives after a reseed what a new build with"
            + " that seed gives")
    void unseededBuildReseedsAsBuilt() throws ReflectiveOperationException {
        int compared = 0;
        for (Map.Entry<Class<?>, Function<GeneratorConfig, Object>> factory : FACTORIES.entrySet()) {
            List<Method> methods = noArgumentMethods(factory.getKey());
            for (Locale locale : List.of(Locale.US, Locale.GERMANY, Locale.JAPAN)) {
                for (boolean advancing : new boolean[] {false, true}) {
                    // A fixed clock, as time-based values (UUIDv7, say) read it; a reseed leaves the clock alone.
                    GeneratorConfig unseeded = GeneratorConfig.builder()
                            .locale(locale)
                            .clock(CLOCK)
                            .advancingConvenienceStreams(advancing)
                            .build();
                    for (long seed : new long[] {0L, 7L, Long.MIN_VALUE}) {
                        Object fresh = factory.getValue()
                                .apply(unseeded.toBuilder().seed(seed).build());
                        Object reused = factory.getValue().apply(unseeded);
                        calls(reused, methods);
                        java.util.Objects.requireNonNull(Reseeding.reseederOf(reused))
                                .reseed(reused, seed);
                        assertEquals(
                                calls(fresh, methods),
                                calls(reused, methods),
                                factory.getKey().getSimpleName() + " " + locale + " advancing " + advancing + " seed "
                                        + seed);
                        compared++;
                    }
                }
            }
        }
        assertTrue(compared > 100, "compared " + compared);
    }

    @Test
    @DisplayName("a street-address generator without city, state or country data gives after a reseed what a new"
            + " build gives")
    void streetAddressWithoutPlaceDataReseedsAsBuilt() throws IllegalAccessException {
        StreetAddressDataProvider streets = new StreetAddressDataProvider() {
            @Override
            public Locale getLocale() {
                return Locale.US;
            }

            @Override
            public String[] getStreetNames() {
                return new String[] {"Oak", "Elm", "Pine"};
            }

            @Override
            public String[] getStreetTypesShort() {
                return new String[] {"St", "Ave"};
            }

            @Override
            public String[] getStreetTypesLong() {
                return new String[] {"Street", "Avenue"};
            }
        };
        DataRegistryContext context = DataRegistryContext.builder()
                .isolated()
                .register(Vocabulary.STREET_ADDRESS, streets)
                .build();
        List<Method> methods = noArgumentMethods(StreetAddressGenerator.class);
        for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
            GeneratorConfig config = GeneratorConfig.builder()
                    .locale(Locale.US)
                    .registryContext(context)
                    .childStreamPolicy(policy)
                    .seed(1L)
                    .build();
            StreetAddressGenerator reused =
                    new StreetAddressGenerator(config.toBuilder().seed(108L).build());
            calls(reused, methods);
            java.util.Objects.requireNonNull(Reseeding.reseederOf(reused)).reseed(reused, 7L);

            assertEquals(
                    calls(new StreetAddressGenerator(config.toBuilder().seed(7L).build()), methods),
                    calls(reused, methods),
                    policy.name());
        }
    }

    private static List<Method> noArgumentMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        for (Method method : type.getMethods()) {
            if (method.getParameterCount() == 0
                    && !Modifier.isStatic(method.getModifiers())
                    && method.getReturnType() != void.class
                    && method.getDeclaringClass() != Object.class
                    && !java.util.stream.BaseStream.class.isAssignableFrom(method.getReturnType())
                    && !method.getName().equals("hashCode")
                    && !method.getName().equals("toString")) {
                methods.add(method);
            }
        }
        methods.sort(Comparator.comparing(Method::getName));
        return methods;
    }

    /**
     * Each method's result, twice over, as text, or the class and message of what it threw; then the calls with
     * arguments that reach state no method without them reaches: the per-locale name generators and their lazy
     * title, suffix and middle-name generators, and unique addresses from a given domain.
     */
    private static List<String> calls(Object generator, List<Method> methods) throws IllegalAccessException {
        List<String> results = new ArrayList<>();
        for (int round = 0; round < 2; round++) {
            for (Method method : methods) {
                results.add(method.getName() + "=" + call(generator, method));
            }
            if (generator instanceof FullNameGenerator names) {
                for (String nationality : java.util.Arrays.asList(null, "de", "it")) {
                    results.add(attempt(() -> names.generate(
                            new FullNameGenerator.NameOptions(true, false, true, true, false, null, nationality))));
                    results.add(attempt(() -> names.generate(
                            new FullNameGenerator.NameOptions(false, true, false, false, true, null, nationality))));
                }
            }
            if (generator instanceof EmailGenerator emails) {
                results.add(attempt(() -> emails.generateUnique("example.org")));
                results.add(attempt(() -> emails.generateUniqueFromDomains("example.com", "example.net")));
            }
        }
        return results;
    }

    private static String attempt(java.util.function.Supplier<String> call) {
        try {
            return call.get();
        } catch (RuntimeException thrown) {
            return "threw " + thrown.getClass().getName() + ": " + thrown.getMessage();
        }
    }

    private static String call(Object generator, Method method) throws IllegalAccessException {
        try {
            // deepToString of a one-element array writes the contents of an array result, primitive or not.
            return Arrays.deepToString(new Object[] {method.invoke(generator)});
        } catch (InvocationTargetException thrown) {
            Throwable cause = thrown.getCause();
            return "threw " + cause.getClass().getName() + ": " + cause.getMessage();
        }
    }
}
