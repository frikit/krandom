/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectGenerator — generic arrays")
class ObjectGeneratorGenericArrayTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                                  .seed(2211L)
                                                                  .collectionSize(2, 2)
                                                                  .build();

    @Test
    @DisplayName("inherited scalar array uses its concrete component binding")
    void inheritedScalarArrayUsesConcreteBinding() {
        StringArrayFixture value = new ObjectGenerator<>(StringArrayFixture.class, CONFIG).generate();

        assertEquals(String[].class, value.values.getClass());
        assertEquals(2, value.values.length);
        for (String item : value.values) {
            assertInstanceOf(String.class, item);
        }
    }

    @Test
    @DisplayName("inherited parameterized array retains nested element arguments")
    void inheritedParameterizedArrayRetainsNestedArguments() {
        IntegerListArrayFixture value = new ObjectGenerator<>(IntegerListArrayFixture.class, CONFIG).generate();

        assertEquals(List[].class, value.values.getClass());
        assertEquals(2, value.values.length);
        for (List<Integer> list : value.values) {
            assertEquals(2, list.size());
            list.forEach(item -> assertInstanceOf(Integer.class, item));
        }
    }

    @Test
    @DisplayName("declared parameterized array retains its component signature")
    void declaredParameterizedArrayRetainsComponentSignature() {
        ParameterizedArrayFixture value = new ObjectGenerator<>(ParameterizedArrayFixture.class, CONFIG).generate();

        assertEquals(2, value.values.length);
        for (List<String> list : value.values) {
            assertEquals(2, list.size());
            list.forEach(item -> assertInstanceOf(String.class, item));
        }
    }

    @Test
    @DisplayName("record parameterized array follows the same recursive type path")
    void recordParameterizedArrayUsesRecursiveType() {
        ParameterizedArrayRecord value = new ObjectGenerator<>(ParameterizedArrayRecord.class, CONFIG).generate();

        assertEquals(2, value.values().length);
        for (List<Integer> list : value.values()) {
            assertEquals(2, list.size());
            list.forEach(item -> assertInstanceOf(Integer.class, item));
        }
    }

    @Test
    @DisplayName("unbound array component fails at the parent path with its full signature")
    void unboundArrayComponentFailsAtParentPath() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnboundArrayFixture.class, CONFIG).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("UnboundArrayFixture.values", context.path());
        assertEquals("T[]", context.declaredType());
    }

    @Test
    @DisplayName("lenient unbound array handling discards the whole array and emits context")
    void lenientUnboundArrayDiscardsWholeValue() {
        AtomicReference<GenerationFailureDiagnostic> observed = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::set)
                                                .build();

        UnboundArrayFixture<?> value = new ObjectGenerator<>(UnboundArrayFixture.class, config).generate();

        assertNull(value.values);
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, observed.get().context().category());
        assertEquals("UnboundArrayFixture.values", observed.get().context().path());
        assertEquals("T[]", observed.get().context().declaredType());
    }

    static class GenericArrayFixture<T> {

        T[] values;
    }

    static final class StringArrayFixture extends GenericArrayFixture<String> {
    }

    static class NestedArrayFixture<U> extends GenericArrayFixture<List<U>> {
    }

    static final class IntegerListArrayFixture extends NestedArrayFixture<Integer> {
    }

    static final class ParameterizedArrayFixture {

        List<String>[] values;
    }

    record ParameterizedArrayRecord(List<Integer>[] values) {
    }

    static final class UnboundArrayFixture<T> {

        T[] values;
    }
}
