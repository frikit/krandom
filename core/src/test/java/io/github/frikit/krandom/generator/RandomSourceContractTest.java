/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.object.FakeRange;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.TextFormatProvider;
import io.github.frikit.krandom.generator.schema.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("random source contract across generator families")
class RandomSourceContractTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("generatorFamilies")
    @DisplayName("each family consumes the configured caller-owned source")
    void callerOwnedSourceIsConsumed(String family, Consumer<GeneratorConfig> generate) {
        TrackingRandom random = new TrackingRandom(42L);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .random(random)
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .build();

        generate.accept(config);

        assertTrue(random.drawCount() > 0, family + " did not consume the configured Random");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objectBuiltinTypes")
    @DisplayName("object built-ins consume the configured caller-owned source")
    void objectBuiltinsConsumeCallerOwnedSource(String type, Consumer<GeneratorConfig> generate) {
        TrackingRandom random = new TrackingRandom(42L);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .random(random)
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .build();

        generate.accept(config);

        assertTrue(random.drawCount() > 0, type + " did not consume the configured Random");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("objectSpecialPaths")
    @DisplayName("object special paths consume the configured caller-owned source")
    void objectSpecialPathsConsumeCallerOwnedSource(String path, Consumer<GeneratorConfig> generate) {
        TrackingRandom random = new TrackingRandom(42L);
        GeneratorConfig config = GeneratorConfig.builder()
                                                .random(random)
                                                .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                                                .build();

        generate.accept(config);

        assertTrue(random.drawCount() > 0, path + " did not consume the configured Random");
    }

    @Test
    @DisplayName("shared Random stays coherent but logical replay follows concurrent call order")
    void sharedRandomIsSafeButReplayDependsOnCallOrder() throws Exception {
        int callCount = 64;
        GeneratorConfig expectedConfig = GeneratorConfig.builder().random(new Random(42L)).build();
        List<String> expected = new DigitGenerator(expectedConfig).generateList(callCount);

        GeneratorConfig concurrentConfig = GeneratorConfig.builder().random(new Random(42L)).build();
        DigitGenerator shared = new DigitGenerator(concurrentConfig);
        CountDownLatch ready = new CountDownLatch(callCount);
        List<CountDownLatch> permits = new ArrayList<>(callCount);
        for (int i = 0; i < callCount; i++) {
            permits.add(new CountDownLatch(1));
        }

        List<String> observed = new ArrayList<>(Collections.nCopies(callCount, null));
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> calls = new ArrayList<>(callCount);
            for (int i = 0; i < callCount; i++) {
                int logicalIndex = i;
                calls.add(executor.submit(() -> {
                    ready.countDown();
                    permits.get(logicalIndex).await();
                    return shared.generate();
                }));
            }

            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS), "concurrent callers did not become ready");
                for (int i = callCount - 1; i >= 0; i--) {
                    permits.get(i).countDown();
                    observed.set(i, calls.get(i).get(5, TimeUnit.SECONDS));
                }
            } finally {
                permits.forEach(CountDownLatch::countDown);
            }
        }

        List<String> expectedInReverseCallOrder = new ArrayList<>(expected);
        Collections.reverse(expectedInReverseCallOrder);
        assertEquals(expectedInReverseCallOrder, observed);
    }

    private static Stream<Arguments> generatorFamilies() {
        return Stream.of(
            Arguments.of("scalar", (Consumer<GeneratorConfig>) config -> new DigitGenerator(config).generate()),
            Arguments.of("object", (Consumer<GeneratorConfig>) config ->
                new ObjectGenerator<>(IntFixture.class, config).generate()),
            Arguments.of("schema", (Consumer<GeneratorConfig>) config ->
                new Schema(config, Map.of("value", context -> context.random().nextInt())).generate()),
            Arguments.of("provider", (Consumer<GeneratorConfig>) config ->
                new TextFormatProvider(config).hexify("^")));
    }

    private static Stream<Arguments> objectBuiltinTypes() {
        return Stream.of(
            objectType("byte", ByteFixture.class),
            objectType("short", ShortFixture.class),
            objectType("int", IntFixture.class),
            objectType("long", LongFixture.class),
            objectType("float", FloatFixture.class),
            objectType("double", DoubleFixture.class),
            objectType("char", CharFixture.class),
            objectType("boolean", BooleanFixture.class),
            objectType("String", StringFixture.class),
            objectType("BigDecimal", BigDecimalFixture.class),
            objectType("BigInteger", BigIntegerFixture.class));
    }

    private static Arguments objectType(String name, Class<?> type) {
        return Arguments.of(name, (Consumer<GeneratorConfig>) config -> new ObjectGenerator<>(type, config).generate());
    }

    private static Stream<Arguments> objectSpecialPaths() {
        return Stream.of(
            objectType("enum", EnumFixture.class),
            objectType("@FakeRange byte", RangedByteFixture.class),
            objectType("@FakeRange short", RangedShortFixture.class),
            objectType("@FakeRange int", RangedIntFixture.class),
            objectType("@FakeRange long", RangedLongFixture.class),
            objectType("@FakeRange float", RangedFloatFixture.class),
            objectType("@FakeRange double", RangedDoubleFixture.class),
            rangedTemporalType("LocalDate", LocalDateFixture.class),
            rangedTemporalType("LocalDateTime", LocalDateTimeFixture.class),
            rangedTemporalType("Instant", InstantFixture.class),
            rangedTemporalType("ZonedDateTime", ZonedDateTimeFixture.class),
            rangedTemporalType("Date", DateFixture.class),
            rangedTemporalType("SQL Date", SqlDateFixture.class),
            rangedTemporalType("SQL Timestamp", SqlTimestampFixture.class));
    }

    private static Arguments rangedTemporalType(String name, Class<?> type) {
        return Arguments.of(name, (Consumer<GeneratorConfig>) config -> {
            GeneratorConfig ranged = config.toBuilder()
                                           .objectDateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31))
                                           .build();
            new ObjectGenerator<>(type, ranged).generate();
        });
    }

    private record ByteFixture(byte value) {
    }

    private record ShortFixture(short value) {
    }

    private record IntFixture(int number) {
    }

    private record LongFixture(long value) {
    }

    private record FloatFixture(float value) {
    }

    private record DoubleFixture(double value) {
    }

    private record CharFixture(char value) {
    }

    private record BooleanFixture(boolean value) {
    }

    private record StringFixture(String value) {
    }

    private record BigDecimalFixture(BigDecimal value) {
    }

    private record BigIntegerFixture(BigInteger value) {
    }

    private record EnumFixture(SourceStatus value) {
    }

    private record RangedByteFixture(@FakeRange(min = 1, max = 10) byte value) {
    }

    private record RangedShortFixture(@FakeRange(min = 1, max = 10) short value) {
    }

    private record RangedIntFixture(@FakeRange(min = 1, max = 10) int value) {
    }

    private record RangedLongFixture(@FakeRange(min = 1, max = 10) long value) {
    }

    private record RangedFloatFixture(@FakeRange(min = 1, max = 10) float value) {
    }

    private record RangedDoubleFixture(@FakeRange(min = 1, max = 10) double value) {
    }

    private record LocalDateFixture(LocalDate value) {
    }

    private record LocalDateTimeFixture(LocalDateTime value) {
    }

    private record InstantFixture(Instant value) {
    }

    private record ZonedDateTimeFixture(ZonedDateTime value) {
    }

    private record DateFixture(java.util.Date value) {
    }

    private record SqlDateFixture(java.sql.Date value) {
    }

    private record SqlTimestampFixture(java.sql.Timestamp value) {
    }

    private enum SourceStatus {
        FIRST,
        SECOND
    }

    private static final class TrackingRandom extends Random {

        private int drawCount;

        private TrackingRandom(long seed) {
            super(seed);
        }

        @Override
        protected int next(int bits) {
            drawCount++;
            return super.next(bits);
        }

        private int drawCount() {
            return drawCount;
        }
    }
}
