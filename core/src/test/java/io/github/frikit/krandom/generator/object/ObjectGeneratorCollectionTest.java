/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.core.model.PersonWithCollections;
import io.github.frikit.krandom.generator.core.model.Status;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — collection auto-population")
class ObjectGeneratorCollectionTest {

    @Test
    @DisplayName("List<String> field size follows shared collection defaults")
    void listPopulated() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        assertNotNull(p.getHobbies(), "List<String> field must not be null");
        assertTrue(p.getHobbies().size() >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(p.getHobbies().size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
    }

    @Test
    @DisplayName("all List<String> elements are non-null strings")
    void listElementsAreStrings() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        for (String hobby : p.getHobbies()) {
            assertNotNull(hobby, "List element must not be null");
            assertFalse(hobby.isEmpty(), "List element must not be empty");
        }
    }

    @Test
    @DisplayName("Set<Status> field is non-null with at least 1 element")
    void setPopulated() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        assertNotNull(p.getRoles(), "Set<Status> field must not be null");
        assertFalse(p.getRoles().isEmpty(), "Set<Status> should not be empty");
    }

    @Test
    @DisplayName("all Set<Status> elements are valid Status constants")
    void setElementsAreValidStatus() {
        var validStatuses = new HashSet<>(Arrays.asList(Status.values()));
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        for (Status role : p.getRoles()) {
            assertNotNull(role, "Set element must not be null");
            assertTrue(validStatuses.contains(role), "unexpected Status value: " + role);
        }
    }

    @Test
    @DisplayName("Map<String,Integer> field is non-null with at least 1 entry")
    void mapPopulated() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        assertNotNull(p.getAttributes(), "Map<String,Integer> field must not be null");
        assertFalse(p.getAttributes().isEmpty(), "Map<String,Integer> should not be empty");
    }

    @Test
    @DisplayName("Map<String,Integer> keys are non-null strings")
    void mapKeysAreStrings() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        for (String key : p.getAttributes().keySet()) {
            assertNotNull(key, "Map key must not be null");
        }
    }

    @Test
    @DisplayName("Map<String,Integer> values are Integers")
    void mapValuesAreIntegers() {
        PersonWithCollections p = new ObjectGenerator<>(PersonWithCollections.class).generate();
        for (Integer value : p.getAttributes().values()) {
            assertNotNull(value, "Map value must not be null");
        }
    }

    // ── Unsupported ambiguous generic shapes ─────────────────────────────────

    @Test
    @DisplayName("raw List field fails with the complete field path and signature")
    void rawListFieldFailsWithContext() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(WithRawList.class).generate());
        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, context.category());
        assertEquals("WithRawList.items", context.path());
        assertEquals(List.class.getTypeName(), context.declaredType());
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }

    @Test
    @DisplayName("raw Map field fails instead of silently returning an empty map")
    void rawMapFieldFailsWithContext() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(WithRawMap.class).generate());
        assertEquals("WithRawMap.data", ex.getContext().orElseThrow().path());
    }

    @Test
    @DisplayName("lenient raw List handling discards the whole value and emits context")
    void lenientRawListReturnsNullAndEmitsDiagnostic() {
        AtomicReference<GenerationFailureDiagnostic> observed = new AtomicReference<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::set)
                                                .build();

        WithRawList value = new ObjectGenerator<>(WithRawList.class, config).generate();

        assertNull(value.items);
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, observed.get().context().category());
        assertEquals("WithRawList.items", observed.get().context().path());
    }

    @Test
    @DisplayName("lenient raw Set, Map, and Optional handling discards every whole value")
    void lenientRawContainersReturnNullAndEmitDiagnostics() {
        List<GenerationFailureDiagnostic> observed = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectIgnoreErrors(true)
                                                .generationFailureListener(observed::add)
                                                .build();

        WithRawContainers value = new ObjectGenerator<>(WithRawContainers.class, config).generate();

        assertNull(value.items);
        assertNull(value.data);
        assertNull(value.optional);
        assertEquals(3, observed.size());
        assertTrue(observed.stream().allMatch(
            diagnostic -> diagnostic.context().category() == GenerationFailureCategory.UNSUPPORTED_TYPE));
    }

    @Test
    @DisplayName("custom list with ambiguous extra type argument fails contextually")
    void customListWithExtraTypeArgumentFailsContextually() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(WithAmbiguousCustomList.class).generate());
        assertEquals(
            "io.github.frikit.krandom.generator.object.ObjectGeneratorCollectionTest$TwoArgumentList"
            + "<java.lang.String, java.lang.Integer>",
            ex.getContext().orElseThrow().declaredType());
    }

    @Test
    @DisplayName("upper-bounded wildcard elements use the effective bound")
    void upperBoundedWildcardUsesEffectiveBound() {
        WithWildcardList obj = new ObjectGenerator<>(WithWildcardList.class).generate();
        assertNotNull(obj.items);
        assertTrue(obj.items.size() >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(obj.items.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        obj.items.forEach(item -> assertTrue(item instanceof Number));
    }

    @Test
    @DisplayName("lower-bounded wildcard elements use the effective bound")
    void lowerBoundedWildcardUsesEffectiveBound() {
        WithLowerWildcardList obj = new ObjectGenerator<>(WithLowerWildcardList.class).generate();
        assertNotNull(obj.items);
        obj.items.forEach(item -> assertTrue(item instanceof Integer));
    }

    @Test
    @DisplayName("unbounded wildcard fails with the parent container signature")
    void unboundedWildcardFailsWithParentSignature() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(WithUnboundedWildcardList.class).generate());
        var context = ex.getContext().orElseThrow();
        assertEquals("WithUnboundedWildcardList.items", context.path());
        assertEquals("java.util.List<?>", context.declaredType());
    }


    @SuppressWarnings("rawtypes")
    static class WithRawList {

        List items;
    }

    @SuppressWarnings("rawtypes")
    static class WithRawMap {

        Map data;
    }

    @SuppressWarnings("rawtypes")
    static class WithRawContainers {

        Set items;
        Map data;
        Optional optional;
    }

    static class WithAmbiguousCustomList {

        TwoArgumentList<String, Integer> items;
    }

    static final class TwoArgumentList<E, M> extends ArrayList<E> {
    }


    static class WithWildcardList {

        List<? extends Number> items;
    }

    static class WithLowerWildcardList {

        List<? super Integer> items;
    }

    static class WithUnboundedWildcardList {

        List<?> items;
    }
}
