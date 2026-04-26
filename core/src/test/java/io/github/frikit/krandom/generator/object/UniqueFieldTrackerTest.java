/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UniqueFieldTracker")
class UniqueFieldTrackerTest {

    @Test
    @DisplayName("rejects maxAttempts below one")
    void rejectsInvalidMaxAttempts() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        assertThrows(IllegalArgumentException.class, () -> tracker.nextUnique("email", () -> "x", 0));
    }

    @Test
    @DisplayName("allows null candidates")
    void allowsNullCandidates() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        assertNull(tracker.nextUnique("email", () -> null, 1));
    }

    @Test
    @DisplayName("retries duplicates until a unique candidate appears")
    void retriesDuplicatesUntilUnique() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        AtomicInteger attempts = new AtomicInteger();

        Object value = tracker.nextUnique("email", () -> attempts.getAndIncrement() == 0 ? "a@example.com" : "b@example.com", 3);
        assertEquals("a@example.com", value);

        Object next = tracker.nextUnique("email", () -> attempts.getAndIncrement() == 1 ? "a@example.com" : "b@example.com", 3);
        assertEquals("b@example.com", next);
    }

    @Test
    @DisplayName("fails when uniqueness attempts are exhausted")
    void failsWhenAttemptsAreExhausted() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        tracker.nextUnique("id", () -> 1L, 1);

        assertThrows(ObjectGenerationException.class, () -> tracker.nextUnique("id", () -> 1L, 2));
    }
}
