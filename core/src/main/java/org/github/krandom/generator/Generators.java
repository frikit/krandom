/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.generator.games.coin.CoinGenerator;
import org.github.krandom.generator.games.dice.DiceGenerator;
import org.github.krandom.generator.games.dice.DiceType;
import org.github.krandom.generator.algorithms.FibonacciGenerator;
import org.github.krandom.generator.algorithms.LuhnGenerator;
import org.github.krandom.generator.base.*;
import org.github.krandom.generator.file.FileExtensionGenerator;
import org.github.krandom.generator.file.FileNameGenerator;
import org.github.krandom.generator.identifier.IsbnGenerator;
import org.github.krandom.generator.location.StreetAddressGenerator;
import org.github.krandom.generator.network.IPv4Generator;
import org.github.krandom.generator.network.IPv6Generator;
import org.github.krandom.generator.network.MacAddressGenerator;
import org.github.krandom.generator.selection.PickGenerator;
import org.github.krandom.generator.selection.PickSetGenerator;
import org.github.krandom.generator.selection.RepeatGenerator;
import org.github.krandom.generator.selection.ShuffleGenerator;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.selection.WeightedGenerator;
import org.github.krandom.generator.text.LoremIpsumGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.CompanyNameGenerator;
import org.github.krandom.generator.user.FullNameGenerator;
import org.github.krandom.generator.user.ProfessionGenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.github.krandom.generator.datetime.DateGenerator;
import org.github.krandom.generator.datetime.InstantGenerator;
import org.github.krandom.generator.datetime.LocalDateTimeGenerator;
import org.github.krandom.generator.datetime.ZonedDateTimeGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * Static factory for all built-in base-type generators.
 *
 * <p>Every {@code of*()} method has two forms:
 * <ul>
 *   <li>No-arg — default range / default character set, uses {@link java.security.SecureRandom}.</li>
 *   <li>With bounds (and optional seed) — fully configured.</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 *   int          value  = Generators.ofInt().generate();
 *   int          roll   = Generators.ofInt(1, 7).generate();          // die [1..6]
 *   List<String> names  = Generators.ofString().generateList(20);
 *   List<Long>   ids    = Generators.ofLong(1L, 1_000_000L).generateList(100);
 *
 *   // Generic lookup by Class
 *   Generator<Integer> g = Generators.forType(Integer.class);
 * }</pre>
 *
 * <p>All generators returned by this class are independent instances — they do not share state.
 */
public final class Generators {

    private Generators() { /* static utility */ }

    // ── Byte ──────────────────────────────────────────────────────────────────

    public static ByteGenerator ofByte() {
        return new ByteGenerator();
    }

    public static ByteGenerator ofByte(byte min, byte max) {
        return new ByteGenerator(min, max);
    }

    public static ByteGenerator ofByte(byte min, byte max, long seed) {
        return new ByteGenerator(min, max, seed);
    }

    // ── Short ─────────────────────────────────────────────────────────────────

    public static ShortGenerator ofShort() {
        return new ShortGenerator();
    }

    public static ShortGenerator ofShort(short min, short max) {
        return new ShortGenerator(min, max);
    }

    public static ShortGenerator ofShort(short min, short max, long seed) {
        return new ShortGenerator(min, max, seed);
    }

    // ── Int ───────────────────────────────────────────────────────────────────

    public static IntGenerator ofInt() {
        return new IntGenerator();
    }

    public static IntGenerator ofInt(int min, int max) {
        return new IntGenerator(min, max);
    }

    public static IntGenerator ofInt(int min, int max, long seed) {
        return new IntGenerator(min, max, seed);
    }

    // ── Natural Number ────────────────────────────────────────────────────────

    public static NaturalNumberGenerator ofNaturalNumber() {
        return new NaturalNumberGenerator();
    }

    public static NaturalNumberGenerator ofNaturalNumber(int min, int max) {
        return new NaturalNumberGenerator(min, max);
    }

    public static NaturalNumberGenerator ofNaturalNumber(int min, int max, long seed) {
        return new NaturalNumberGenerator(min, max, seed);
    }

    // ── Long ──────────────────────────────────────────────────────────────────

    public static LongGenerator ofLong() {
        return new LongGenerator();
    }

    public static LongGenerator ofLong(long min, long max) {
        return new LongGenerator(min, max);
    }

    public static LongGenerator ofLong(long min, long max, long seed) {
        return new LongGenerator(min, max, seed);
    }

