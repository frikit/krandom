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
import org.github.krandom.generator.network.IPv4Generator;
import org.github.krandom.generator.network.IPv6Generator;
import org.github.krandom.generator.selection.PickGenerator;
import org.github.krandom.generator.selection.PickSetGenerator;
import org.github.krandom.generator.selection.RepeatGenerator;
import org.github.krandom.generator.selection.ShuffleGenerator;
import org.github.krandom.generator.selection.UniqueGenerator;
import org.github.krandom.generator.selection.WeightedGenerator;
import org.github.krandom.generator.text.ParagraphGenerator;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.SyllableGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.ProfessionGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generators factory")
class GeneratorsTest {

    // ── Byte ──────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofByte() returns ByteGenerator")
    void ofByte() { assertInstanceOf(ByteGenerator.class, Generators.ofByte()); }

    @Test @DisplayName("ofByte(min, max) returns ByteGenerator")
    void ofByteRange() { assertInstanceOf(ByteGenerator.class, Generators.ofByte((byte) 0, (byte) 10)); }

    @Test @DisplayName("ofByte(min, max, seed) returns ByteGenerator")
    void ofByteSeeded() { assertInstanceOf(ByteGenerator.class, Generators.ofByte((byte) 0, (byte) 10, 1L)); }

    // ── Short ─────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofShort() returns ShortGenerator")
    void ofShort() { assertInstanceOf(ShortGenerator.class, Generators.ofShort()); }

    @Test @DisplayName("ofShort(min, max) returns ShortGenerator")
    void ofShortRange() { assertInstanceOf(ShortGenerator.class, Generators.ofShort((short) 0, (short) 100)); }

    @Test @DisplayName("ofShort(min, max, seed) returns ShortGenerator")
    void ofShortSeeded() { assertInstanceOf(ShortGenerator.class, Generators.ofShort((short) 0, (short) 100, 1L)); }

    // ── Int ───────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofInt() returns IntGenerator")
    void ofInt() { assertInstanceOf(IntGenerator.class, Generators.ofInt()); }

    @Test @DisplayName("ofInt(min, max) returns IntGenerator")
    void ofIntRange() { assertInstanceOf(IntGenerator.class, Generators.ofInt(0, 100)); }

    @Test @DisplayName("ofInt(min, max, seed) returns IntGenerator")
    void ofIntSeeded() { assertInstanceOf(IntGenerator.class, Generators.ofInt(0, 100, 1L)); }

    // ── Long ──────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofLong() returns LongGenerator")
    void ofLong() { assertInstanceOf(LongGenerator.class, Generators.ofLong()); }

    @Test @DisplayName("ofLong(min, max) returns LongGenerator")
    void ofLongRange() { assertInstanceOf(LongGenerator.class, Generators.ofLong(0L, 100L)); }

    @Test @DisplayName("ofLong(min, max, seed) returns LongGenerator")
    void ofLongSeeded() { assertInstanceOf(LongGenerator.class, Generators.ofLong(0L, 100L, 1L)); }

    // ── Float ─────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofFloat() returns FloatGenerator")
    void ofFloat() { assertInstanceOf(FloatGenerator.class, Generators.ofFloat()); }

    @Test @DisplayName("ofFloat(min, max) returns FloatGenerator")
    void ofFloatRange() { assertInstanceOf(FloatGenerator.class, Generators.ofFloat(0f, 1f)); }

    @Test @DisplayName("ofFloat(min, max, seed) returns FloatGenerator")
    void ofFloatSeeded() { assertInstanceOf(FloatGenerator.class, Generators.ofFloat(0f, 1f, 1L)); }

    // ── Double ────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofDouble() returns DoubleGenerator")
    void ofDouble() { assertInstanceOf(DoubleGenerator.class, Generators.ofDouble()); }

    @Test @DisplayName("ofDouble(min, max) returns DoubleGenerator")
    void ofDoubleRange() { assertInstanceOf(DoubleGenerator.class, Generators.ofDouble(0.0, 1.0)); }

    @Test @DisplayName("ofDouble(min, max, seed) returns DoubleGenerator")
    void ofDoubleSeeded() { assertInstanceOf(DoubleGenerator.class, Generators.ofDouble(0.0, 1.0, 1L)); }

    // ── Natural ───────────────────────────────────────────────────────────────

