/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

        Object value = tracker.nextUnique(
                "email", () -> attempts.getAndIncrement() == 0 ? "a@example.com" : "b@example.com", 3);
        assertEquals("a@example.com", value);

        Object next = tracker.nextUnique(
                "email", () -> attempts.getAndIncrement() == 1 ? "a@example.com" : "b@example.com", 3);
        assertEquals("b@example.com", next);
    }

    @Test
    @DisplayName("fails when uniqueness attempts are exhausted")
    void failsWhenAttemptsAreExhausted() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        tracker.nextUnique("id", () -> 1L, 1);

        UniqueFieldTracker.ExhaustedException error =
                assertThrows(UniqueFieldTracker.ExhaustedException.class, () -> tracker.nextUnique("id", () -> 1L, 2));

        assertInstanceOf(ObjectGenerationException.class, error);
        assertTrue(error.getMessage().contains("field 'id' after 2 attempts"));
    }

    @Test
    @DisplayName("a supplier's own failure is not reported as exhaustion")
    void supplierFailurePropagatesAsItself() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();

        assertThrows(
                IllegalStateException.class,
                () -> tracker.nextUnique(
                        "id",
                        () -> {
                            throw new IllegalStateException("boom");
                        },
                        2));
    }

    @Test
    @DisplayName("fields do not share their values")
    void fieldsDoNotShareTheirValues() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        tracker.nextUnique("id", () -> 1L, 1);

        assertEquals(1L, tracker.nextUnique("code", () -> 1L, 1));
    }

    @Test
    @DisplayName("a released value can be used again, and releasing what was never used changes nothing")
    void releasedValueCanBeUsedAgain() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        tracker.nextUnique("id", () -> 1L, 1);

        tracker.release("id", 1L);
        assertEquals(1L, tracker.nextUnique("id", () -> 1L, 1));

        tracker.release("id", 2L);
        tracker.release("unknown", 1L);
        assertThrows(UniqueFieldTracker.ExhaustedException.class, () -> tracker.nextUnique("id", () -> 1L, 1));
    }

    @Test
    @DisplayName("a field's key tells apart owners with the same field name")
    void fieldKeyTellsApartOwners() {
        assertEquals("java.lang.String.id", UniqueFieldTracker.fieldKey(String.class, "id"));
    }

    @Test
    @DisplayName("the value count covers what a field holds, and a null is no value")
    void valueCountCoversWhatAFieldHolds() {
        UniqueFieldTracker tracker = new UniqueFieldTracker();
        assertEquals(0, tracker.valueCount("id"));

        tracker.nextUnique("id", () -> null, 1);
        assertEquals(0, tracker.valueCount("id"));

        tracker.nextUnique("id", () -> 1L, 1);
        assertEquals(1, tracker.valueCount("id"));
    }
}