    // ── Float ─────────────────────────────────────────────────────────────────

    public static FloatGenerator ofFloat() {
        return new FloatGenerator();
    }

    public static FloatGenerator ofFloat(float min, float max) {
        return new FloatGenerator(min, max);
    }

    public static FloatGenerator ofFloat(float min, float max, long seed) {
        return new FloatGenerator(min, max, seed);
    }

    // ── Double ────────────────────────────────────────────────────────────────

    public static DoubleGenerator ofDouble() {
        return new DoubleGenerator();
    }

    public static DoubleGenerator ofDouble(double min, double max) {
        return new DoubleGenerator(min, max);
    }

    public static DoubleGenerator ofDouble(double min, double max, long seed) {
        return new DoubleGenerator(min, max, seed);
    }

    // ── Normal Distribution ───────────────────────────────────────────────────

    public static NormalDistributionGenerator ofNormal() {
        return new NormalDistributionGenerator();
    }

    public static NormalDistributionGenerator ofNormal(double mean, double standardDeviation) {
        return new NormalDistributionGenerator(mean, standardDeviation);
    }

    public static NormalDistributionGenerator ofNormal(double mean, double standardDeviation, long seed) {
        return new NormalDistributionGenerator(mean, standardDeviation, seed);
    }

    // ── Prime ─────────────────────────────────────────────────────────────────

    public static PrimeGenerator ofPrime() {
        return new PrimeGenerator();
    }

    public static PrimeGenerator ofPrime(int min, int max) {
        return new PrimeGenerator(min, max);
    }

    public static PrimeGenerator ofPrime(int min, int max, long seed) {
        return new PrimeGenerator(min, max, seed);
    }

    // ── Char ──────────────────────────────────────────────────────────────────

    /** Letters (upper + lower). */
    public static CharGenerator ofChar() {
        return CharGenerator.letters();
    }

    /** Custom character pool via a {@link CharGenerator.Builder}. */
    public static CharGenerator.Builder ofChar(CharGenerator.Builder builder) {
        return Objects.requireNonNull(builder, "builder");
    }

    // ── Boolean ───────────────────────────────────────────────────────────────

    public static BooleanGenerator ofBoolean() {
        return new BooleanGenerator();
    }

    public static BooleanGenerator ofBoolean(long seed) {
        return new BooleanGenerator(seed);
    }

    // ── String ────────────────────────────────────────────────────────────────

    /** Letters only, length 5–20. */
    public static StringGenerator ofString() {
        return StringGenerator.letters();
    }

    /** Full control via a pre-configured builder. */
    public static StringGenerator ofString(StringGenerator.Builder builder) {
        return Objects.requireNonNull(builder, "builder").build();
    }

    // ── Algorithms ────────────────────────────────────────────────────────────

    /** Returns a generator that produces random Fibonacci numbers. */
    public static FibonacciGenerator ofFibonacci() {
        return new FibonacciGenerator();
    }

    /** Returns a generator that produces 10-digit Luhn-valid number strings. */
    public static LuhnGenerator ofLuhn() {
        return new LuhnGenerator();
    }

    // ── Games ─────────────────────────────────────────────────────────────────

    /** Returns a generator that produces random coin-flip results ({@code HEAD} or {@code TAIL}). */
    public static CoinGenerator ofCoin() {
        return new CoinGenerator();
    }

    /** Returns a generator for the given die type (results in {@code [1, sides]}). */
    public static DiceGenerator ofDice(DiceType type) {
        return new DiceGenerator(type);
    }

    // ── Network ───────────────────────────────────────────────────────────────

    /** Returns a generator that produces random IPv4 addresses (RFC 791, unicast range). */
    public static IPv4Generator ofIPv4() {
        return new IPv4Generator();
    }

    /** Returns a generator that produces random IPv6 addresses (RFC 4291 / RFC 5952). */
    public static IPv6Generator ofIPv6() {
        return new IPv6Generator();
    }

    // ── BigDecimal ────────────────────────────────────────────────────────────

    /** Returns a generator producing random {@link BigDecimal} values ([0, 1&nbsp;000&nbsp;000], scale 2). */
    public static BigDecimalGenerator ofBigDecimal() {
        return new BigDecimalGenerator();
    }

    /**
     * Returns a generator producing random {@link BigDecimal} values in [min, max] with scale 2.
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (inclusive)
     */
    public static BigDecimalGenerator ofBigDecimal(BigDecimal min, BigDecimal max) {
        return new BigDecimalGenerator(min, max);
    }

