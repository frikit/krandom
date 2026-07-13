/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ObjectGenerator — parameterized nested objects")
class ObjectGeneratorParameterizedObjectTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                                  .seed(2212L)
                                                                  .collectionSize(2, 2)
                                                                  .build();

    @Test
    @DisplayName("mutable fields retain nested class and record bindings")
    void mutableFieldsRetainNestedObjectBindings() {
        ParameterizedFields value = new ObjectGenerator<>(ParameterizedFields.class, CONFIG).generate();

        assertInstanceOf(String.class, value.text.value);
        assertEquals(2, value.nested.value.size());
        value.nested.value.forEach(item -> assertInstanceOf(Integer.class, item));
        assertInstanceOf(String.class, value.record.value());
        value.record.values().forEach(item -> assertInstanceOf(String.class, item));
        assertEquals(2, value.boxes.size());
        value.boxes.forEach(box -> assertInstanceOf(Long.class, box.value));
    }

    @Test
    @DisplayName("record components retain nested object bindings")
    void recordComponentsRetainNestedObjectBindings() {
        ParameterizedRecord value = new ObjectGenerator<>(ParameterizedRecord.class, CONFIG).generate();

        assertInstanceOf(Integer.class, value.box().value);
        assertEquals(2, value.boxes().size());
        value.boxes().forEach(box -> assertInstanceOf(String.class, box.value));
    }

    @Test
    @DisplayName("inherited variables flow into a parameterized child")
    void inheritedVariableFlowsIntoChild() {
        StringHolder value = new ObjectGenerator<>(StringHolder.class, CONFIG).generate();

        assertInstanceOf(String.class, value.box.value);
        value.box.values.forEach(item -> assertInstanceOf(String.class, item));
    }

    @Test
    @DisplayName("unbounded child argument fails at the parent with its full signature")
    void unboundedChildArgumentFailsAtParent() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnboundedHolder.class, CONFIG).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("UnboundedHolder.box", context.path());
        assertEquals(Box.class.getName() + "<?>", context.declaredType());
    }

    @Test
    @DisplayName("raw generic child fails at the parent boundary")
    void rawGenericChildFailsAtParent() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(RawHolder.class, CONFIG).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("RawHolder.box", context.path());
        assertEquals(Box.class.getName(), context.declaredType());
    }

    static final class Box<T> {

        T value;
        List<T> values;
    }

    record GenericRecord<T>(T value, List<T> values) {
    }

    static final class ParameterizedFields {

        Box<String> text;
        Box<List<Integer>> nested;
        GenericRecord<String> record;
        List<Box<Long>> boxes;
    }

    record ParameterizedRecord(Box<Integer> box, List<Box<String>> boxes) {
    }

    static class GenericHolder<T> {

        Box<T> box;
    }

    static final class StringHolder extends GenericHolder<String> {
    }

    static final class UnboundedHolder {

        Box<?> box;
    }

    @SuppressWarnings("rawtypes")
    static final class RawHolder {

        Box box;
    }
}
