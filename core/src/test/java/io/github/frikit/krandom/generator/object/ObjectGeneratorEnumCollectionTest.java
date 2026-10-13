/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("ObjectGenerator — EnumSet and EnumMap members")
class ObjectGeneratorEnumCollectionTest {

    enum Color {
        RED,
        GREEN,
        BLUE
    }

    record EnumSetRecord(EnumSet<Color> colors) {}

    record EnumMapRecord(EnumMap<Color, Integer> byColor) {}

    static class EnumCollectionsHolder {

        EnumSet<Color> colors;
        EnumMap<Color, String> labels;
    }

    /** A raw {@code Enum} bound resolves to {@code Enum.class}, which names no constants. */
    @SuppressWarnings("rawtypes")
    static class WildcardEnumSetHolder {

        EnumSet<? extends Enum> values;
    }

    @SuppressWarnings("rawtypes")
    static class WildcardEnumMapHolder {

        EnumMap<? extends Enum, String> values;
    }

    private static GeneratorConfig config(long seed, List<GenerationFailureDiagnostic> diagnostics) {
        return GeneratorConfig.builder()
                .seed(seed)
                .collectionSize(1, 3)
                .generationFailureListener(diagnostics::add)
                .build();
    }

    @Test
    @DisplayName("record components receive populated EnumSet and EnumMap instances")
    void recordComponentsReceiveEnumCollections() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = config(42L, diagnostics);

        EnumSetRecord set = new ObjectGenerator<>(EnumSetRecord.class, config).generate();
        EnumMapRecord map = new ObjectGenerator<>(EnumMapRecord.class, config).generate();

        assertNotNull(set.colors());
        assertFalse(set.colors().isEmpty());
        assertTrue(set.colors().stream().allMatch(Color.class::isInstance));
        assertNotNull(map.byColor());
        assertFalse(map.byColor().isEmpty());
        assertTrue(map.byColor().keySet().stream().allMatch(Color.class::isInstance));
        assertTrue(map.byColor().values().stream().allMatch(Integer.class::isInstance));
        assertTrue(diagnostics.isEmpty(), () -> "unexpected diagnostics: " + diagnostics);
    }

    @Test
    @DisplayName("class fields receive populated EnumSet and EnumMap instances")
    void classFieldsReceiveEnumCollections() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        EnumCollectionsHolder holder =
                new ObjectGenerator<>(EnumCollectionsHolder.class, config(7L, diagnostics)).generate();

        assertNotNull(holder.colors);
        assertFalse(holder.colors.isEmpty());
        assertNotNull(holder.labels);
        assertFalse(holder.labels.isEmpty());
        assertTrue(holder.labels.values().stream().allMatch(String.class::isInstance));
        assertTrue(diagnostics.isEmpty(), () -> "unexpected diagnostics: " + diagnostics);
    }

    @Test
    @DisplayName("seeded EnumSet and EnumMap members are reproducible")
    void seededEnumCollectionsAreReproducible() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        EnumCollectionsHolder first =
                new ObjectGenerator<>(EnumCollectionsHolder.class, config(99L, diagnostics)).generate();
        EnumCollectionsHolder second =
                new ObjectGenerator<>(EnumCollectionsHolder.class, config(99L, diagnostics)).generate();

        assertEquals(first.colors, second.colors);
        assertEquals(first.labels, second.labels);
    }

    /**
     * The map is created before its entries, so its raw bound fails at construction. The set's
     * elements are generated first, so in strict mode the {@code Enum} element type is reported as
     * unsupported before the set is built. Either way the failure is contextual, never a raw
     * exception from {@code EnumSet.noneOf} or the {@code EnumMap} constructor.
     */
    @ParameterizedTest(name = "{2}")
    @MethodSource("wildcardHolders")
    @DisplayName("a raw enum bound is a contextual failure in strict mode")
    void wildcardEnumBoundFailsContextually(
            String strictPath, GenerationFailureCategory strictCategory, Class<?> holderType) {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        ObjectGenerationException error = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectGenerator<>(holderType, config(1L, diagnostics)).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(strictCategory, context.category());
        assertEquals(strictPath, context.path());
        assertEquals(1, diagnostics.size());
    }

    @ParameterizedTest(name = "{2}")
    @MethodSource("wildcardHolders")
    @DisplayName("a raw enum bound resolves to null and reports a construction failure in lenient mode")
    void wildcardEnumBoundIsNullWhenLenient(
            String strictPath, GenerationFailureCategory strictCategory, Class<?> holderType) throws Exception {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                .seed(1L)
                .objectIgnoreErrors(true)
                .generationFailureListener(diagnostics::add)
                .build();

        Object holder = new ObjectGenerator<>(holderType, config).generate();
        var field = holderType.getDeclaredField("values");
        field.setAccessible(true);

        assertNull(field.get(holder));
        GenerationFailureDiagnostic last = diagnostics.get(diagnostics.size() - 1);
        assertEquals(holderType.getSimpleName() + ".values", last.context().path());
        assertEquals(GenerationFailureCategory.CONSTRUCTION, last.context().category());
        assertEquals(GenerationOperation.CONSTRUCT, last.context().operation());
        assertTrue(String.valueOf(last.causeType()).contains("IllegalArgumentException"), String.valueOf(last));
    }

    private static Stream<Arguments> wildcardHolders() {
        return Stream.of(
                Arguments.of(
                        "WildcardEnumMapHolder.values",
                        GenerationFailureCategory.CONSTRUCTION,
                        WildcardEnumMapHolder.class),
                Arguments.of(
                        "WildcardEnumSetHolder.values[]",
                        GenerationFailureCategory.UNSUPPORTED_TYPE,
                        WildcardEnumSetHolder.class));
    }
}
