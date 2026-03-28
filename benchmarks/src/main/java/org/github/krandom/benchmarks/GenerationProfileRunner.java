/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.benchmarks;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.github.krandom.generator.base.RegexGenerator;
import org.github.krandom.generator.user.FirstNameGenerator;

import java.util.Locale;

/**
 * Macro profiling utility for large-scale generation loops.
 *
 * <p>Designed for CI-optional runs and local performance tracking.
 */
public final class GenerationProfileRunner {

    private static final int[] RUN_SIZES = { 100_000, 1_000_000 };

    private GenerationProfileRunner() {
    }

    public static void main(String[] args) {
        System.out.println("kRandom macro profile");
        System.out.println("====================");

        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.US)
                                                .seed(7L)
                                                .build();

        profileCase("first-name", new FirstNameGenerator(config));
        profileCase("regex-ssn", new RegexGenerator("\\d{3}-\\d{2}-\\d{4}", 7L));
        profileCase("object-simple-user", Generators.ofObject(BenchmarkFixtures.SimpleUser.class));
    }

    private static void profileCase(String label, Generator<?> generator) {
        System.out.println();
        System.out.println("Case: " + label);
        System.out.printf("%-10s %-14s %-14s%n", "count", "ops/sec", "heap-delta-mb");

        for (int runSize : RUN_SIZES) {
            // Warmup to reduce first-run noise.
            for (int i = 0; i < 10_000; i++) {
                generator.generate();
            }

            long beforeMemory = usedMemoryBytes();
            long started = System.nanoTime();
            for (int i = 0; i < runSize; i++) {
                generator.generate();
            }
            long elapsedNanos = System.nanoTime() - started;
            long afterMemory = usedMemoryBytes();

            double opsPerSecond = runSize / (elapsedNanos / 1_000_000_000.0);
            double heapDeltaMb = (afterMemory - beforeMemory) / (1024.0 * 1024.0);
            System.out.printf("%-10d %-14.2f %-14.2f%n", runSize, opsPerSecond, heapDeltaMb);
        }
    }

    private static long usedMemoryBytes() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}