    @Test @DisplayName("ofNaturalNumber() returns NaturalNumberGenerator")
    void ofNaturalNumber() { assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber()); }

    @Test @DisplayName("ofNaturalNumber(min, max) returns NaturalNumberGenerator")
    void ofNaturalNumberRange() { assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber(0, 100)); }

    @Test @DisplayName("ofNaturalNumber(min, max, seed) returns NaturalNumberGenerator")
    void ofNaturalNumberSeeded() { assertInstanceOf(NaturalNumberGenerator.class, Generators.ofNaturalNumber(0, 100, 1L)); }

    // ── Normal Distribution ───────────────────────────────────────────────────

    @Test @DisplayName("ofNormal() returns NormalDistributionGenerator")
    void ofNormal() { assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal()); }

    @Test @DisplayName("ofNormal(mean, stdDev) returns NormalDistributionGenerator")
    void ofNormalParams() { assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal(0.0, 1.0)); }

    @Test @DisplayName("ofNormal(mean, stdDev, seed) returns NormalDistributionGenerator")
    void ofNormalSeeded() { assertInstanceOf(NormalDistributionGenerator.class, Generators.ofNormal(0.0, 1.0, 1L)); }

    // ── Prime ─────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofPrime() returns PrimeGenerator")
    void ofPrime() { assertInstanceOf(PrimeGenerator.class, Generators.ofPrime()); }

    @Test @DisplayName("ofPrime(min, max) returns PrimeGenerator")
    void ofPrimeRange() { assertInstanceOf(PrimeGenerator.class, Generators.ofPrime(2, 100)); }

    @Test @DisplayName("ofPrime(min, max, seed) returns PrimeGenerator")
    void ofPrimeSeeded() { assertInstanceOf(PrimeGenerator.class, Generators.ofPrime(2, 100, 1L)); }

    // ── Char ──────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofChar() returns CharGenerator")
    void ofChar() { assertInstanceOf(CharGenerator.class, Generators.ofChar()); }

    @Test @DisplayName("ofChar(builder) returns the same builder")
    void ofCharBuilder() {
        CharGenerator.Builder builder = CharGenerator.builder().uppercase();
        assertSame(builder, Generators.ofChar(builder));
    }

    // ── Boolean ───────────────────────────────────────────────────────────────

    @Test @DisplayName("ofBoolean() returns BooleanGenerator")
    void ofBoolean() { assertInstanceOf(BooleanGenerator.class, Generators.ofBoolean()); }

    @Test @DisplayName("ofBoolean(seed) returns BooleanGenerator")
    void ofBooleanSeeded() { assertInstanceOf(BooleanGenerator.class, Generators.ofBoolean(1L)); }

    // ── String ────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofString() returns StringGenerator")
    void ofString() { assertInstanceOf(StringGenerator.class, Generators.ofString()); }

    @Test @DisplayName("ofString(builder) returns StringGenerator")
    void ofStringBuilder() {
        assertInstanceOf(StringGenerator.class,
                Generators.ofString(StringGenerator.builder().length(5)));
    }

    // ── Algorithms ────────────────────────────────────────────────────────────

    @Test @DisplayName("ofFibonacci() returns FibonacciGenerator")
    void ofFibonacci() { assertInstanceOf(FibonacciGenerator.class, Generators.ofFibonacci()); }

    @Test @DisplayName("ofLuhn() returns LuhnGenerator")
    void ofLuhn() { assertInstanceOf(LuhnGenerator.class, Generators.ofLuhn()); }

    // ── Selection / helper-style generators ──────────────────────────────────

    @Test @DisplayName("pickFrom(source) returns PickGenerator")
    void pickFrom() {
        assertInstanceOf(PickGenerator.class, Generators.pickFrom(List.of("a", "b")));
    }

    @Test @DisplayName("pickSetFrom(source, count) returns PickSetGenerator")
    void pickSetFrom() {
        assertInstanceOf(PickSetGenerator.class, Generators.pickSetFrom(List.of(1, 2, 3), 2));
    }

    @Test @DisplayName("shuffleOf(source) returns ShuffleGenerator")
    void shuffleOf() {
        assertInstanceOf(ShuffleGenerator.class, Generators.shuffleOf(List.of(1, 2, 3)));
    }

    @Test @DisplayName("weighted(values, weights) returns WeightedGenerator")
    void weighted() {
        assertInstanceOf(WeightedGenerator.class, Generators.weighted(List.of("h", "t"), List.of(7, 3)));
    }

    @Test @DisplayName("unique(source) returns UniqueGenerator")
    void unique() {
        assertInstanceOf(UniqueGenerator.class, Generators.unique(() -> 1));
    }

    @Test @DisplayName("unique(source, maxAttempts) returns UniqueGenerator")
    void uniqueMaxAttempts() {
        assertInstanceOf(UniqueGenerator.class, Generators.unique(() -> 1, 5));
    }

    @Test @DisplayName("repeat(source, count) returns RepeatGenerator")
    void repeat() {
        assertInstanceOf(RepeatGenerator.class, Generators.repeat(() -> 1, 3));
    }

    @Test @DisplayName("ofProfession() returns ProfessionGenerator")
    void ofProfession() {
        assertInstanceOf(ProfessionGenerator.class, Generators.ofProfession());
    }

    @Test @DisplayName("ofWord() returns WordGenerator")
    void ofWord() {
        assertInstanceOf(WordGenerator.class, Generators.ofWord());
    }

    @Test @DisplayName("ofSyllable() returns SyllableGenerator")
    void ofSyllable() {
        assertInstanceOf(SyllableGenerator.class, Generators.ofSyllable());
    }

    @Test @DisplayName("ofSentence() returns SentenceGenerator")
    void ofSentence() {
        assertInstanceOf(SentenceGenerator.class, Generators.ofSentence());
    }

    @Test @DisplayName("ofParagraph() returns ParagraphGenerator")
    void ofParagraph() {
        assertInstanceOf(ParagraphGenerator.class, Generators.ofParagraph());
    }

    @Test @DisplayName("ofFileExtension() returns FileExtensionGenerator")
    void ofFileExtension() {
        assertInstanceOf(FileExtensionGenerator.class, Generators.ofFileExtension());
    }

    @Test @DisplayName("ofFileName() returns FileNameGenerator")
    void ofFileName() {
        assertInstanceOf(FileNameGenerator.class, Generators.ofFileName());
    }

    // ── Games ─────────────────────────────────────────────────────────────────

    @Test @DisplayName("ofCoin() returns CoinGenerator")
    void ofCoin() { assertInstanceOf(CoinGenerator.class, Generators.ofCoin()); }

    @Test @DisplayName("ofDice(D6) returns DiceGenerator")
    void ofDice() { assertInstanceOf(DiceGenerator.class, Generators.ofDice(DiceType.D6)); }

    // ── Network ───────────────────────────────────────────────────────────────

    @Test @DisplayName("ofIPv4() returns IPv4Generator")
    void ofIPv4() { assertInstanceOf(IPv4Generator.class, Generators.ofIPv4()); }

    @Test @DisplayName("ofIPv6() returns IPv6Generator")
    void ofIPv6() { assertInstanceOf(IPv6Generator.class, Generators.ofIPv6()); }

    // ── forType ───────────────────────────────────────────────────────────────

    @Test @DisplayName("forType(Byte.class) returns a generator")
    void forTypeByte() { assertNotNull(Generators.forType(Byte.class).generate()); }

    @Test @DisplayName("forType(byte.class) returns a generator")
    void forTypeBytePrimitive() { assertNotNull(Generators.forType(byte.class).generate()); }

    @Test @DisplayName("forType(Short.class) returns a generator")
    void forTypeShort() { assertNotNull(Generators.forType(Short.class).generate()); }

    @Test @DisplayName("forType(short.class) returns a generator")
    void forTypeShortPrimitive() { assertNotNull(Generators.forType(short.class).generate()); }

    @Test @DisplayName("forType(Integer.class) returns a generator")
    void forTypeInteger() { assertNotNull(Generators.forType(Integer.class).generate()); }

    @Test @DisplayName("forType(int.class) returns a generator")
    void forTypeIntPrimitive() { assertNotNull(Generators.forType(int.class).generate()); }

    @Test @DisplayName("forType(Long.class) returns a generator")
    void forTypeLong() { assertNotNull(Generators.forType(Long.class).generate()); }

    @Test @DisplayName("forType(long.class) returns a generator")
    void forTypeLongPrimitive() { assertNotNull(Generators.forType(long.class).generate()); }

    @Test @DisplayName("forType(Float.class) returns a generator")
    void forTypeFloat() { assertNotNull(Generators.forType(Float.class).generate()); }

    @Test @DisplayName("forType(float.class) returns a generator")
    void forTypeFloatPrimitive() { assertNotNull(Generators.forType(float.class).generate()); }

    @Test @DisplayName("forType(Double.class) returns a generator")
    void forTypeDouble() { assertNotNull(Generators.forType(Double.class).generate()); }

    @Test @DisplayName("forType(double.class) returns a generator")
    void forTypeDoublePrimitive() { assertNotNull(Generators.forType(double.class).generate()); }

    @Test @DisplayName("forType(Character.class) returns a generator")
    void forTypeCharacter() { assertNotNull(Generators.forType(Character.class).generate()); }

    @Test @DisplayName("forType(char.class) returns a generator")
    void forTypeCharPrimitive() { assertNotNull(Generators.forType(char.class).generate()); }

    @Test @DisplayName("forType(Boolean.class) returns a generator")
    void forTypeBoolean() { assertNotNull(Generators.forType(Boolean.class).generate()); }

    @Test @DisplayName("forType(boolean.class) returns a generator")
    void forTypeBooleanPrimitive() { assertNotNull(Generators.forType(boolean.class).generate()); }

    @Test @DisplayName("forType(String.class) returns a generator")
    void forTypeString() { assertNotNull(Generators.forType(String.class).generate()); }

    @Test @DisplayName("forType(null) throws NullPointerException")
    void forTypeNullThrows() {
        assertThrows(NullPointerException.class, () -> Generators.forType(null));
    }

    @Test @DisplayName("forType(unknown type) throws IllegalArgumentException")
    void forTypeUnknownThrows() {
        assertThrows(IllegalArgumentException.class, () -> Generators.forType(Object.class));
    }
}
