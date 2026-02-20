/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.algorithms;

import org.github.krandom.generator.Generator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * A {@link Generator} that produces random Fibonacci numbers.
 *
 * <p>The precomputed sequence covers all 93 Fibonacci numbers that fit in a signed 64-bit long
 * (indices 0–92, from {@code 0} up to {@code 7540113804746346429}).
 *
 * <p>In addition to the random {@link #generate()} method, this class exposes:
 * <ul>
 *   <li>{@link #getNumber(int)} — retrieve the Fibonacci number at a specific index.</li>
 *   <li>{@link #generateSequence(long, long)} — produce a Fibonacci sequence within a range.</li>
 *   <li>{@link #generateSequence()} — produce the complete sequence.</li>
 * </ul>
 */
public final class FibonacciGenerator implements Generator<Long> {

    /** All Fibonacci numbers that fit in a {@code long}, indices 0–92. */
    private static final List<Long> FIBONACCI_SEQUENCE;

    static {
        List<Long> seq = new ArrayList<>(93);
        seq.add(0L); seq.add(1L); seq.add(1L); seq.add(2L); seq.add(3L);
        seq.add(5L); seq.add(8L); seq.add(13L); seq.add(21L); seq.add(34L);
        seq.add(55L); seq.add(89L); seq.add(144L); seq.add(233L); seq.add(377L);
        seq.add(610L); seq.add(987L); seq.add(1597L); seq.add(2584L); seq.add(4181L);
        seq.add(6765L); seq.add(10946L); seq.add(17711L); seq.add(28657L); seq.add(46368L);
        seq.add(75025L); seq.add(121393L); seq.add(196418L); seq.add(317811L); seq.add(514229L);
        seq.add(832040L); seq.add(1346269L); seq.add(2178309L); seq.add(3524578L); seq.add(5702887L);
        seq.add(9227465L); seq.add(14930352L); seq.add(24157817L); seq.add(39088169L); seq.add(63245986L);
        seq.add(102334155L); seq.add(165580141L); seq.add(267914296L); seq.add(433494437L); seq.add(701408733L);
        seq.add(1134903170L); seq.add(1836311903L); seq.add(2971215073L); seq.add(4807526976L); seq.add(7778742049L);
        seq.add(12586269025L); seq.add(20365011074L); seq.add(32951280099L); seq.add(53316291173L); seq.add(86267571272L);
        seq.add(139583862445L); seq.add(225851433717L); seq.add(365435296162L); seq.add(591286729879L); seq.add(956722026041L);
        seq.add(1548008755920L); seq.add(2504730781961L); seq.add(4052739537881L); seq.add(6557470319842L); seq.add(10610209857723L);
        seq.add(17167680177565L); seq.add(27777890035288L); seq.add(44945570212853L); seq.add(72723460248141L); seq.add(117669030460994L);
        seq.add(190392490709135L); seq.add(308061521170129L); seq.add(498454011879264L); seq.add(806515533049393L); seq.add(1304969544928657L);
        seq.add(2111485077978050L); seq.add(3416454622906707L); seq.add(5527939700884757L); seq.add(8944394323791464L); seq.add(14472334024676221L);
        seq.add(23416728348467685L); seq.add(37889062373143906L); seq.add(61305790721611591L); seq.add(99194853094755497L); seq.add(160500643816367088L);
        seq.add(259695496911122585L); seq.add(420196140727489673L); seq.add(679891637638612258L); seq.add(1100087778366101931L); seq.add(1779979416004714189L);
        seq.add(2880067194370816120L); seq.add(4660046610375530309L); seq.add(7540113804746346429L);
        FIBONACCI_SEQUENCE = Collections.unmodifiableList(seq);
    }

    private final RandomGenerator random;

    /** Creates a generator backed by {@link SecureRandom}. */
    public FibonacciGenerator() {
        this.random = new SecureRandom();
    }

    /**
     * Returns a uniformly random element from the precomputed Fibonacci sequence.
     *
     * @return a random Fibonacci number in the range [0, 7540113804746346429]
     */
    @Override
    public Long generate() {
        return FIBONACCI_SEQUENCE.get(random.nextInt(FIBONACCI_SEQUENCE.size()));
    }

    /**
     * Returns the Fibonacci number at the given index.
     *
     * @param index index in [0, 92]
     * @throws IllegalArgumentException if the index is out of range
     */
    public long getNumber(int index) {
        if (index < 0) throw new IllegalArgumentException("Index should be >= 0!");
        if (index > 92) throw new IllegalArgumentException("Index should be <= 92!");
        return FIBONACCI_SEQUENCE.get(index);
    }

    /**
     * Generates a Fibonacci sequence starting at {@code from} and stopping when the current
     * value exceeds {@code tillNumber}.
     *
     * @param from       the starting value of the sequence
     * @param tillNumber the inclusive upper bound; values greater than this are excluded
     * @return an immutable list of Fibonacci numbers within the requested range
     */
    public List<Long> generateSequence(long from, long tillNumber) {
        List<Long> result = new ArrayList<>();
        long current = from;
        long next = 1;
        while (current <= tillNumber) {
            result.add(current);
            if (current > next) {
                break;
            }
            long sum = current + next;
            current = next;
            next = sum;
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Generates the complete Fibonacci sequence (all 93 values that fit in a {@code long}).
     *
     * @return an immutable list of all 93 Fibonacci numbers starting from 0
     */
    public List<Long> generateSequence() {
        return generateSequence(0L, Long.MAX_VALUE);
    }
}
