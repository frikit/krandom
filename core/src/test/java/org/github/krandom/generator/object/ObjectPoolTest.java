/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectPool")
class ObjectPoolTest {

    @Test
    @DisplayName("isInProgress returns false before begin is called")
    void notInProgressInitially() {
        ObjectPool pool = new ObjectPool();
        assertFalse(pool.isInProgress(String.class));
    }

    @Test
    @DisplayName("isInProgress returns true after begin is called")
    void inProgressAfterBegin() {
        ObjectPool pool = new ObjectPool();
        pool.begin(String.class);
        assertTrue(pool.isInProgress(String.class));
    }

    @Test
    @DisplayName("isInProgress returns false after end is called")
    void notInProgressAfterEnd() {
        ObjectPool pool = new ObjectPool();
        pool.begin(String.class);
        pool.end(String.class, "hello");
        assertFalse(pool.isInProgress(String.class));
    }

    @Test
    @DisplayName("getCached returns null before any instance is completed")
    void cachedNullInitially() {
        ObjectPool pool = new ObjectPool();
        assertNull(pool.getCached(Integer.class));
    }

    @Test
    @DisplayName("getCached returns the instance passed to end")
    void cachedAfterEnd() {
        ObjectPool pool = new ObjectPool();
        Object obj = new Object();
        pool.begin(Object.class);
        pool.end(Object.class, obj);
        assertSame(obj, pool.getCached(Object.class));
    }

    @Test
    @DisplayName("end with null instance does not cache null")
    void endWithNullNotCached() {
        ObjectPool pool = new ObjectPool();
        pool.begin(Double.class);
        pool.end(Double.class, null);
        assertFalse(pool.isInProgress(Double.class), "type should no longer be in-progress");
        assertNull(pool.getCached(Double.class), "null instance must not be cached");
    }

    @Test
    @DisplayName("pool tracks multiple types independently")
    void multipleTypesTrackedIndependently() {
        ObjectPool pool = new ObjectPool();
        pool.begin(String.class);
        pool.begin(Integer.class);
        assertTrue(pool.isInProgress(String.class));
        assertTrue(pool.isInProgress(Integer.class));
        assertFalse(pool.isInProgress(Double.class));
        pool.end(String.class, "value");
        assertFalse(pool.isInProgress(String.class));
        assertTrue(pool.isInProgress(Integer.class));
    }

    @Test
    @DisplayName("pool with size 0 never caches completed instances")
    void zeroSizedPoolDoesNotCache() {
        ObjectPool pool = new ObjectPool(0);
        pool.begin(String.class);
        pool.end(String.class, "value");
        assertNull(pool.getCached(String.class));
    }

    @Test
    @DisplayName("pool evicts oldest entries when capacity is exceeded")
    void poolEvictsOldestEntries() {
        ObjectPool pool = new ObjectPool(2);
        Object first = new Object();
        Object second = new Object();
        Object third = new Object();

        pool.begin(Object.class);
        pool.end(Object.class, first);
        pool.begin(Object.class);
        pool.end(Object.class, second);
        pool.begin(Object.class);
        pool.end(Object.class, third);

        assertSame(third, pool.getCached(Object.class),
                   "most recent cached instance should be returned");
    }

    @Test
    @DisplayName("negative pool size throws IllegalArgumentException")
    void negativePoolSizeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new ObjectPool(-1));
    }
}
