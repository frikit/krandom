/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("FieldGeneratorResolver edge coverage")
class FieldGeneratorResolverCoverageTest {

    interface CustomList<E> extends List<E> {}

    abstract static class AbstractCustomList<E> extends java.util.AbstractList<E> {}

    static final class ConcreteCustomList<E> extends ArrayList<E> {}

    @Test
    @DisplayName("toListType handles interface, abstract, unknown concrete, and CopyOnWriteArrayList")
    void toListTypeFallbackBranches() throws Exception {
        List<Object> values = new ArrayList<>(List.of("a", "b", "c"));

        List<Object> interfaceFallback = invokeToListType(CustomList.class, values);
        assertEquals(ArrayList.class, interfaceFallback.getClass());
        assertEquals(values, interfaceFallback);

        List<Object> abstractFallback = invokeToListType(AbstractCustomList.class, values);
        assertEquals(ArrayList.class, abstractFallback.getClass());
        assertEquals(values, abstractFallback);

        List<Object> concreteFallback = invokeToListType(ConcreteCustomList.class, values);
        assertEquals(ArrayList.class, concreteFallback.getClass());
        assertEquals(values, concreteFallback);

        List<Object> copyOnWrite = invokeToListType(CopyOnWriteArrayList.class, values);
        assertEquals(CopyOnWriteArrayList.class, copyOnWrite.getClass());
        assertEquals(values, copyOnWrite);
    }

    @SuppressWarnings("unchecked")
    private static List<Object> invokeToListType(Class<?> rawType, List<Object> values) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toListType", Class.class, List.class);
        method.setAccessible(true);
        return (List<Object>) method.invoke(null, rawType, values);
    }
}
