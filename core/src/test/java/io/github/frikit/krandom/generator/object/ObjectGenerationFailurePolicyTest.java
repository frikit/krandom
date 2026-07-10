/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationFailureContext;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        ObjectGenerationException thrown = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerationFailurePolicy(false, diagnostics::add).handle(failure, "fallback"));

        assertSame(failure, thrown);
        assertEquals(List.of(CONTEXT), diagnostics.stream().map(GenerationFailureDiagnostic::context).toList());
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
            List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
            String fallback = new String("fallback");
            String result = new ObjectGenerationFailurePolicy(true, diagnostics::add).handle(failure(), fallback);

            assertSame(fallback, result);
            GenerationFailureDiagnostic diagnostic = diagnostics.getFirst();
            assertSame(CONTEXT, diagnostic.context());
            assertEquals(IllegalStateException.class.getName(), diagnostic.causeType());
            assertTrue(diagnostic.replayIdentity().isEmpty());
            String diagnosticMessage = appender.list.getFirst().getFormattedMessage();
            assertTrue(diagnosticMessage.contains("ASSIGN/ASSIGNMENT"));
            assertTrue(diagnosticMessage.contains("Order.customer"));
            assertTrue(diagnosticMessage.contains("java.lang.IllegalStateException"));
            assertFalse(diagnosticMessage.contains("personal-looking-value"));
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
    @DisplayName("listener receives the configured safe replay identity")
    void listenerReceivesReplayIdentity() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();

        new ObjectGenerationFailurePolicy(true, diagnostics::add, Optional.of("safe-recipe"))
            .handle(failure(), "fallback");

        assertEquals(Optional.of("safe-recipe"), diagnostics.getFirst().replayIdentity());
    }

    @Test
    @DisplayName("lenient policy rejects failures without structured context")
    void lenientPolicyRejectsUnstructuredFailure() {
        ObjectGenerationException failure = new ObjectGenerationException("legacy");

        assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectGenerationFailurePolicy(true).handle(failure, "fallback"));
    }

    @Test
    @DisplayName("listener failure cannot replace strict or lenient generation behavior")
    void listenerFailureDoesNotReplaceGenerationBehavior() {
        ObjectGenerationException strictFailure = failure();
        ObjectGenerationFailurePolicy strict = new ObjectGenerationFailurePolicy(
            false, diagnostic -> { throw new IllegalStateException("personal-looking-value"); });
        assertSame(
            strictFailure,
            assertThrows(ObjectGenerationException.class, () -> strict.handle(strictFailure, "fallback")));

        ObjectGenerationFailurePolicy lenient = new ObjectGenerationFailurePolicy(
            true, diagnostic -> { throw new IllegalStateException("personal-looking-value"); });
        assertEquals("fallback", lenient.handle(failure(), "fallback"));
    }

    @Test
    @DisplayName("diagnostic rejects null structural fields")
    void diagnosticRejectsNullFields() {
        assertThrows(NullPointerException.class,
                     () -> new GenerationFailureDiagnostic(null, "cause", java.util.Optional.empty()));
        assertThrows(NullPointerException.class,
                     () -> new GenerationFailureDiagnostic(CONTEXT, null, java.util.Optional.empty()));
        assertThrows(NullPointerException.class,
                     () -> new GenerationFailureDiagnostic(CONTEXT, "cause", null));
    }

    @Test
    @DisplayName("configured listener observes strict resolver failures")
    void configuredListenerObservesResolverFailure() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .generationFailureListener(diagnostics::add)
                                                .build();

        assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnsupportedHolder.class, config).generate());

        GenerationFailureDiagnostic diagnostic = diagnostics.getFirst();
        assertEquals(GenerationFailureCategory.UNSUPPORTED_TYPE, diagnostic.context().category());
        assertEquals("UnsupportedHolder.task", diagnostic.context().path());
        assertEquals(UnsupportedOperationException.class.getName(), diagnostic.causeType());
    }

    @Test
    @DisplayName("seeded portable resolver diagnostics carry a safe replay recipe")
    void seededResolverFailureCarriesSafeReplayRecipe() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed("do-not-log-this-text")
                                                .generationFailureListener(diagnostics::add)
                                                .build();

        assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectGenerator<>(UnsupportedHolder.class, config).generate());

        String recipe = diagnostics.getFirst().replayIdentity().orElseThrow();
        assertTrue(recipe.contains("format=krandom-recipe"));
        assertFalse(recipe.contains("do-not-log-this-text"));
    }

    @Test
    @DisplayName("configured listener observes lenient assignment failures")
    void configuredListenerObservesAssignmentFailure() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectIgnoreErrors(true)
                                                .objectOverride(
                                                    AssignmentHolder.class,
                                                    "value",
                                                    () -> "personal-looking-value")
                                                .generationFailureListener(diagnostics::add)
                                                .build();

        AssignmentHolder holder = new ObjectGenerator<>(AssignmentHolder.class, config).generate();

        assertEquals(0, holder.value);
        GenerationFailureDiagnostic diagnostic = diagnostics.getFirst();
        assertEquals(GenerationFailureCategory.ASSIGNMENT, diagnostic.context().category());
        assertEquals("AssignmentHolder.value", diagnostic.context().path());
        assertEquals(IllegalArgumentException.class.getName(), diagnostic.causeType());
    }

    private static ObjectGenerationException failure() {
        return new ObjectGenerationException(
            "personal-looking-value",
            CONTEXT,
            new IllegalStateException("personal-looking-value"));
    }

    static class UnsupportedHolder {

        Runnable task;
    }

    static class AssignmentHolder {

        int value;
    }
}
