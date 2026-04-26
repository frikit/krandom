/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.benchmarks;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.user.FirstNameGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class FirstNameGeneratorBenchmark {

    @State(Scope.Benchmark)
    public static class LocaleState {
        @Param({
            "en_US",
            "en_GB",
            "en_AU",
            "fr_FR",
            "de_DE",
            "ja_JP",
            "es_ES",
            "it_IT",
            "pt_BR",
            "zh_CN"
        })
        public String localeTag;

        public FirstNameGenerator generator;

        @Setup(Level.Trial)
        public void setUp() {
            String[] parts = localeTag.split("_");
            Locale locale = Locale.of(parts[0], parts[1]);
            generator = new FirstNameGenerator(
                GeneratorConfig.builder().locale(locale).seed(7L).build()
            );
        }
    }

    @Benchmark
    public String firstName(LocaleState state) {
        return state.generator.generate();
    }
}
