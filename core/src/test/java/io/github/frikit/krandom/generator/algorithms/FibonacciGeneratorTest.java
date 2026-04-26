/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.algorithms;

import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FibonacciGenerator")
class FibonacciGeneratorTest {

    private static final int SAMPLES = 200;

    /**
     * All 93 Fibonacci numbers that fit in a signed 64-bit long (indices 0–92).
     */
    private static final List<Long> FULL_FIBONACCI = List.of(
        0L,
        1L,
        1L,
        2L,
        3L,
        5L,
        8L,
        13L,
        21L,
        34L,
        55L,
        89L,
        144L,
        233L,
        377L,
        610L,
        987L,
        1597L,
        2584L,
        4181L,
        6765L,
        10946L,
        17711L,
        28657L,
        46368L,
        75025L,
        121393L,
        196418L,
        317811L,
        514229L,
        832040L,
        1346269L,
        2178309L,
        3524578L,
        5702887L,
        9227465L,
        14930352L,
        24157817L,
        39088169L,
        63245986L,
        102334155L,
        165580141L,
        267914296L,
        433494437L,
        701408733L,
        1134903170L,
        1836311903L,
        2971215073L,
        4807526976L,
        7778742049L,
        12586269025L,
        20365011074L,
        32951280099L,
        53316291173L,
        86267571272L,
        139583862445L,
        225851433717L,
        365435296162L,
        591286729879L,
        956722026041L,
        1548008755920L,
        2504730781961L,
        4052739537881L,
        6557470319842L,
        10610209857723L,
        17167680177565L,
        27777890035288L,
        44945570212853L,
        72723460248141L,
        117669030460994L,
        190392490709135L,
        308061521170129L,
        498454011879264L,
        806515533049393L,
        1304969544928657L,
        2111485077978050L,
        3416454622906707L,
        5527939700884757L,
        8944394323791464L,
        14472334024676221L,
        23416728348467685L,
        37889062373143906L,
        61305790721611591L,
        99194853094755497L,
        160500643816367088L,
        259695496911122585L,
        420196140727489673L,
        679891637638612258L,
        1100087778366101931L,
        1779979416004714189L,
        2880067194370816120L,
        4660046610375530309L,
        7540113804746346429L
    );

    @Test
    @DisplayName("generate() always returns a value in the known Fibonacci set")
    void generateReturnsKnownFibonacci() {
        FibonacciGenerator gen = new FibonacciGenerator();
        Set<Long> known = new HashSet<>(FULL_FIBONACCI);
        for (int i = 0; i < SAMPLES; i++) {
            assertTrue(known.contains(gen.generate()),
                       "generate() returned a value not in the Fibonacci set");
        }
    }

    @Test
    @DisplayName("generate() produces variety across samples")
    void generateProducesVariety() {
        FibonacciGenerator gen = new FibonacciGenerator();
        Set<Long> seen = new HashSet<>();
        for (int i = 0; i < SAMPLES; i++) seen.add(gen.generate());
        assertTrue(seen.size() > 1, "Expected distinct values over " + SAMPLES + " samples");
    }

    @Test
    @DisplayName("getNumber returns correct values at first indices [0,5]")
    void getNumberFirstIndices() {
        FibonacciGenerator gen = new FibonacciGenerator();
        long[] expected = { 0, 1, 1, 2, 3, 5 };
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], gen.getNumber(i), "Index " + i);
        }
    }

    @Test
    @DisplayName("getNumber returns correct values at last indices [89,92]")
    void getNumberLastIndices() {
        FibonacciGenerator gen = new FibonacciGenerator();
        assertEquals(1779979416004714189L, gen.getNumber(89));
        assertEquals(2880067194370816120L, gen.getNumber(90));
        assertEquals(4660046610375530309L, gen.getNumber(91));
        assertEquals(7540113804746346429L, gen.getNumber(92));
    }

    @Test
    @DisplayName("getNumber(-1) throws IllegalArgumentException")
    void getNumberNegativeIndexThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> new FibonacciGenerator().getNumber(-1));
    }

    @Test
    @DisplayName("getNumber(93) throws IllegalArgumentException")
    void getNumberIndexTooHighThrows() {
        assertThrows(IllegalArgumentException.class,
                     () -> new FibonacciGenerator().getNumber(93));
    }

    @Test
    @DisplayName("generateSequence() returns the complete 93-element sequence")
    void generateSequenceFullDefault() {
        List<Long> seq = new FibonacciGenerator().generateSequence();
        assertEquals(FULL_FIBONACCI, seq);
    }

    @Test
    @DisplayName("generateSequence(1, Long.MAX_VALUE) omits the leading zero")
    void generateSequenceFromOne() {
        List<Long> seq = new FibonacciGenerator().generateSequence(1L, Long.MAX_VALUE);
        assertEquals(FULL_FIBONACCI.subList(1, FULL_FIBONACCI.size()), seq);
    }

    @Test
    @DisplayName("generateSequence(1, -1) returns empty list when tillNumber < from")
    void generateSequenceNoMatch() {
        List<Long> seq = new FibonacciGenerator().generateSequence(1L, -1L);
        assertTrue(seq.isEmpty(), "Expected empty sequence when tillNumber < from");
    }

    @Test
    @DisplayName("generateSequence(0, 10) contains only Fibonacci numbers <= 10")
    void generateSequenceBounded() {
        List<Long> seq = new FibonacciGenerator().generateSequence(0L, 10L);
        assertEquals(List.of(0L, 1L, 1L, 2L, 3L, 5L, 8L), seq);
    }

    @Test
    @DisplayName("generateList returns correct count of random Fibonacci numbers")
    void generateList() {
        List<Long> list = new FibonacciGenerator().generateList(15);
        assertEquals(15, list.size());
        Set<Long> known = new HashSet<>(FULL_FIBONACCI);
        list.forEach(v -> assertTrue(known.contains(v)));
    }

    @Test
    @DisplayName("stream produces on-demand Fibonacci values")
    void streamUsage() {
        Set<Long> known = new HashSet<>(FULL_FIBONACCI);
        new FibonacciGenerator().stream().limit(50)
                                .forEach(v -> assertTrue(known.contains(v), "Unexpected value in stream: " + v));
    }

    @Test
    @DisplayName("Generators.ofFibonacci() factory returns a FibonacciGenerator")
    void factoryMethod() {
        assertInstanceOf(FibonacciGenerator.class, Generators.ofFibonacci());
    }
}
