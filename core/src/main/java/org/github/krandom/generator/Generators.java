/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator;

import org.github.krandom.games.coin.CoinGenerator;
import org.github.krandom.games.dice.DiceGenerator;
import org.github.krandom.games.dice.DiceType;
import org.github.krandom.network.IPv4Generator;
import org.github.krandom.network.IPv6Generator;
import org.github.krandom.generator.algorithms.FibonacciGenerator;
import org.github.krandom.generator.algorithms.LuhnGenerator;
import org.github.krandom.generator.base.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