    // ── BigInteger ────────────────────────────────────────────────────────────

    /** Returns a generator producing random {@link BigInteger} values ([0, {@link Long#MAX_VALUE}]). */
    public static BigIntegerGenerator ofBigInteger() {
        return new BigIntegerGenerator();
    }

    /**
     * Returns a generator producing random {@link BigInteger} values in [min, max].
     *
     * @param min lower bound (inclusive)
     * @param max upper bound (inclusive)
     */
    public static BigIntegerGenerator ofBigInteger(BigInteger min, BigInteger max) {
        return new BigIntegerGenerator(min, max);
    }

    // ── Date / Time ───────────────────────────────────────────────────────────

    /** Returns a generator that produces random {@link java.time.LocalDate} values (1970–2100). */
    public static DateGenerator ofLocalDate() {
        return new DateGenerator();
    }

    /** Returns a generator that produces random {@link java.time.LocalDateTime} values (1970–2100). */
    public static LocalDateTimeGenerator ofLocalDateTime() {
        return new LocalDateTimeGenerator();
    }

    /** Returns a generator that produces random {@link java.time.Instant} values (1970–2100 at UTC midnight). */
    public static InstantGenerator ofInstant() {
        return new InstantGenerator();
    }

    /** Returns a generator that produces random {@link java.time.ZonedDateTime} values (1970–2100). */
    public static ZonedDateTimeGenerator ofZonedDateTime() {
        return new ZonedDateTimeGenerator();
    }

    // ── Full name ─────────────────────────────────────────────────────────────

    /** Returns a generator that produces random full names (first + last) in {@link java.util.Locale#US}. */
    public static FullNameGenerator ofFullName() {
        return new FullNameGenerator();
    }

    // ── Street address ────────────────────────────────────────────────────────

    /** Returns a generator that produces random US-style street addresses (e.g. {@code "123 Oak Ave"}). */
    public static StreetAddressGenerator ofStreetAddress() {
        return new StreetAddressGenerator();
    }

    // ── Company name ──────────────────────────────────────────────────────────

    /** Returns a generator that produces random company names including a legal-form suffix. */
    public static CompanyNameGenerator ofCompanyName() {
        return new CompanyNameGenerator();
    }

    /** Returns a generator that produces file extensions (for example: {@code "png"}, {@code "pdf"}). */
    public static FileExtensionGenerator ofFileExtension() {
        return new FileExtensionGenerator();
    }

    /** Returns a generator that produces file names and file names with extensions. */
    public static FileNameGenerator ofFileName() {
        return new FileNameGenerator();
    }

    /** Returns a generator that produces locale-aware profession/job-title values. */
    public static ProfessionGenerator ofProfession() {
        return new ProfessionGenerator();
    }

    // ── Lorem Ipsum ───────────────────────────────────────────────────────────

    /** Returns a generator that produces Lorem Ipsum sentences (default {@link LoremIpsumGenerator.Mode#SENTENCE}). */
    public static LoremIpsumGenerator ofLoremIpsum() {
        return new LoremIpsumGenerator();
    }

    /**
     * Returns a generator that produces Lorem Ipsum text in the specified mode.
     *
     * @param mode {@link LoremIpsumGenerator.Mode#WORD}, {@link LoremIpsumGenerator.Mode#SENTENCE},
     *             or {@link LoremIpsumGenerator.Mode#PARAGRAPH}; must not be {@code null}
     */
    public static LoremIpsumGenerator ofLoremIpsum(LoremIpsumGenerator.Mode mode) {
        return new LoremIpsumGenerator(mode);
    }

    /** Returns a generator that produces natural-looking pseudo-words. */
    public static WordGenerator ofWord() {
        return new WordGenerator();
    }

    // ── MAC address ───────────────────────────────────────────────────────────

    /** Returns a generator that produces random MAC addresses ({@code "XX:XX:XX:XX:XX:XX"}). */
    public static MacAddressGenerator ofMacAddress() {
        return new MacAddressGenerator();
    }

    // ── ISBN ──────────────────────────────────────────────────────────────────

    /** Returns a generator that produces random ISBN-13 numbers. */
    public static IsbnGenerator ofIsbn() {
        return new IsbnGenerator();
    }

