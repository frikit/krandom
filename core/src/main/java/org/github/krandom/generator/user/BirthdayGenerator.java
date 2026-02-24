/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Random;

/**
 * Generates random birth dates as {@link LocalDate} values.
 *
 * <p>Supports type-based age ranges via {@link AgeType}, custom min/max age bounds,
 * or the default full range of 1–100 years ago.
 *
 * <p>When constructed with a {@link Locale}, {@link #generateAsString()} uses the locale's
 * conventional date format. Supported locales and their formats:
 * <ul>
 *   <li>{@code en_US} (default): {@code M/d/yyyy} — e.g., {@code "5/27/1983"}
 *   <li>{@code en_GB}, {@code en_AU}: {@code d/M/yyyy} — e.g., {@code "27/5/1983"}
 *   <li>{@code de_DE}: {@code d.M.yyyy} — e.g., {@code "27.5.1983"}
 *   <li>{@code ja_JP}: {@code yyyy/M/d} — e.g., {@code "1983/5/27"}
 *   <li>{@code fr_FR}, {@code es_ES}, {@code it_IT}, {@code pt_BR}: {@code d/M/yyyy}
 *   <li>{@code zh_CN}: {@code yyyy年M月d日} — e.g., {@code "1983年5月27日"}
 * </ul>
 *
 * <pre>{@code
 * LocalDate birthday = new BirthdayGenerator().generate();
 * LocalDate adultBirthday = new BirthdayGenerator(AgeType.ADULT).generate();
 *
 * // Formatted output
 * String formatted = new BirthdayGenerator().generateAsString();              // "5/27/1983"
 * String american  = new BirthdayGenerator().generateAsAmericanString();      // "05/27/1983"
 * String german    = new BirthdayGenerator(Locale.GERMANY).generateAsString(); // "27.5.1983"
 * }</pre>
 */
public final class BirthdayGenerator implements Generator<LocalDate> {

    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 100;

