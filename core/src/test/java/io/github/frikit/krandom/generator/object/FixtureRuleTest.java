/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.object.FixtureRuleProblem.Category;
import io.github.frikit.krandom.generator.object.exception.FixtureRuleException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FixtureRule, FixtureRuleProblem and FixtureRuleException")
class FixtureRuleTest {

    @Test
    @DisplayName("an ObjectFaker rule has a registration number and a GeneratorConfig entry has -1")
    void registrationFollowsTheKind() {
        FixtureRule rule = FixtureRule.registered(FixtureRuleKind.DEPENDENT_RULE, "address.city", 3);
        FixtureRule entry = FixtureRule.configured(FixtureRuleKind.CONTEXT_PREDICATE_OVERRIDE, "email");

        assertEquals(3, rule.registration());
        assertEquals(-1, entry.registration());
        assertEquals("dependent rule #3 'address.city'", rule.describe());
        assertEquals("GeneratorConfig context predicate override at 'email'", entry.describe());
        assertTrue(FixtureRuleKind.IGNORE.isRegisteredOnObjectFaker());
        assertFalse(FixtureRuleKind.UNIQUE_FIELD.isRegisteredOnObjectFaker());
    }

    @Test
    @DisplayName("a rule rejects a missing kind or path, a blank path, and a registration that its kind does not take")
    void invalidRulesAreRejected() {
        assertThrows(NullPointerException.class, () -> new FixtureRule(null, "a", 1));
        assertThrows(NullPointerException.class, () -> new FixtureRule(FixtureRuleKind.RULE, null, 1));
        assertEquals(
                "path must not be blank",
                assertThrows(IllegalArgumentException.class, () -> new FixtureRule(FixtureRuleKind.RULE, " ", 1))
                        .getMessage());
        assertEquals(
                "an ObjectFaker rule needs a registration number >= 1",
                assertThrows(IllegalArgumentException.class, () -> FixtureRule.registered(FixtureRuleKind.RULE, "a", 0))
                        .getMessage());
        assertEquals(
                "a GeneratorConfig entry has registration -1",
                assertThrows(
                                IllegalArgumentException.class,
                                () -> new FixtureRule(FixtureRuleKind.FIELD_OVERRIDE, "a", 1))
                        .getMessage());
        assertEquals(
                "a GeneratorConfig entry has registration -1",
                assertThrows(IllegalArgumentException.class, () -> new FixtureRule(FixtureRuleKind.EXCLUSION, "a", 0))
                        .getMessage());
    }

    @Test
    @DisplayName("a problem rejects a missing category, rule or reason, and a blank reason")
    void invalidProblemsAreRejected() {
        FixtureRule rule = FixtureRule.registered(FixtureRuleKind.INCLUDE, "name", 1);

        assertThrows(NullPointerException.class, () -> new FixtureRuleProblem(null, rule, "why"));
        assertThrows(NullPointerException.class, () -> new FixtureRuleProblem(Category.UNUSED, null, "why"));
        assertThrows(NullPointerException.class, () -> new FixtureRuleProblem(Category.UNUSED, rule, null));
        assertThrows(IllegalArgumentException.class, () -> new FixtureRuleProblem(Category.UNUSED, rule, " "));
        assertEquals(
                "AMBIGUOUS include #1 'name': because",
                new FixtureRuleProblem(Category.AMBIGUOUS, rule, "because").describe());
    }

    @Test
    @DisplayName("the exception needs a problem, counts one problem in the singular, and keeps copies")
    void exceptionListsItsProblems() {
        FixtureRuleProblem problem = new FixtureRuleProblem(
                Category.SHADOWED, FixtureRule.configured(FixtureRuleKind.TYPE_OVERRIDE, "root"), "because");

        FixtureRuleException single = new FixtureRuleException(String.class, List.of(), List.of(problem));
        FixtureRuleException undecided = new FixtureRuleException(String.class, List.of("value"), List.of());

        assertEquals(
                "ObjectFaker for java.lang.String has 1 fixture rule problem:\n"
                        + "- SHADOWED GeneratorConfig type override at 'root': because",
                single.getMessage());
        assertEquals(List.of(problem), single.getProblems());
        assertEquals(List.of(), single.getUndecidedProperties());
        assertEquals(List.of("value"), undecided.getUndecidedProperties());
        assertTrue(undecided
                .getMessage()
                .endsWith("UNDECIDED root properties [value]: each needs a rule, an include or an ignore"));
        assertThrows(
                IllegalArgumentException.class, () -> new FixtureRuleException(String.class, List.of(), List.of()));
        assertThrows(NullPointerException.class, () -> new FixtureRuleException(null, List.of("a"), List.of()));
    }

    @Test
    @DisplayName("the exception keeps its problems through serialization")
    void exceptionSerializes() throws IOException, ClassNotFoundException {
        FixtureRuleProblem problem = new FixtureRuleProblem(
                Category.UNUSED, FixtureRule.registered(FixtureRuleKind.IGNORE, "a.b", 2), "because");
        FixtureRuleException original = new FixtureRuleException(String.class, List.of("c"), List.of(problem));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        FixtureRuleException copy;
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            copy = (FixtureRuleException) in.readObject();
        }

        assertEquals(original.getMessage(), copy.getMessage());
        assertEquals(List.of(problem), copy.getProblems());
        assertEquals(List.of("c"), copy.getUndecidedProperties());
    }
}