    /**
     * Returns a generator that produces random ISBN numbers in the specified format.
     *
     * @param type {@link IsbnGenerator.IsbnType#ISBN_10} or {@link IsbnGenerator.IsbnType#ISBN_13};
     *             must not be {@code null}
     */
    public static IsbnGenerator ofIsbn(IsbnGenerator.IsbnType type) {
        return new IsbnGenerator(type);
    }

    // ── Selection / helper-style generators ──────────────────────────────────

    /** Returns a generator that picks one random element from the given source list. */
    public static <T> PickGenerator<T> pickFrom(List<T> source) {
        return new PickGenerator<>(source);
    }

    /** Returns a generator that picks {@code count} distinct elements without replacement. */
    public static <T> PickSetGenerator<T> pickSetFrom(List<T> source, int count) {
        return new PickSetGenerator<>(source, count);
    }

    /** Returns a generator that returns a shuffled copy of the given list. */
    public static <T> ShuffleGenerator<T> shuffleOf(List<T> source) {
        return new ShuffleGenerator<>(source);
    }

    /** Returns a weighted generator that selects values according to positive integer weights. */
    public static <T> WeightedGenerator<T> weighted(List<T> values, List<Integer> weights) {
        return new WeightedGenerator<>(values, weights);
    }

    /** Returns a unique-value decorator using {@link Objects#equals(Object, Object)} semantics. */
    public static <T> UniqueGenerator<T> unique(Generator<T> source) {
        return new UniqueGenerator<>(source);
    }

    /** Returns a unique-value decorator with bounded attempts for each generated value. */
    public static <T> UniqueGenerator<T> unique(Generator<T> source, int maxAttempts) {
        return new UniqueGenerator<>(source, maxAttempts);
    }

    /** Returns a unique-value decorator with a custom equality comparator. */
    public static <T> UniqueGenerator<T> unique(Generator<T> source, BiPredicate<T, T> comparator) {
        return new UniqueGenerator<>(source, comparator);
    }

    /** Returns a generator that invokes the given source generator {@code count} times per call. */
    public static <T> RepeatGenerator<T> repeat(Generator<T> source, int count) {
        return new RepeatGenerator<>(source, count);
    }

    // ── Generic lookup by type ────────────────────────────────────────────────

    /**
     * Return a default {@link Generator} for the given Java primitive wrapper class.
     *
     * <p>Supported types: {@code Byte}, {@code Short}, {@code Integer}, {@code Long},
     * {@code Float}, {@code Double}, {@code Character}, {@code Boolean}, {@code String}.
     *
     * @param type the wrapper class; must not be {@code null}
     * @throws IllegalArgumentException if the type has no built-in generator
     */
    @SuppressWarnings("unchecked")
    public static <T> Generator<T> forType(Class<T> type) {
        Objects.requireNonNull(type, "type must not be null");
        Supplier<?> factory = REGISTRY.get(type);
        if (factory == null) {
            throw new IllegalArgumentException(
                    "No built-in generator for type: " + type.getName() +
                    ". Register a custom Generator or use one of the of*() methods.");
        }
        return (Generator<T>) factory.get();
    }

    // ── Internal registry ─────────────────────────────────────────────────────

    private static final Map<Class<?>, Supplier<? extends Generator<?>>> REGISTRY;

    static {
        REGISTRY = new HashMap<>();
        REGISTRY.put(Byte.class,      ByteGenerator::new);
        REGISTRY.put(byte.class,      ByteGenerator::new);
        REGISTRY.put(Short.class,     ShortGenerator::new);
        REGISTRY.put(short.class,     ShortGenerator::new);
        REGISTRY.put(Integer.class,   IntGenerator::new);
        REGISTRY.put(int.class,       IntGenerator::new);
        REGISTRY.put(Long.class,      LongGenerator::new);
        REGISTRY.put(long.class,      LongGenerator::new);
        REGISTRY.put(Float.class,     FloatGenerator::new);
        REGISTRY.put(float.class,     FloatGenerator::new);
        REGISTRY.put(Double.class,    DoubleGenerator::new);
        REGISTRY.put(double.class,    DoubleGenerator::new);
        REGISTRY.put(Character.class, CharGenerator::letters);
        REGISTRY.put(char.class,      CharGenerator::letters);
        REGISTRY.put(Boolean.class,   BooleanGenerator::new);
        REGISTRY.put(boolean.class,   BooleanGenerator::new);
        REGISTRY.put(String.class,    StringGenerator::letters);
    }
}