    private static final DateTimeFormatter STRING_FORMAT   = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final DateTimeFormatter AMERICAN_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final Map<String, DateTimeFormatter> LOCALE_FORMATTERS = Map.ofEntries(
            Map.entry("en_US", DateTimeFormatter.ofPattern("M/d/yyyy")),
            Map.entry("en_GB", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("en_AU", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("fr_FR", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("de_DE", DateTimeFormatter.ofPattern("d.M.yyyy")),
            Map.entry("de",    DateTimeFormatter.ofPattern("d.M.yyyy")),
            Map.entry("ja_JP", DateTimeFormatter.ofPattern("yyyy/M/d")),
            Map.entry("ja",    DateTimeFormatter.ofPattern("yyyy/M/d")),
            Map.entry("es_ES", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("it_IT", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("pt_BR", DateTimeFormatter.ofPattern("d/M/yyyy")),
            Map.entry("zh_CN", DateTimeFormatter.ofPattern("yyyy年M月d日")),
            Map.entry("zh",    DateTimeFormatter.ofPattern("yyyy年M月d日"))
    );

    private final Random random;
    private final int minAge;
    private final int maxAge;
    private final Locale locale;

    // ── No-locale constructors (backward compatible) ───────────────────────────

    /** Generates birthdays for ages in the full range [1, 100]. */
    public BirthdayGenerator() {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.empty(), null);
    }

    /** Generates birthdays for ages [1, 100] with a fixed seed for reproducible output. */
    public BirthdayGenerator(long seed) {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.of(seed), null);
    }

    /**
     * Generates birthdays appropriate for the given {@link AgeType}.
     *
     * @param type the age category; must not be {@code null}
     */
    public BirthdayGenerator(AgeType type) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.empty(),
                null);
    }

    /**
     * Generates birthdays appropriate for the given {@link AgeType}, with a fixed seed.
     *
     * @param type the age category; must not be {@code null}
     * @param seed PRNG seed for reproducible output
     */
    public BirthdayGenerator(AgeType type, long seed) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.of(seed),
                null);
    }

    /**
     * Generates birthdays for the given inclusive age range.
     *
     * @param minAge minimum age in years (inclusive, must be ≥ 0)
     * @param maxAge maximum age in years (inclusive, must be ≥ {@code minAge})
     */
    public BirthdayGenerator(int minAge, int maxAge) {
        this(minAge, maxAge, OptionalLong.empty(), null);
    }

    // ── Locale-aware constructors ─────────────────────────────────────────────

    /**
     * Generates birthdays for ages [1, 100] with locale-aware string formatting.
     *
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     */
    public BirthdayGenerator(Locale locale) {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.empty(),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    /**
     * Generates birthdays for the given {@link AgeType} with locale-aware string formatting.
     *
     * @param type   the age category; must not be {@code null}
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     */
    public BirthdayGenerator(AgeType type, Locale locale) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.empty(),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    /**
     * Generates birthdays for the given inclusive age range with locale-aware string formatting.
     *
     * @param minAge minimum age in years (inclusive, must be ≥ 0)
     * @param maxAge maximum age in years (inclusive, must be ≥ {@code minAge})
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     */
    public BirthdayGenerator(int minAge, int maxAge, Locale locale) {
        this(minAge, maxAge, OptionalLong.empty(),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    /**
     * Generates birthdays for ages [1, 100] with locale-aware formatting and a fixed seed.
     *
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     * @param seed   PRNG seed for reproducible output
     */
    public BirthdayGenerator(Locale locale, long seed) {
        this(DEFAULT_MIN, DEFAULT_MAX, OptionalLong.of(seed),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    /**
     * Generates birthdays for the given {@link AgeType} with locale-aware formatting and a fixed seed.
     *
     * @param type   the age category; must not be {@code null}
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     * @param seed   PRNG seed for reproducible output
     */
    public BirthdayGenerator(AgeType type, Locale locale, long seed) {
        this(Objects.requireNonNull(type, "type must not be null").getMinAge(),
                type.getMaxAge(),
                OptionalLong.of(seed),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    /**
     * Generates birthdays for the given inclusive age range with locale-aware formatting and a fixed seed.
     *
     * @param minAge minimum age in years (inclusive, must be ≥ 0)
     * @param maxAge maximum age in years (inclusive, must be ≥ {@code minAge})
     * @param locale the locale used to format output from {@link #generateAsString()};
     *               must not be {@code null}
     * @param seed   PRNG seed for reproducible output
     */
    public BirthdayGenerator(int minAge, int maxAge, Locale locale, long seed) {
        this(minAge, maxAge, OptionalLong.of(seed),
                Objects.requireNonNull(locale, "locale must not be null"));
    }

    // ── Canonical private constructor ─────────────────────────────────────────

    private BirthdayGenerator(int minAge, int maxAge, OptionalLong seed, Locale locale) {
        if (minAge < 0) {
            throw new IllegalArgumentException("minAge must be >= 0, got: " + minAge);
        }
        if (maxAge < minAge) {
            throw new IllegalArgumentException(
                    "maxAge must be >= minAge, got: minAge=" + minAge + ", maxAge=" + maxAge);
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.locale = locale;
        this.random = seed.isPresent() ? new Random(seed.getAsLong()) : new SecureRandom();
    }

    // ── Generation ────────────────────────────────────────────────────────────

    /**
     * Generates a random birth date consistent with a person of the configured age range.
     *
     * <p>The returned date is in the past; the person's current age (in completed years)
     * will fall within [{@code minAge}, {@code maxAge}].
     */
    @Override
    public LocalDate generate() {
        LocalDate today    = LocalDate.now();
        int age            = minAge + random.nextInt(maxAge - minAge + 1);
        LocalDate latest   = today.minusYears(age);
        LocalDate earliest = today.minusYears((long) age + 1).plusDays(1);
        long daysInRange   = ChronoUnit.DAYS.between(earliest, latest) + 1;
        return earliest.plusDays(random.nextLong(daysInRange));
    }

    /**
     * Generates a birthday and formats it according to the configured locale.
     *
     * <p>If no locale was specified, formats as {@code M/d/yyyy} (e.g., {@code "5/27/1983"}),
     * matching the Chance.js {@code birthday({string:true})} output.
     *
     * <p>If a locale is set but has no specific formatter registered, falls back first to a
     * language-level match, then to {@code M/d/yyyy}.
     */
    public String generateAsString() {
        LocalDate date = generate();
        if (locale == null) return date.format(STRING_FORMAT);
        DateTimeFormatter fmt = LOCALE_FORMATTERS.get(locale.getLanguage() + "_" + locale.getCountry());
        if (fmt == null) fmt = LOCALE_FORMATTERS.get(locale.getLanguage());
        if (fmt == null) fmt = STRING_FORMAT;
        return date.format(fmt);
    }

    /**
     * Generates a birthday and formats it as {@code MM/dd/yyyy} (e.g., {@code "05/27/1983"}).
     *
     * <p>Month and day are zero-padded, matching the Chance.js {@code birthday({american:true})}
     * output. This method is unaffected by the locale setting.
     */
    public String generateAsAmericanString() {
        return generate().format(AMERICAN_FORMAT);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    /** Returns the minimum age (inclusive) used for birthday generation. */
    public int getMinAge() {
        return minAge;
    }

    /** Returns the maximum age (inclusive) used for birthday generation. */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * Returns the locale configured for string formatting, or {@code null} if no locale was set.
     *
     * @return the locale, or {@code null}
     */
    public Locale getLocale() {
        return locale;
    }
}
