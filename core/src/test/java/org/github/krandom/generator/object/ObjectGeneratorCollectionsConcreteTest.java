/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ObjectGenerator — concrete queue/sorted collections")
class ObjectGeneratorCollectionsConcreteTest {

    static class ConcreteCollectionsHolder {
        ArrayDeque<String> arrayDeque;
        PriorityQueue<String> priorityQueue;
        TreeSet<String> treeSet;
        TreeMap<String, Integer> treeMap;
        LinkedList<String> linkedList;
    }

    static class InterfaceCollectionsHolder {
        Queue<String> queue;
        SortedSet<String> sortedSet;
        NavigableSet<String> navigableSet;
        SortedMap<String, Integer> sortedMap;
        NavigableMap<String, Integer> navigableMap;
        Map<String, Integer> plainMap;
    }

    @Test
    @DisplayName("concrete queue/sorted collections are populated with matching implementations")
    void concreteCollectionsPopulated() {
        ConcreteCollectionsHolder value = new ObjectGenerator<>(ConcreteCollectionsHolder.class).generate();
        assertNotNull(value.arrayDeque);
        assertNotNull(value.priorityQueue);
        assertNotNull(value.treeSet);
        assertNotNull(value.treeMap);
        assertNotNull(value.linkedList);
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
}
