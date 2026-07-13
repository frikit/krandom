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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — inherited generic bindings")
class ObjectGeneratorInheritedGenericTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
                                                                  .seed(2210L)
                                                                  .collectionSize(2, 2)
                                                                  .build();

    @Test
    @DisplayName("concrete superclass binding applies to direct and container fields")
    void concreteSuperclassBindingAppliesEverywhere() {
        StringFixture value = new ObjectGenerator<>(StringFixture.class, CONFIG).generate();

        assertInstanceOf(String.class, value.direct);
        assertEquals(2, value.values.size());
        value.values.forEach(item -> assertInstanceOf(String.class, item));
        assertEquals(2, value.mapped.size());
        value.mapped.values().forEach(item -> assertInstanceOf(String.class, item));
        assertTrue(value.optional.isPresent());
        assertInstanceOf(String.class, value.optional.orElseThrow());
    }

    @Test
    @DisplayName("multi-level binding retains its nested parameterized type")
    void multiLevelBindingRetainsNestedType() {
        IntegerListFixture value = new ObjectGenerator<>(IntegerListFixture.class, CONFIG).generate();

        assertEquals(2, value.direct.size());
        value.direct.forEach(item -> assertInstanceOf(Integer.class, item));
        assertEquals(2, value.values.size());
        for (List<Integer> inner : value.values) {
            assertEquals(2, inner.size());
            inner.forEach(item -> assertInstanceOf(Integer.class, item));
        }
        value.mapped.values().forEach(inner -> inner.forEach(item -> assertInstanceOf(Integer.class, item)));
        assertTrue(value.optional.isPresent());
        value.optional.orElseThrow().forEach(item -> assertInstanceOf(Integer.class, item));
    }

    @Test
    @DisplayName("unbound type variable fails with its declared path and signature")
    void unboundTypeVariableFailsWithContext() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnboundFixture.class, CONFIG).generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("UnboundFixture.value", context.path());
        assertEquals("T", context.declaredType());
    }

    static class GenericFixture<T> {

        T direct;
        List<T> values;
        Map<String, T> mapped;
        Optional<T> optional;
    }

    static final class StringFixture extends GenericFixture<String> {
    }

    static class NestedGenericFixture<U> extends GenericFixture<List<U>> {
    }

    static final class IntegerListFixture extends NestedGenericFixture<Integer> {
    }

    static final class UnboundFixture<T> {

        T value;
    }
}
