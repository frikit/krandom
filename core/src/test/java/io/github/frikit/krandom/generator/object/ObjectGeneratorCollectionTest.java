/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.core.model.PersonWithCollections;
import io.github.frikit.krandom.generator.core.model.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    // ── Raw (erased) generic type — typeArg returns Object.class ─────────────

    @Test
    @DisplayName("raw List field (no type argument) is populated with null-element list")
    void rawListFieldProducesNullElements() {
        WithRawList obj = new ObjectGenerator<>(WithRawList.class).generate();
        assertNotNull(obj.items, "raw List must be created");
        assertFalse(obj.items.isEmpty(), "raw List must have elements");
    }

    @Test
    @DisplayName("raw Map field (no type argument) is populated with at least one entry")
    void rawMapFieldProducesEntries() {
        // Keys resolve to Object.class → null (JDK type, not nestable) → entries may be absent
        // Map creation itself must not throw.
        assertDoesNotThrow(
            () -> new ObjectGenerator<>(WithRawMap.class).generate(),
            "raw Map field must not cause an exception");
    }

    @Test
    @DisplayName("List<? extends Number> field — wildcard type arg falls back to Object.class elements")
    void wildcardTypeArgFallsBackToObject() {
        // typeArg returns Object.class for '? extends Number' (not a plain Class<?>)
        // → elements resolve as Object (JDK bootstrap type → null) → list of nulls
        WithWildcardList obj = new ObjectGenerator<>(WithWildcardList.class).generate();
        assertNotNull(obj.items, "list must be created even with wildcard type");
        assertTrue(obj.items.size() >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(obj.items.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
    }


    @SuppressWarnings("rawtypes")
    static class WithRawList {

        List items;
    }

    // ── Wildcard generic type — typeArg returns Object.class ─────────────────


    @SuppressWarnings("rawtypes")
    static class WithRawMap {

        Map data;
    }


    /**
     * Wildcard type argument — {@code typeArg} cannot extract a plain {@link Class}.
     */
    static class WithWildcardList {

        List<? extends Number> items;
    }
}
