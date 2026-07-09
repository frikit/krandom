/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldGeneratorResolver collection fallback coverage")
class FieldGeneratorResolverCollectionFallbackTest {

    @SuppressWarnings("unchecked")
    private static List<Object> invokeToListType(Class<?> rawType, List<Object> values) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toListType", Class.class, List.class);
        method.setAccessible(true);
        return (List<Object>) method.invoke(null, rawType, values);
    }

    @SuppressWarnings("unchecked")
    private static Set<Object> invokeToSetType(Class<?> rawType, List<Object> values) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toSetType", Class.class, List.class);
        method.setAccessible(true);
        return (Set<Object>) method.invoke(null, rawType, values);
    }

    @SuppressWarnings("unchecked")
    private static Queue<Object> invokeToQueueType(Class<?> rawType, List<Object> values) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toQueueType", Class.class, List.class);
        method.setAccessible(true);
        return (Queue<Object>) method.invoke(null, rawType, values);
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> invokeToMapType(Class<?> rawType) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod("toMapType", Class.class);
        method.setAccessible(true);
        return (Map<Object, Object>) method.invoke(null, rawType);
    }

    private static Object invokeInstantiateCollectionType(Class<?> rawType, Class<?> expectedType) throws Exception {
        Method method = FieldGeneratorResolver.class.getDeclaredMethod(
            "instantiateCollectionType", Class.class, Class.class);
        method.setAccessible(true);
        return method.invoke(null, rawType, expectedType);
    }

    @Test
    @DisplayName("toListType returns null for non-constructible concrete list subtype")
    void toListTypeNonConstructibleConcreteReturnsNull() throws Exception {
        List<Object> values = new ArrayList<>(List.of("a", "b", "c"));
        List<Object> generated = invokeToListType(NoDefaultCtorList.class, values);
        assertNull(generated);

        List<Object> rejecting = invokeToListType(RejectingList.class, values);
        assertNull(rejecting);
    }

    @Test
    @DisplayName("toSetType handles concrete subtype and rejecting concrete set")
    void toSetTypeConcreteAndRejectingBranches() throws Exception {
        List<Object> values = new ArrayList<>(List.of("a", "b", "c"));
        Set<Object> interfaceFallback = invokeToSetType(CustomSet.class, values);
        assertEquals(LinkedHashSet.class, interfaceFallback.getClass());
        assertEquals(new LinkedHashSet<>(values), interfaceFallback);

        Set<Object> abstractFallback = invokeToSetType(AbstractCustomSet.class, values);
        assertEquals(LinkedHashSet.class, abstractFallback.getClass());
        assertEquals(new LinkedHashSet<>(values), abstractFallback);

        Set<Object> concrete = invokeToSetType(ConcreteCustomSet.class, values);
        assertEquals(ConcreteCustomSet.class, concrete.getClass());
        assertEquals(new LinkedHashSet<>(values), concrete);

        Set<Object> rejecting = invokeToSetType(RejectingSet.class, values);
        assertNull(rejecting);
    }

    @Test
    @DisplayName("toQueueType filters nulls for concrete queue and returns null for non-constructible subtype")
    void toQueueTypeQueueSpecificFallbacks() throws Exception {
        Queue<Object> concreteAllNonNull = invokeToQueueType(ConcreteCustomQueue.class, List.of("a", "b", "c"));
        assertEquals(ConcreteCustomQueue.class, concreteAllNonNull.getClass());
        assertEquals(3, concreteAllNonNull.size());

        List<Object> valuesWithNull = Arrays.asList("a", null, "c");
        Queue<Object> concrete = invokeToQueueType(ConcreteCustomQueue.class, valuesWithNull);
        assertEquals(ConcreteCustomQueue.class, concrete.getClass());
        assertEquals(2, concrete.size());
        assertTrue(concrete.contains("a"));
        assertTrue(concrete.contains("c"));

        Queue<Object> priorityQueue = invokeToQueueType(PriorityQueue.class, Arrays.asList("a", new Object()));
        assertEquals(PriorityQueue.class, priorityQueue.getClass());

        Queue<Object> interfaceQueue = invokeToQueueType(Queue.class, List.of("a", "b"));
        assertEquals(ArrayDeque.class, interfaceQueue.getClass());

        Queue<Object> abstractQueue = invokeToQueueType(AbstractCustomQueue.class, List.of("a", "b"));
        assertEquals(ArrayDeque.class, abstractQueue.getClass());

        Queue<Object> exploding = invokeToQueueType(ExplodingQueue.class, Arrays.asList("a", null));
        assertNull(exploding);

        Queue<Object> noDefaultCtor = invokeToQueueType(NoDefaultCtorQueue.class, List.of("x"));
        assertNull(noDefaultCtor);
    }

    @Test
    @DisplayName("toMapType covers interface, concrete, and non-constructible branches")
    void toMapTypeBranches() throws Exception {
        Map<Object, Object> interfaceMap = invokeToMapType(Map.class);
        assertEquals(LinkedHashMap.class, interfaceMap.getClass());

        Map<Object, Object> abstractMap = invokeToMapType(AbstractCustomMap.class);
        assertEquals(LinkedHashMap.class, abstractMap.getClass());

        Map<Object, Object> concrete = invokeToMapType(ConcreteCustomMap.class);
        assertEquals(ConcreteCustomMap.class, concrete.getClass());

        Map<Object, Object> noDefaultCtor = invokeToMapType(NoDefaultCtorMap.class);
        assertNull(noDefaultCtor);
    }

    @Test
    @DisplayName("strict map insertion failure reports sanitized indexed context")
    void strictMapInsertionFailureIsContextual() {
        GeneratorConfig config = mapFailureConfig(false);

        ObjectGenerationException error = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(ThrowingMapHolder.class, config).generate());

        var context = error.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.COLLECTION_INSERTION, context.category());
        assertEquals(GenerationOperation.INSERT, context.operation());
        assertEquals("ThrowingMapHolder.values[0]", context.path());
        assertTrue(error.getCause() instanceof UnsupportedOperationException);
        assertFalse(error.getMessage().contains("reject put"));
    }

    @Test
    @DisplayName("lenient map insertion failure discards the whole map")
    void lenientMapInsertionFailureReturnsNull() {
        ThrowingMapHolder holder = new ObjectGenerator<>(ThrowingMapHolder.class, mapFailureConfig(true)).generate();

        assertNull(holder.values);
    }

    private static GeneratorConfig mapFailureConfig(boolean ignoreErrors) {
        return GeneratorConfig.builder()
                              .collectionSize(1, 1)
                              .objectIgnoreErrors(ignoreErrors)
                              .objectSemanticMode(ObjectGenerationSemanticMode.STRUCTURAL_ONLY)
                              .build();
    }

    @Test
    @DisplayName("instantiateCollectionType returns null when raw type is not assignable")
    void instantiateCollectionTypeMismatchReturnsNull() throws Exception {
        Object mismatch = invokeInstantiateCollectionType(String.class, List.class);
        assertNull(mismatch);
    }

    static final class NoDefaultCtorList<E> extends ArrayList<E> {

        private NoDefaultCtorList(String ignored) {
        }
    }

    static final class RejectingList<E> extends ArrayList<E> {

        @Override
        public boolean addAll(java.util.Collection<? extends E> collection) {
            throw new UnsupportedOperationException("reject");
        }
    }

    interface CustomSet<E> extends Set<E> {
    }

    abstract static class AbstractCustomSet<E> extends java.util.AbstractSet<E> {
    }

    static final class ConcreteCustomSet<E> extends LinkedHashSet<E> {
    }

    static final class RejectingSet<E> extends LinkedHashSet<E> {

        @Override
        public boolean add(E element) {
            throw new UnsupportedOperationException("reject");
        }
    }

    static final class ConcreteCustomQueue<E> extends ArrayDeque<E> {
    }

    static final class NoDefaultCtorQueue<E> extends ArrayDeque<E> {

        private NoDefaultCtorQueue(String ignored) {
        }
    }

    static final class ExplodingQueue<E> extends ArrayDeque<E> {

        @Override
        public void clear() {
            throw new UnsupportedOperationException("reject clear");
        }
    }

    abstract static class AbstractCustomQueue<E> extends java.util.AbstractQueue<E> {
    }

    abstract static class AbstractCustomMap<K, V> extends java.util.AbstractMap<K, V> {
    }

    static final class ConcreteCustomMap<K, V> extends LinkedHashMap<K, V> {
    }

    static final class NoDefaultCtorMap<K, V> extends LinkedHashMap<K, V> {

        private NoDefaultCtorMap(String ignored) {
        }
    }

    static final class ThrowingPutMap<K, V> extends LinkedHashMap<K, V> {

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException("reject put");
        }
    }

    static final class ThrowingMapHolder {

        ThrowingPutMap<String, Integer> values;
    }
}
