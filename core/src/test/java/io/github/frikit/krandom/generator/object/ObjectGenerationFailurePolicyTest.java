/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectGenerationFailurePolicy")
class ObjectGenerationFailurePolicyTest {

    private static final GenerationFailureContext CONTEXT = new GenerationFailureContext(
        GenerationFailureCategory.ASSIGNMENT,
        GenerationOperation.ASSIGN,
        "Order.customer",
        ObjectGenerationFailurePolicyTest.class,
        String.class.getName(),
        2,
        -1);

    @Test
    @DisplayName("strict policy throws the exact contextual failure")
    void strictPolicyThrowsExactFailure() {
        ObjectGenerationException failure = failure();

        ObjectGenerationException thrown = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerationFailurePolicy(false).handle(failure, "fallback"));

        assertSame(failure, thrown);
    }

    @Test
    @DisplayName("lenient policy returns fallback and logs only sanitized context")
    void lenientPolicyReturnsFallbackAndLogsSanitizedContext() {
        Logger logger = (Logger) LoggerFactory.getLogger(ObjectGenerationFailurePolicy.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Level previousLevel = logger.getLevel();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(appender);
        try {
            String fallback = new String("fallback");
            String result = new ObjectGenerationFailurePolicy(true).handle(failure(), fallback);

            assertSame(fallback, result);
            String diagnostic = appender.list.getFirst().getFormattedMessage();
            assertTrue(diagnostic.contains("ASSIGN/ASSIGNMENT"));
            assertTrue(diagnostic.contains("Order.customer"));
            assertTrue(diagnostic.contains("java.lang.IllegalStateException"));
            assertFalse(diagnostic.contains("personal-looking-value"));
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(previousLevel);
        }
    }

    @Test
    @DisplayName("lenient policy accepts a contextual failure without an underlying cause")
    void lenientPolicyAcceptsMissingCause() {
        ObjectGenerationException failure = new ObjectGenerationException("sanitized", CONTEXT, null);

        assertEquals("fallback", new ObjectGenerationFailurePolicy(true).handle(failure, "fallback"));
    }

    @Test
    @DisplayName("lenient policy rejects failures without structured context")
    void lenientPolicyRejectsUnstructuredFailure() {
        ObjectGenerationException failure = new ObjectGenerationException("legacy");

        assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectGenerationFailurePolicy(true).handle(failure, "fallback"));
    }

    private static ObjectGenerationException failure() {
        return new ObjectGenerationException(
            "personal-looking-value",
            CONTEXT,
            new IllegalStateException("personal-looking-value"));
    }
}
