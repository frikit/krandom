/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.location.CountryCodes;
import io.github.frikit.krandom.generator.user.Gender;
import io.github.frikit.krandom.generator.user.TitleGenerator;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * One source of everyday fake values for tests and fixtures, returned as plain values.
 *
 * <pre>{@code
 * Krandom fake = Krandom.seeded("checkout-fixtures", Locale.UK);
 *
 * String forename = fake.firstName();
 * String surname = fake.lastName();
 * LocalDate birthDate = fake.birthday(25, 80);
 * String postcode = fake.postalCode();
 * }</pre>
 *
 * <p>Each method is one kind of value with a generator of its own, and every call of a method shares
 * that generator, whatever its arguments. The exceptions are {@link #nationalIdCompact()}, which
 * draws from {@link #nationalId()}'s stream, and the overloads of {@code firstName},
 * {@code middleName} and {@code pick}: each pair shares one stream.
 * With a seeded configuration, each method's stream is
 * derived from the seed and the method's name with
 * {@link GenerationRecipe#deriveChildSeed(long, String)}, whatever the configured
 * {@link ChildStreamPolicy}. Repeated calls vary, a method's values are unaffected by calls of other
 * methods, and the same seed repeats the same values; {@link #today()} and the dates derived from it
 * follow the configured clock, so fix the clock for dates that are the same on every day. Without a
 * seed, each method draws from fresh, independent randomness. Create one session and reuse it: a
 * new session starts every stream again.
 *
 * <p>For a generator the session does not cover, or for an independent stream of a kind it does,
 * build the generator from {@link #streamConfig(String)}. A method throws
 * {@link UnsupportedOperationException} when the configured locale has no data for its kind.
 * National IDs follow the configuration's
 * {@link GeneratorConfig#getNationalIdSafetyPolicy() national-ID safety policy}, which refuses them
 * by default. A session is not thread-safe; use one per thread.
 */
public final class Krandom {

    private static final String KIND_STREAM_PREFIX = "krandom.";
    private static final Pattern NATIONAL_ID_SEPARATORS = Pattern.compile("[^\\p{L}\\p{N}]");

    private final GeneratorConfig config;
    private final Map<String, Object> kinds = new HashMap<>();

    private Krandom(GeneratorConfig config) {
        this.config = config;
    }

    /**
     * Creates a session over a configuration, using its seed, locale, clock, registries, and
     * policies.
     *
     * @param config the root configuration; must not be {@code null}
     * @return a new session
     * @throws IllegalArgumentException if the configuration uses a caller-owned {@code Random} or a
     *     random factory, which every kind of value would share
     */
    public static Krandom of(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        if (config.getRandom().isPresent() || config.getRandomFactory().isPresent()) {
            throw new IllegalArgumentException("Krandom needs a seed or the default random source: a caller-owned"
                    + " Random or random factory would be shared by every kind of value");
        }
        return new Krandom(config);
    }

    /**
     * Creates a session seeded with a textual seed, in {@link GeneratorConfig}'s default locale
     * ({@code en_US}). The text is hashed into a numeric seed, so {@code seeded("42")} differs from a
     * numeric seed of {@code 42}.
     *
     * @param seed the textual seed
     * @return a new session
     */
    public static Krandom seeded(String seed) {
        return of(GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a session seeded with a textual seed, in the given locale.
     *
     * @param seed the textual seed
     * @param locale the locale of the generated values
     * @return a new session
     */
    public static Krandom seeded(String seed, Locale locale) {
        return of(GeneratorConfig.builder().seed(seed).locale(locale).build());
    }

    /**
     * Returns the root configuration of this session.
     *
     * @return the configuration passed to {@link #of(GeneratorConfig)}
     */
    public GeneratorConfig getConfig() {
        return config;
    }

    /**
     * Returns a configuration for a stream of your own, for any generator the session does not cover
     * or for an independent stream of a kind it does. With a seed, its seed is derived from this
     * session's seed and {@code name}; without one it is the root configuration, whose generators
     * each draw fresh randomness. Equal names give equal streams, so build each generator once and
     * keep it.
     *
     * @param name a stable, non-blank, single-line stream name
     * @return the configuration for that stream
     * @throws IllegalArgumentException if {@code name} is blank or spans lines
     */
    public GeneratorConfig streamConfig(String name) {
        Objects.requireNonNull(name, "name must not be null");
        if (name.isBlank() || name.indexOf('\n') >= 0 || name.indexOf('\r') >= 0) {
            throw new IllegalArgumentException("name must be a non-blank single-line value");
        }
        OptionalLong seed = config.getSeed();
        if (seed.isEmpty()) {
            return config;
        }
        return config.toBuilder()
                .seed(GenerationRecipe.deriveChildSeed(seed.getAsLong(), name))
                .build();
    }

    // ── People ────────────────────────────────────────────────────────────────

    /**
     * Returns a first name.
     *
     * @return a locale-aware first name
     */
    public String firstName() {
        return kind("firstName", Generators::ofFirstName).generate();
    }

    /**
     * Returns a first name traditionally given to the gender, from the same stream as
     * {@link #firstName()}.
     *
     * @param gender the gender the name belongs to; must not be {@code null}
     * @return a locale-aware first name
     */
    public String firstName(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return kind("firstName", Generators::ofFirstName).generate(gender);
    }

    /**
     * Returns a middle name.
     *
     * @return a locale-aware middle name
     */
    public String middleName() {
        return kind("middleName", Generators::ofMiddleName).generate();
    }

    /**
     * Returns a middle name traditionally given to the gender, from the same stream as
     * {@link #middleName()}.
     *
     * @param gender the gender the name belongs to; must not be {@code null}
     * @return a locale-aware middle name
     */
    public String middleName(Gender gender) {
        Objects.requireNonNull(gender, "gender must not be null");
        return kind("middleName", Generators::ofMiddleName).generate(gender);
    }

    /**
     * Returns a last name.
     *
     * @return a locale-aware last name
     */
    public String lastName() {
        return kind("lastName", Generators::ofLastName).generate();
    }

    /**
     * Returns a full name.
     *
     * @return a locale-aware full name
     */
    public String fullName() {
        return kind("fullName", Generators::ofFullName).generate();
    }

    /**
     * Returns an honorific title such as {@code Mr} or {@code Dr}.
     *
     * @return a locale-aware title
     */
    public String title() {
        return kind("title", TitleGenerator::new).generate();
    }

    /**
     * Returns a marital status such as {@code Married} or {@code Dissolved partnership}.
     *
     * @return a locale-aware marital status
     */
    public String maritalStatus() {
        return kind("maritalStatus", Generators::ofMaritalStatus).generate();
    }

    /**
     * Returns an email address.
     *
     * @return an email address
     */
    public String email() {
        return kind("email", Generators::ofEmail).generate();
    }

    /**
     * Returns a phone number.
     *
     * @return a locale-aware phone number
     */
    public String phoneNumber() {
        return kind("phoneNumber", Generators::ofPhoneNumber).generate();
    }

    /**
     * Returns a national identity number in the locale's usual written form, for example
     * {@code AB 12 34 56 C} in the United Kingdom.
     *
     * @return a national identity number
     * @throws IllegalStateException if the configuration's national-ID safety policy refuses it
     * @throws UnsupportedOperationException if the configured locale has no national-ID data
     */
    public String nationalId() {
        return kind("nationalId", Generators::ofNationalId).generate();
    }

    /**
     * Returns the next {@linkplain #nationalId() national identity number} with its separators
     * removed, for example {@code AB123456C}. It draws from the same stream as {@link #nationalId()}.
     *
     * @return a national identity number with only its letters and digits
     * @throws IllegalStateException if the configuration's national-ID safety policy refuses it
     * @throws UnsupportedOperationException if the configured locale has no national-ID data
     */
    public String nationalIdCompact() {
        return NATIONAL_ID_SEPARATORS.matcher(nationalId()).replaceAll("");
    }

    /**
     * Returns a birth date for an age between {@code minAge} and {@code maxAge}, both inclusive, on
     * {@link #today()}.
     *
     * @param minAge the minimum age in years
     * @param maxAge the maximum age in years
     * @return a birth date in that age range
     * @throws IllegalArgumentException if {@code minAge} is negative or greater than {@code maxAge}
     */
    public LocalDate birthday(int minAge, int maxAge) {
        if (minAge < 0 || minAge > maxAge) {
            throw new IllegalArgumentException(
                    "ages must satisfy 0 <= minAge <= maxAge, got: " + minAge + " and " + maxAge);
        }
        LocalDate today = today();
        return kind("birthday", Generators::ofLocalDate)
                .between(today.minusYears(maxAge + 1L).plusDays(1), today.minusYears(minAge));
    }

    // ── Places ────────────────────────────────────────────────────────────────

    /**
     * Returns a street address.
     *
     * @return a locale-aware street address
     */
    public String streetAddress() {
        return kind("streetAddress", Generators::ofStreetAddress).generate();
    }

    /**
     * Returns a city name.
     *
     * @return a locale-aware city name
     */
    public String city() {
        return kind("city", Generators::ofCity).generate();
    }

    /**
     * Returns a postal code.
     *
     * @return a locale-aware postal code
     */
    public String postalCode() {
        return kind("postalCode", Generators::ofPostalCode).generate();
    }

    /**
     * Returns an address type by property use, such as {@code "Residential"} or {@code "Commercial"}.
     *
     * @return a locale-aware address type
     */
    public String addressType() {
        return kind("addressType", Generators::ofAddressType).generate();
    }

    /**
     * Returns what an address is used for, such as {@code "Home address"} or {@code "Billing address"}.
     *
     * @return a locale-aware address purpose
     */
    public String addressPurpose() {
        return kind("addressPurpose", Generators::ofAddressPurpose).generate();
    }

    /**
     * Returns a country name.
     *
     * @return a locale-aware country name
     */
    public String country() {
        return kind("country", Generators::ofCountry).generate();
    }

    /**
     * Returns an ISO 3166-1 alpha-2 country code, such as {@code "DE"}. The three code formats are drawn
     * independently, so use {@link #countryCodes()} when the codes must belong to one country.
     *
     * @return a two-letter country code
     */
    public String countryCode() {
        return kind("countryCode", Generators::ofCountry).generateCode();
    }

    /**
     * Returns an ISO 3166-1 alpha-3 country code, such as {@code "DEU"}, drawn independently of the other
     * code formats.
     *
     * @return a three-letter country code
     */
    public String countryCodeAlpha3() {
        return kind("countryCodeAlpha3", Generators::ofCountry).generateCodeAlpha3();
    }

    /**
     * Returns an ISO 3166-1 numeric country code, such as {@code "276"}, drawn independently of the other
     * code formats.
     *
     * @return a three-digit country code
     */
    public String countryCodeNumeric() {
        return kind("countryCodeNumeric", Generators::ofCountry).generateCodeNumeric();
    }

    /**
     * Returns the alpha-2, alpha-3 and numeric ISO 3166-1 codes of one country, such as {@code GB},
     * {@code GBR} and {@code 826}, for when the codes must belong together.
     *
     * @return the codes of one country
     */
    public CountryCodes countryCodes() {
        return kind("countryCodes", Generators::ofCountry).generateCodes();
    }

    // ── Dates, numbers, and choices ───────────────────────────────────────────

    /**
     * Returns today's date in the configuration's clock, so a fixed clock fixes it.
     *
     * @return today's date
     */
    public LocalDate today() {
        return LocalDate.now(config.getClock());
    }

    /**
     * Returns a date between two bounds, both inclusive.
     *
     * @param fromInclusive the earliest date
     * @param toInclusive the latest date
     * @return a date in that range
     * @throws IllegalArgumentException if {@code fromInclusive} is after {@code toInclusive}
     */
    public LocalDate dateBetween(LocalDate fromInclusive, LocalDate toInclusive) {
        return kind("date", Generators::ofLocalDate).between(fromInclusive, toInclusive);
    }

    /**
     * Returns an integer between two bounds, both inclusive, unlike {@code Generators.ofInt(min, max)},
     * whose upper bound is exclusive.
     *
     * @param minInclusive the smallest value
     * @param maxInclusive the largest value
     * @return an integer in that range
     * @throws IllegalArgumentException if {@code minInclusive} is greater than {@code maxInclusive}
     */
    public int intBetween(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException(
                    "minInclusive must be <= maxInclusive, got: " + minInclusive + " > " + maxInclusive);
        }
        return (int) random("int").nextLong(minInclusive, (long) maxInclusive + 1);
    }

    /**
     * Returns one of the given values.
     *
     * @param values the values to choose from; must not be empty
     * @param <T> the value type
     * @return one of {@code values}
     * @throws IllegalArgumentException if {@code values} is empty
     */
    @SafeVarargs
    public final <T> T pick(T... values) {
        return pick(Arrays.asList(Objects.requireNonNull(values, "values must not be null")));
    }

    /**
     * Returns one element of the given list. It shares its stream with {@link #pick(Object...)}.
     *
     * @param values the values to choose from; must not be empty
     * @param <T> the value type
     * @return one element of {@code values}
     * @throws IllegalArgumentException if {@code values} is empty
     */
    public <T> T pick(List<? extends T> values) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        return values.get(random("pick").nextInt(values.size()));
    }

    private Random random(String kind) {
        return kind(kind, GeneratorConfig::createRandom);
    }

    @SuppressWarnings("unchecked")
    private <G> G kind(String kind, Function<GeneratorConfig, G> factory) {
        Object generator = kinds.get(kind);
        if (generator == null) {
            generator = factory.apply(streamConfig(KIND_STREAM_PREFIX + kind));
            kinds.put(kind, generator);
        }
        return (G) generator;
    }
}
