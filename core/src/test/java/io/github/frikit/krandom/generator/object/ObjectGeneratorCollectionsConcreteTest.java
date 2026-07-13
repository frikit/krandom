/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerator — concrete queue/sorted collections")
class ObjectGeneratorCollectionsConcreteTest {

    @Test
    @DisplayName("concrete queue/sorted collections are populated with matching implementations")
    void concreteCollectionsPopulated() {
        ConcreteCollectionsHolder value = new ObjectGenerator<>(ConcreteCollectionsHolder.class).generate();
        assertNotNull(value.arrayList);
        assertNotNull(value.vector);
        assertNotNull(value.stack);
        assertNotNull(value.copyOnWriteArrayList);
        assertNotNull(value.arrayDeque);
        assertNotNull(value.priorityQueue);
        assertNotNull(value.treeSet);
        assertNotNull(value.treeMap);
        assertNotNull(value.linkedList);
        assertEquals(ArrayList.class, value.arrayList.getClass());
        assertEquals(Vector.class, value.vector.getClass());
        assertEquals(Stack.class, value.stack.getClass());
        assertEquals(CopyOnWriteArrayList.class, value.copyOnWriteArrayList.getClass());
        assertEquals(ArrayDeque.class, value.arrayDeque.getClass());
        assertEquals(PriorityQueue.class, value.priorityQueue.getClass());
        assertEquals(TreeSet.class, value.treeSet.getClass());
        assertEquals(TreeMap.class, value.treeMap.getClass());
        assertEquals(LinkedList.class, value.linkedList.getClass());
    }

    @Test
    @DisplayName("queue/sorted interfaces resolve to supported concrete implementations")
    void interfaceCollectionsUseSupportedImplementations() {
        InterfaceCollectionsHolder value = new ObjectGenerator<>(InterfaceCollectionsHolder.class).generate();
        assertNotNull(value.queue);
        assertNotNull(value.sortedSet);
        assertNotNull(value.navigableSet);
        assertNotNull(value.sortedMap);
        assertNotNull(value.navigableMap);
        assertNotNull(value.plainMap);
        assertTrue(value.queue instanceof ArrayDeque);
        assertTrue(value.sortedSet instanceof TreeSet);
        assertTrue(value.navigableSet instanceof TreeSet);
        assertTrue(value.sortedMap instanceof TreeMap);
        assertTrue(value.navigableMap instanceof TreeMap);
        assertThrows(UnsupportedOperationException.class, () -> value.plainMap.put("x", 1));
    }

    @Test
    @DisplayName("custom concrete collection subtypes are instantiated and assigned")
    void customConcreteCollectionSubtypesAreAssigned() {
        CustomCollectionsHolder value = assertDoesNotThrow(
            () -> new ObjectGenerator<>(CustomCollectionsHolder.class).generate());
        assertNotNull(value.customList);
        assertNotNull(value.customSet);
        assertNotNull(value.customQueue);
        assertNotNull(value.customMap);
        assertEquals(CustomStringList.class, value.customList.getClass());
        assertEquals(CustomStringSet.class, value.customSet.getClass());
        assertEquals(CustomStringQueue.class, value.customQueue.getClass());
        assertEquals(CustomStringIntMap.class, value.customMap.getClass());
        assertTrue(value.customList.size() >= FieldGeneratorResolver.DEFAULT_MIN_ELEMENT_COUNT);
        assertTrue(value.customList.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        assertFalse(value.customQueue.isEmpty());
        assertTrue(value.customQueue.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        assertFalse(value.customMap.isEmpty());
        assertTrue(value.customMap.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        assertTrue(value.customSet.size() > 0
                   && value.customSet.size() <= FieldGeneratorResolver.DEFAULT_MAX_ELEMENT_COUNT);
        value.customList.forEach(item -> assertTrue(item instanceof String));
        value.customSet.forEach(item -> assertTrue(item instanceof String));
        value.customQueue.forEach(item -> assertTrue(item instanceof String));
        value.customMap.forEach((key, item) -> {
            assertTrue(key instanceof String);
            assertTrue(item instanceof Integer);
        });
    }

    @Test
    @DisplayName("non-constructible concrete map subtype resolves to null without throwing")
    void nonConstructibleConcreteMapSubtypeResolvesToNull() {
        NonConstructibleMapHolder value = assertDoesNotThrow(
            () -> new ObjectGenerator<>(NonConstructibleMapHolder.class).generate());
        assertNull(value.customMap);
    }


    static class ConcreteCollectionsHolder {

        ArrayList<String>            arrayList;
        Vector<String>               vector;
        Stack<String>                stack;
        CopyOnWriteArrayList<String> copyOnWriteArrayList;
        ArrayDeque<String>           arrayDeque;
        PriorityQueue<String>        priorityQueue;
        TreeSet<String>              treeSet;
        TreeMap<String, Integer>     treeMap;
        LinkedList<String>           linkedList;
    }


    static class InterfaceCollectionsHolder {

        Queue<String>                 queue;
        SortedSet<String>             sortedSet;
        NavigableSet<String>          navigableSet;
        SortedMap<String, Integer>    sortedMap;
        NavigableMap<String, Integer> navigableMap;
        Map<String, Integer>          plainMap;
    }

    static class CustomCollectionsHolder {

        CustomStringList   customList;
        CustomStringSet    customSet;
        CustomStringQueue  customQueue;
        CustomStringIntMap customMap;
    }

    static final class CustomStringList extends ArrayList<String> {
    }

    static final class CustomStringSet extends LinkedHashSet<String> {
    }

    static final class CustomStringQueue extends ArrayDeque<String> {
    }

    static final class CustomStringIntMap extends LinkedHashMap<String, Integer> {
    }

    static class NonConstructibleMapHolder {

        NoDefaultCtorLinkedHashMap customMap;
    }

    static final class NoDefaultCtorLinkedHashMap extends LinkedHashMap<String, Integer> {

        private NoDefaultCtorLinkedHashMap(String ignored) {
        }
    }
}
