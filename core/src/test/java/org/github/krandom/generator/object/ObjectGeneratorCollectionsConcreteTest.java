/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
