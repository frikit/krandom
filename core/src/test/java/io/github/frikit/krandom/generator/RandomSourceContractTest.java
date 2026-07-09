/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.object.ObjectGenerationSemanticMode;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.TextFormatProvider;
import io.github.frikit.krandom.generator.schema.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
