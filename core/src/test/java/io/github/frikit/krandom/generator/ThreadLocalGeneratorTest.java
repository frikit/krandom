/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalGeneratorTest {

    @Test
    @DisplayName("threadLocal wrapper produces values from multiple threads without error")
    void threadLocalProducesValues() throws InterruptedException {
        Generator<String> gen = Generators.threadLocal(Generators::ofFullName);

        int threadCount = 4;
        int valuesPerThread = 50;
        Set<String> allValues = ConcurrentHashMap.newKeySet();
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    for (int j = 0; j < valuesPerThread; j++) {
                        String value = gen.generate();
                        assertNotNull(value);
                        assertFalse(value.isBlank());
                        allValues.add(value);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertTrue(allValues.size() > 1, "Should have generated distinct values");
    }

    @Test
    @DisplayName("threadLocal factory is called once per thread")
    void factoryCalledOncePerThread() throws InterruptedException {
        int[] callCount = {0};
        Generator<Integer> gen = Generators.threadLocal(() -> {
            synchronized (callCount) { callCount[0]++; }
            return Generators.ofInt(1, 1000);
        });

        int threadCount = 3;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Thread.startVirtualThread(() -> {
                try {
                    gen.generate();
                    gen.generate();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        assertEquals(threadCount, callCount[0], "Factory should be called once per thread");
    }

    @Test
    @DisplayName("null factory throws NullPointerException")
    void nullFactoryThrows() {
        assertThrows(NullPointerException.class, () -> Generators.threadLocal(null));
    }
}
