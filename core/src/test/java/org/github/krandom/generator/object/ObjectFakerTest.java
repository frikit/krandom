/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.object;

import org.github.krandom.generator.object.exception.ObjectGenerationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ObjectFaker")
class ObjectFakerTest {

    @Test
    @DisplayName("static factory returns an ObjectFaker")
    void staticFactoryReturnsObjectFaker() {
        assertInstanceOf(ObjectFaker.class, ObjectFaker.of(FixtureUser.class));
    }

    @Test
    @DisplayName("ruleFor applies deterministic root field rules")
    void ruleForAppliesBaseRules() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("firstName", () -> "Ada")
            .ruleFor("lastName", () -> "Lovelace")
            .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("Lovelace", user.lastName);
        assertNotNull(user.email);
    }

    @Test
    @DisplayName("dependent rule can use previously generated root values")
    void dependentRuleUsesGeneratedRootValues() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("firstName", () -> "Ada")
            .ruleFor("lastName", () -> "Lovelace")
            .ruleFor("email", generated -> generated.firstName.toLowerCase()
                                            + "." + generated.lastName.toLowerCase() + "@example.com")
            .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("Lovelace", user.lastName);
        assertEquals("ada.lovelace@example.com", user.email);
    }

    @Test
    @DisplayName("context rules can use generation metadata")
    void contextRulesCanUseGenerationMetadata() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ruleForContext("email", ctx -> ctx.getOwnerType().getSimpleName().toLowerCase() + "-" + ctx.getFieldName())
            .generate();

        assertEquals("fixtureuser-email", user.email);
    }

    @Test
    @DisplayName("ignore leaves the configured field untouched")
    void ignoreLeavesFieldUntouched() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ignore("email")
            .generate();

        assertNull(user.email);
        assertNotNull(user.firstName);
    }

    @Test
    @DisplayName("ignore varargs excludes multiple fields")
    void ignoreVarargsExcludesMultipleFields() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ignore("firstName", "email")
            .generate();

        assertNull(user.firstName);
        assertNull(user.email);
        assertNotNull(user.lastName);
    }

    @Test
    @DisplayName("postProcess can mutate the generated object")
    void postProcessMutatesGeneratedObject() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("firstName", () -> "Ada")
            .afterGenerate(generated -> generated.email = generated.firstName.toLowerCase() + "@example.com")
            .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("ada@example.com", user.email);
    }

    @Test
    @DisplayName("populate fills an existing mutable instance")
    void populateFillsExistingInstance() {
        ObjectGeneratorConfig config = ObjectGeneratorConfig.builder()
            .overrideDefaultInitialization(true)
            .build();

        FixtureUser existing = new FixtureUser();
        existing.firstName = "legacy";

        FixtureUser populated = new ObjectFaker<>(FixtureUser.class, config)
            .ruleFor("firstName", () -> "Ada")
            .ruleFor("lastName", () -> "Lovelace")
            .populate(existing);

        assertEquals(existing, populated);
        assertEquals("Ada", existing.firstName);
        assertEquals("Lovelace", existing.lastName);
    }

    @Test
    @DisplayName("populate rejects record instances")
    void populateRejectsRecordInstances() {
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> new ObjectFaker<>(FixtureRecord.class).populate(new FixtureRecord("Ada", "ada@example.com")));

        assertTrue(ex.getMessage().contains("does not support records"));
    }

    @Test
    @DisplayName("populate rejects incompatible instances")
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void populateRejectsIncompatibleInstances() {
        ObjectFaker raw = new ObjectFaker(FixtureUser.class);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> raw.populate(new WrongFixtureType()));

        assertTrue(ex.getMessage().contains("instance must be assignable"));
    }

    @Test
    @DisplayName("dependent rules rebuild records")
    void dependentRulesRebuildRecords() {
        FixtureRecord record = new ObjectFaker<>(FixtureRecord.class)
            .ruleFor("firstName", () -> "Ada")
            .ruleFor("email", generated -> generated.firstName().toLowerCase() + "@example.com")
            .generate();

        assertEquals("Ada", record.firstName());
        assertEquals("ada@example.com", record.email());
    }

    @Test
    @DisplayName("generateList reuses one configured faker")
    void generateListUsesConfiguredFaker() {
        List<FixtureUser> users = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("firstName", () -> "Ada")
            .generateList(3);

        assertEquals(3, users.size());
        assertTrue(users.stream().allMatch(user -> "Ada".equals(user.firstName)));
    }

    @Test
    @DisplayName("invalid dependent assignments are wrapped as generation failures")
    void invalidDependentAssignmentsAreWrapped() {
        ObjectGenerationException ex = assertThrows(
            ObjectGenerationException.class,
            () -> new ObjectFaker<>(PrimitiveFixture.class)
                .ruleFor("age", generated -> null)
                .generate());

        assertTrue(ex.getMessage().contains("Failed to apply fixture rule"));
    }

    @Test
    @DisplayName("duplicate rules are rejected")
    void duplicateRulesAreRejected() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("firstName", () -> "Ada");

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> faker.ruleFor("firstName", () -> "Grace"));

        assertTrue(ex.getMessage().contains("already has a registered rule"));
    }

    @Test
    @DisplayName("ignored fields cannot later receive rules")
    void ignoredFieldsCannotReceiveRules() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
            .ignore("email");

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> faker.ruleFor("email", () -> "ada@example.com"));

        assertTrue(ex.getMessage().contains("already ignored"));
    }

    @Test
    @DisplayName("context rules participate in duplicate validation")
    void contextRulesParticipateInDuplicateValidation() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
            .ruleForContext("email", ctx -> "ctx");

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> faker.ruleFor("email", generated -> "derived"));

        assertTrue(ex.getMessage().contains("already has a registered rule"));
    }

    @Test
    @DisplayName("dependent rules participate in duplicate validation")
    void dependentRulesParticipateInDuplicateValidation() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
            .ruleFor("email", generated -> "derived");

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> faker.ruleForContext("email", ctx -> "ctx"));

        assertTrue(ex.getMessage().contains("already has a registered rule"));
    }

    @Test
    @DisplayName("final fields are rejected")
    void finalFieldsAreRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectFaker<>(FinalFieldFixture.class).ruleFor("locked", () -> "value"));

        assertTrue(ex.getMessage().contains("is final"));
    }

    @Test
    @DisplayName("ambiguous shadowed fields are rejected")
    void ambiguousShadowedFieldsAreRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectFaker<>(ChildShadowFixture.class).ruleFor("value", () -> "x"));

        assertTrue(ex.getMessage().contains("ambiguous"));
    }

    @Test
    @DisplayName("unknown fields are rejected")
    void unknownFieldsAreRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectFaker<>(FixtureUser.class).ruleFor("missing", () -> "x"));

        assertTrue(ex.getMessage().contains("Unknown field"));
    }

    @Test
    @DisplayName("unknown record components are rejected")
    void unknownRecordComponentsAreRejected() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> new ObjectFaker<>(FixtureRecord.class).ruleFor("missing", () -> "x"));

        assertTrue(ex.getMessage().contains("Unknown record component"));
    }

    static final class FixtureUser {
        String firstName;
        String lastName;
        String email;
    }

    static final class PrimitiveFixture {
        int age;
    }

    static final class WrongFixtureType {
        String value;
    }

    static final class FinalFieldFixture {
        final String locked = "keep";
    }

    static class ParentShadowFixture {
        String value;
    }

    static final class ChildShadowFixture extends ParentShadowFixture {
        String value;
    }

    record FixtureRecord(String firstName, String email) {
    }
}
