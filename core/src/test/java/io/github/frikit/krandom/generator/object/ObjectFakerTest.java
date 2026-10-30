/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import jakarta.validation.constraints.NotNull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectFaker")
class ObjectFakerTest {

    @Test
    @DisplayName("static factory returns an ObjectFaker")
    void staticFactoryReturnsObjectFaker() {
        assertInstanceOf(ObjectFaker.class, ObjectFaker.of(FixtureUser.class));
    }

    @Test
    @DisplayName("ObjectGeneratorConfig can project into the public GeneratorConfig path")
    void objectConfigProjectsIntoPublicGeneratorConfigPath() {
        FixtureUser user = new ObjectFaker<>(
                        FixtureUser.class,
                        ObjectGeneratorConfig.builder().build().toGeneratorConfig())
                .generate();
        assertNotNull(user);
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
    @DisplayName("ruleFor replaces a field initializer")
    void ruleForReplacesFieldInitializer() {
        Basket basket = new ObjectFaker<>(Basket.class)
                .ruleFor("products", () -> List.of("apple", "pear"))
                .generate();

        assertEquals(List.of("apple", "pear"), basket.products);
        assertEquals("keep", basket.note, "a field without a rule keeps its initializer");
    }

    @Test
    @DisplayName("ruleFor replaces the initializer of an inherited field")
    void ruleForReplacesInheritedFieldInitializer() {
        TaggedChild child = new ObjectFaker<>(TaggedChild.class)
                .ruleFor("tags", () -> List.of("red"))
                .generate();

        assertEquals(List.of("red"), child.tags);
    }

    @Test
    @DisplayName("a rule on an inherited semantic field survives semantic coherence")
    void ruleOnInheritedSemanticFieldSurvivesCoherence() {
        InheritedContact contact = new ObjectFaker<>(InheritedContact.class)
                .ruleFor("email", () -> "rule@example.org")
                .generate();

        assertEquals("rule@example.org", contact.email);
        assertNotNull(contact.firstName);
        assertNotNull(contact.lastName);
    }

    @Test
    @DisplayName("strict mode generates after every writable field is configured")
    void strictModeGeneratesCompleteConfiguration() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("lastName", () -> "Lovelace")
                .ruleFor("email", () -> "ada@example.com")
                .strict()
                .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("Lovelace", user.lastName);
        assertEquals("ada@example.com", user.email);
    }

    @Test
    @DisplayName("dependent rule can use previously generated root values")
    void dependentRuleUsesGeneratedRootValues() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("lastName", () -> "Lovelace")
                .ruleFor(
                        "email",
                        generated -> generated.firstName.toLowerCase() + "." + generated.lastName.toLowerCase()
                                + "@example.com")
                .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("Lovelace", user.lastName);
        assertEquals("ada.lovelace@example.com", user.email);
    }

    @Test
    @DisplayName("context rules can use generation metadata")
    void contextRulesCanUseGenerationMetadata() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleForContext(
                        "email", ctx -> ctx.getOwnerType().getSimpleName().toLowerCase() + "-" + ctx.getFieldName())
                .generate();

        assertEquals("fixtureuser-email", user.email);
    }

    @Test
    @DisplayName("ignore leaves the configured field untouched")
    void ignoreLeavesFieldUntouched() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class).ignore("email").generate();

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
    @DisplayName("duplicate ignore paths are idempotent")
    void duplicateIgnorePathsAreIdempotent() {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class);

        assertSame(faker, faker.ignore("address.city"));
        assertSame(faker, faker.ignore("address.city"));
    }

    @Test
    @DisplayName("include restricts generation to selected root fields")
    void includeRestrictsGenerationToSelectedFields() {
        FixtureUser user =
                new ObjectFaker<>(FixtureUser.class).include("firstName").generate();

        assertNotNull(user.firstName);
        assertNull(user.lastName);
        assertNull(user.email);
    }

    @Test
    @DisplayName("fields with explicit rules remain active in include mode")
    void includeModeKeepsRuleDrivenFieldsActive() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .include("firstName")
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("email", generated -> generated.firstName.toLowerCase() + "@example.com")
                .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("ada@example.com", user.email);
        assertNull(user.lastName);
    }

    @Test
    @DisplayName("include mode can still ignore other non-included fields")
    void includeModeCanIgnoreSeparateFields() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .include("firstName")
                .ignore("lastName")
                .ruleFor("firstName", () -> "Ada")
                .generate();

        assertEquals("Ada", user.firstName);
        assertNull(user.lastName);
        assertNull(user.email);
    }

    @Test
    @DisplayName("include mode works for record fixtures")
    void includeModeWorksForRecords() {
        FixtureRecord record =
                new ObjectFaker<>(FixtureRecord.class).include("firstName").generate();

        assertNotNull(record.firstName());
        assertNull(record.email());
    }

    @Test
    @DisplayName("include mode respects inherited and static fields correctly")
    void includeModeRespectsInheritedAndStaticFields() {
        HierarchyChildFixture fixture = new ObjectFaker<>(HierarchyChildFixture.class)
                .include("childValue")
                .generate();

        assertNotNull(fixture.childValue);
        assertNull(fixture.parentValue);
    }

    @Test
    @DisplayName("ignore mode respects inherited fields correctly")
    void ignoreModeRespectsInheritedFields() {
        HierarchyChildFixture fixture = new ObjectFaker<>(HierarchyChildFixture.class)
                .ignore("parentValue")
                .generate();

        assertNotNull(fixture.childValue);
        assertNull(fixture.parentValue);
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
    @DisplayName("named profiles can bundle reusable fixture rules")
    void namedProfilesBundleReusableRules() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .profile(
                        "minimal",
                        faker -> faker.include("firstName", "email")
                                .ruleFor("firstName", () -> "Ada")
                                .ruleFor("email", generated -> generated.firstName.toLowerCase() + "@example.com"))
                .useProfile("minimal")
                .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("ada@example.com", user.email);
        assertNull(user.lastName);
    }

    @Test
    @DisplayName("useProfile applies multiple named profiles in order")
    void useProfileAppliesMultipleProfilesInOrder() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .profile(
                        "names",
                        faker -> faker.ruleFor("firstName", () -> "Ada").ruleFor("lastName", () -> "Lovelace"))
                .profile(
                        "email",
                        faker -> faker.ruleFor(
                                "email",
                                generated -> generated.firstName.toLowerCase() + "." + generated.lastName.toLowerCase()
                                        + "@example.com"))
                .useProfile("names", "email")
                .generate();

        assertEquals("Ada", user.firstName);
        assertEquals("Lovelace", user.lastName);
        assertEquals("ada.lovelace@example.com", user.email);
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    void populateRejectsIncompatibleInstances() {
        ObjectFaker raw = new ObjectFaker(FixtureUser.class);

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> raw.populate(new WrongFixtureType()));

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

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
        assertEquals(GenerationOperation.APPLY_RULE, context.operation());
        assertEquals("PrimitiveFixture.age", context.path());
        assertEquals(int.class.getTypeName(), context.declaredType());
        assertEquals(0, context.depth());
        assertTrue(ex.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("a later rule replaces an earlier rule for the same field")
    void laterRuleReplacesEarlierRule() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("firstName", () -> "Grace")
                .generate();

        assertEquals("Grace", user.firstName);
    }

    @Test
    @DisplayName("ignored fields cannot later receive any kind of rule")
    void ignoredFieldsCannotReceiveRules() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).ignore("email");

        IllegalStateException plain =
                assertThrows(IllegalStateException.class, () -> faker.ruleFor("email", () -> "ada@example.com"));
        IllegalStateException contextual =
                assertThrows(IllegalStateException.class, () -> faker.ruleForContext("email", ctx -> "ctx"));
        IllegalStateException dependent =
                assertThrows(IllegalStateException.class, () -> faker.ruleFor("email", generated -> "derived"));

        assertTrue(plain.getMessage().contains("already ignored"));
        assertTrue(contextual.getMessage().contains("already ignored"));
        assertTrue(dependent.getMessage().contains("already ignored"));
    }

    @Test
    @DisplayName("a field with any kind of rule cannot later be ignored")
    void fieldsWithRulesCannotLaterBeIgnored() {
        ObjectFaker<FixtureUser> plain = new ObjectFaker<>(FixtureUser.class).ruleFor("email", () -> "plain");
        ObjectFaker<FixtureUser> contextual =
                new ObjectFaker<>(FixtureUser.class).ruleForContext("email", ctx -> "ctx");
        ObjectFaker<FixtureUser> dependent =
                new ObjectFaker<>(FixtureUser.class).ruleFor("email", generated -> "derived");

        for (ObjectFaker<FixtureUser> faker : List.of(plain, contextual, dependent)) {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("email"));
            assertTrue(ex.getMessage().contains("already has a registered rule"));
        }
    }

    @Test
    @DisplayName("included fields cannot later be ignored")
    void includedFieldsCannotLaterBeIgnored() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).include("email");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("email"));

        assertTrue(ex.getMessage().contains("already included"));
    }

    @Test
    @DisplayName("ignored fields cannot later be included")
    void ignoredFieldsCannotLaterBeIncluded() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).ignore("email");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.include("email"));

        assertTrue(ex.getMessage().contains("already ignored"));
    }

    @Test
    @DisplayName("a dependent rule replaces a context rule")
    void dependentRuleReplacesContextRule() {
        AtomicInteger contextCalls = new AtomicInteger();
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleForContext("email", ctx -> {
                    contextCalls.incrementAndGet();
                    return "ctx";
                })
                .ruleFor("email", generated -> "derived")
                .generate();

        assertEquals("derived", user.email);
        assertEquals(0, contextCalls.get(), "the replaced context rule must not run");
    }

    @Test
    @DisplayName("a context rule replaces a dependent rule")
    void contextRuleReplacesDependentRule() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("email", generated -> "derived")
                .ruleForContext("email", ctx -> "ctx")
                .generate();

        assertEquals("ctx", user.email);
    }

    @Test
    @DisplayName("a base rule replaces a dependent rule")
    void baseRuleReplacesDependentRule() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("email", generated -> "derived")
                .ruleFor("email", () -> "plain@example.com")
                .generate();

        assertEquals("plain@example.com", user.email);
    }

    @Test
    @DisplayName("a dependent rule that replaces a base rule drops it from the cached generator")
    void dependentRuleReplacingBaseRuleDropsItFromCachedGenerator() {
        AtomicInteger baseCalls = new AtomicInteger();
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).ruleFor("firstName", () -> {
            baseCalls.incrementAndGet();
            return "Ada";
        });
        assertEquals("Ada", faker.generate().firstName);

        faker.ruleFor("firstName", user -> "Grace");

        assertEquals("Grace", faker.generate().firstName);
        assertEquals(1, baseCalls.get(), "the replaced base rule must not run again");
    }

    @Test
    @DisplayName("a replaced dependent rule keeps its place in the evaluation order")
    void replacedDependentRuleKeepsItsPlace() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("lastName", generated -> "Lovelace")
                .ruleFor("email", generated -> generated.lastName + "@example.com")
                .ruleFor("lastName", generated -> "Hopper")
                .generate();

        assertEquals("Hopper", user.lastName);
        assertEquals("Hopper@example.com", user.email);
    }

    @Test
    @DisplayName("a new dependent rule keeps the seeded sequence of a started faker")
    void newDependentRuleKeepsSeededSequence() {
        GeneratorConfig config = GeneratorConfig.builder().seed(7L).build();
        ObjectFaker<FixtureUser> control = new ObjectFaker<>(FixtureUser.class, config);
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class, config);
        control.generate();
        faker.generate();

        faker.ruleFor("email", generated -> "derived@example.com");

        FixtureUser expected = control.generate();
        FixtureUser actual = faker.generate();
        assertEquals(expected.firstName, actual.firstName);
        assertEquals(expected.lastName, actual.lastName);
        assertEquals("derived@example.com", actual.email);
    }

    @Test
    @DisplayName("a dependent rule that replaces a context rule drops it from the cached generator")
    void dependentRuleReplacingContextRuleDropsItFromCachedGenerator() {
        AtomicInteger contextCalls = new AtomicInteger();
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).ruleForContext("firstName", ctx -> {
            contextCalls.incrementAndGet();
            return "Ada";
        });
        assertEquals("Ada", faker.generate().firstName);

        faker.ruleFor("firstName", user -> "Grace");

        assertEquals("Grace", faker.generate().firstName);
        assertEquals(1, contextCalls.get(), "the replaced context rule must not run again");
    }

    @Test
    @DisplayName("an ignored nested path, or a path below an ignored field, cannot receive a rule")
    void ignoredNestedPathsCannotReceiveRules() {
        ObjectFaker<FixtureUserWithAddress> nested =
                new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address.city");
        ObjectFaker<FixtureUserWithAddress> below = new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address");

        IllegalStateException exact =
                assertThrows(IllegalStateException.class, () -> nested.ruleFor("address.city", () -> "Paris"));
        IllegalStateException child =
                assertThrows(IllegalStateException.class, () -> below.ruleFor("address.city", () -> "Paris"));

        assertTrue(exact.getMessage().contains("'address.city' is already ignored"));
        assertTrue(child.getMessage().contains("'address' is already ignored"));
        assertSame(nested, nested.ruleFor("address", () -> new FixtureAddress()), "an ancestor rule stays legal");
    }

    @Test
    @DisplayName("a dependent rule added after a generation in include mode generates its field")
    void dependentRuleAfterGenerationInIncludeModeGeneratesItsField() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).include("firstName");
        assertNull(faker.generate().email);

        faker.ruleFor("email", user -> user.email + "!");

        String email = faker.generate().email;
        assertNotEquals("null!", email, "the rebuilt generator includes the newly ruled field");
        assertTrue(email.endsWith("!"));
    }

    @Test
    @DisplayName("rules and ignores registered after a generation apply to the next object")
    void rulesRegisteredAfterGenerationApplyToNextObject() {
        ObjectFaker<FixtureUser> plain = new ObjectFaker<>(FixtureUser.class);
        assertNotNull(plain.generate().firstName);
        plain.ruleFor("firstName", () -> "Late");
        assertEquals("Late", plain.generate().firstName);

        ObjectFaker<FixtureUser> contextual = new ObjectFaker<>(FixtureUser.class);
        assertNotNull(contextual.generate().firstName);
        contextual.ruleForContext("firstName", ctx -> "Context");
        assertEquals("Context", contextual.generate().firstName);

        ObjectFaker<FixtureUser> ignoring = new ObjectFaker<>(FixtureUser.class);
        assertNotNull(ignoring.generate().email);
        ignoring.ignore("email");
        assertNull(ignoring.generate().email);
    }

    @Test
    @DisplayName("a nested path below an ignored root cannot be ignored again")
    void nestedPathBelowIgnoredRootCannotBeIgnored() {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("address.city"));

        assertTrue(ex.getMessage().contains("'address' is already ignored"));
    }

    @Test
    @DisplayName("a root field cannot be ignored while a nested include targets it")
    void rootFieldCannotBeIgnoredWhileNestedIncludeTargetsIt() {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).include("address.city");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("address"));

        assertTrue(ex.getMessage().contains("nested include rules already target 'address.city'"));
    }

    @Test
    @DisplayName("a rule refines a field that an applied profile already set")
    void ruleRefinesFieldSetByAppliedProfile() {
        FixtureUser user = new ObjectFaker<>(FixtureUser.class)
                .profile(
                        "defaults",
                        faker -> faker.ruleFor("firstName", () -> "Ada").ruleFor("lastName", () -> "Lovelace"))
                .useProfile("defaults")
                .ruleFor("firstName", () -> "Grace")
                .generate();

        assertEquals("Grace", user.firstName);
        assertEquals("Lovelace", user.lastName);
    }

    @Test
    @DisplayName("nested ruleFor applies deterministic nested field rules")
    void nestedRuleForAppliesDeterministicNestedFieldRules() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("address.city", () -> "London")
                .ruleFor("address.postalCode", generated -> generated.address.city + "-1")
                .generate();

        assertEquals("Ada", user.firstName);
        assertNotNull(user.address);
        assertEquals("London", user.address.city);
        assertEquals("London-1", user.address.postalCode);
    }

    @Test
    @DisplayName("nested context rules receive nested owner metadata")
    void nestedContextRulesReceiveNestedOwnerMetadata() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .ruleForContext(
                        "address.city",
                        ctx -> ctx.getOwnerType().getSimpleName().toLowerCase() + "-" + ctx.getFieldName() + "-"
                                + ctx.getDepth())
                .generate();

        assertNotNull(user.address);
        assertEquals("fixtureaddress-city-1", user.address.city);
    }

    @Test
    @DisplayName("nested rules rebuild record paths when needed")
    void nestedRulesRebuildRecordPathsWhenNeeded() {
        FixtureRecordWithNestedRecord record = new ObjectFaker<>(FixtureRecordWithNestedRecord.class)
                .ruleForContext(
                        "address.city",
                        context -> "Paris-"
                                + context.getDeclaration()
                                        .orElseThrow()
                                        .getClass()
                                        .getSimpleName())
                .ruleFor("address.postalCode", generated -> generated.address().city() + "-75000")
                .generate();

        assertNotNull(record.address());
        assertEquals("Paris-Method", record.address().city());
        assertEquals("Paris-Method-75000", record.address().postalCode());
    }

    @Test
    @DisplayName("nested populate materializes missing parents")
    void nestedPopulateMaterializesMissingParents() {
        FixtureUserWithAddress existing = new FixtureUserWithAddress();

        FixtureUserWithAddress populated = new ObjectFaker<>(FixtureUserWithAddress.class)
                .ruleFor("address.city", () -> "Berlin")
                .populate(existing);

        assertSame(existing, populated);
        assertNotNull(existing.address);
        assertEquals("Berlin", existing.address.city);
    }

    @Test
    @DisplayName("nested assignment helpers materialize missing parents and reuse mutable parents")
    void nestedAssignmentHelpersMaterializeMissingParentsAndReuseMutableParents() throws Exception {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class);
        Method resolveRulePath = ObjectFaker.class.getDeclaredMethod("resolveRulePath", String.class);
        resolveRulePath.setAccessible(true);
        Object path = resolveRulePath.invoke(faker, "address.city");
        Class<?> rulePathType = Class.forName("io.github.frikit.krandom.generator.object.ObjectFaker$RulePath");
        Method assignNestedValue = ObjectFaker.class.getDeclaredMethod(
                "assignNestedValue", Object.class, rulePathType, int.class, Object.class);
        assignNestedValue.setAccessible(true);

        FixtureUserWithAddress missingAddress = new FixtureUserWithAddress();
        FixtureUserWithAddress materialized =
                (FixtureUserWithAddress) assignNestedValue.invoke(faker, missingAddress, path, 0, "Berlin");
        assertSame(missingAddress, materialized);
        assertNotNull(missingAddress.address);
        assertEquals("Berlin", missingAddress.address.city);

        FixtureUserWithAddress existingAddressHolder = new FixtureUserWithAddress();
        existingAddressHolder.address = new FixtureAddress();
        FixtureAddress existingAddress = existingAddressHolder.address;
        FixtureUserWithAddress reused =
                (FixtureUserWithAddress) assignNestedValue.invoke(faker, existingAddressHolder, path, 0, "Paris");
        assertSame(existingAddressHolder, reused);
        assertSame(existingAddress, existingAddressHolder.address);
        assertEquals("Paris", existingAddressHolder.address.city);
    }

    @Test
    @DisplayName("nested rules keep their root object active in include mode")
    void nestedRulesKeepTheirRootObjectActiveInIncludeMode() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .include("firstName")
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("address.city", () -> "Rome")
                .generate();

        assertEquals("Ada", user.firstName);
        assertNull(user.lastName);
        assertNotNull(user.address);
        assertEquals("Rome", user.address.city);
    }

    @Test
    @DisplayName("nested include prunes sibling fields under the selected root")
    void nestedIncludePrunesSiblingFields() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .include("address.city")
                .generate();

        assertNull(user.firstName);
        assertNull(user.lastName);
        assertNotNull(user.address);
        assertNotNull(user.address.city);
        assertNull(user.address.postalCode);
        assertEquals(0, user.address.houseNumber);
    }

    @Test
    @DisplayName("nested include prunes nested record components too")
    void nestedIncludePrunesNestedRecordComponents() {
        FixtureRecordWithNestedRecord record = new ObjectFaker<>(FixtureRecordWithNestedRecord.class)
                .include("address.city")
                .generate();

        assertNull(record.firstName());
        assertNotNull(record.address());
        assertNotNull(record.address().city());
        assertNull(record.address().postalCode());
    }

    @Test
    @DisplayName("nested ignore clears only the targeted nested field")
    void nestedIgnoreClearsOnlyTargetedField() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .ignore("address.city")
                .generate();

        assertNotNull(user.address);
        assertNull(user.address.city);
        assertNotNull(user.address.postalCode);
    }

    @Test
    @DisplayName("nested ignore skips missing parent paths during populate")
    void nestedIgnoreSkipsMissingParentPathsDuringPopulate() throws Exception {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address.city");
        FixtureUserWithAddress existing = new FixtureUserWithAddress();

        Method resolveRulePath = ObjectFaker.class.getDeclaredMethod("resolveRulePath", String.class);
        resolveRulePath.setAccessible(true);
        Object path = resolveRulePath.invoke(faker, "address.city");
        Class<?> rulePathType = Class.forName("io.github.frikit.krandom.generator.object.ObjectFaker$RulePath");
        Method clearFieldValue = ObjectFaker.class.getDeclaredMethod("clearFieldValue", Object.class, rulePathType);
        clearFieldValue.setAccessible(true);

        FixtureUserWithAddress cleared = (FixtureUserWithAddress) clearFieldValue.invoke(faker, existing, path);
        assertSame(existing, cleared);
        assertNull(existing.address);
    }

    @Test
    @DisplayName("root include can still combine with nested ignore")
    void rootIncludeCanCombineWithNestedIgnore() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .include("address")
                .ignore("address.city")
                .generate();

        assertNull(user.firstName);
        assertNull(user.lastName);
        assertNotNull(user.address);
        assertNull(user.address.city);
        assertNotNull(user.address.postalCode);
    }

    @Test
    @DisplayName("ignored roots cannot later receive nested rules")
    void ignoredRootsCannotLaterReceiveNestedRules() {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address");

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> faker.ruleFor("address.city", () -> "Madrid"));

        assertTrue(ex.getMessage().contains("already ignored"));
    }

    @Test
    @DisplayName("root fields with nested rules cannot later be ignored")
    void nestedRuleRootsCannotLaterBeIgnored() {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).ruleFor("address.city", () -> "Madrid");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("address"));

        assertTrue(ex.getMessage().contains("nested fixture rules"));
    }

    @Test
    @DisplayName("nested include roots cannot later be ignored")
    void nestedIncludeRootsCannotLaterBeIgnored() {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).include("address.city");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("address"));

        assertTrue(ex.getMessage().contains("nested include")
                || ex.getMessage().contains("nested fixture rules")
                || ex.getMessage().contains("already included"));
    }

    @Test
    @DisplayName("root include plus nested include keeps the wider root payload")
    void rootIncludePlusNestedIncludeKeepsWiderRootPayload() {
        FixtureUserWithAddress user = new ObjectFaker<>(FixtureUserWithAddress.class)
                .include("address")
                .include("address.city")
                .generate();

        assertNotNull(user.address);
        assertNotNull(user.address.city);
        assertNotNull(user.address.postalCode);
    }

    @Test
    @DisplayName("ignored roots cannot later receive nested include paths")
    void ignoredRootsCannotLaterReceiveNestedIncludePaths() {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class).ignore("address");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.include("address.city"));

        assertTrue(ex.getMessage().contains("already ignored"));
    }

    @Test
    @DisplayName("exact nested include and ignore conflicts are rejected")
    void exactNestedIncludeAndIgnoreConflictsAreRejected() {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).include("address.city");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.ignore("address.city"));

        assertTrue(ex.getMessage().contains("already included"));
    }

    @Test
    @DisplayName("nested primitive paths are rejected")
    void nestedPrimitivePathsAreRejected() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectFaker<>(FixtureUserWithAddress.class).ruleFor("address.houseNumber.value", () -> 7));

        assertTrue(ex.getMessage().contains("crosses primitive"));
    }

    @Test
    @DisplayName("blank nested path segments are rejected")
    void blankNestedPathSegmentsAreRejected() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new ObjectFaker<>(FixtureUserWithAddress.class).ruleFor("address..city", () -> "Rome"));

        assertTrue(ex.getMessage().contains("Invalid field path"));
    }

    @Test
    @DisplayName("nested include helper methods cover pruning edge branches and primitive defaults")
    void nestedIncludeHelperMethodsCoverPruningEdgesAndPrimitiveDefaults() throws Exception {
        ObjectFaker<FixtureUserWithAddress> faker =
                new ObjectFaker<>(FixtureUserWithAddress.class).include("address.city");

        Method buildIncludeTree = ObjectFaker.class.getDeclaredMethod("buildIncludeTree");
        buildIncludeTree.setAccessible(true);
        Object includeTree = buildIncludeTree.invoke(faker);
        Class<?> includeNodeType = Class.forName("io.github.frikit.krandom.generator.object.ObjectFaker$IncludeNode");

        Method pruneToIncludedPaths =
                ObjectFaker.class.getDeclaredMethod("pruneToIncludedPaths", Object.class, includeNodeType);
        pruneToIncludedPaths.setAccessible(true);
        assertNull(pruneToIncludedPaths.invoke(faker, null, includeTree));

        Field childrenField = includeNodeType.getDeclaredField("children");
        childrenField.setAccessible(true);
        Object addressNode = ((java.util.Map<?, ?>) childrenField.get(includeTree)).get("address");
        assertNotNull(addressNode);

        FixtureAddress address = new FixtureAddress();
        address.city = "Rome";
        address.postalCode = "00100";
        address.houseNumber = 7;
        FixtureAddress prunedAddress = (FixtureAddress) pruneToIncludedPaths.invoke(faker, address, addressNode);
        assertSame(address, prunedAddress);
        assertEquals("Rome", prunedAddress.city);
        assertNull(prunedAddress.postalCode);
        assertEquals(0, prunedAddress.houseNumber);

        ObjectFaker<FixtureUserWithAddress> keepAllFaker =
                new ObjectFaker<>(FixtureUserWithAddress.class).include("address");
        Object keepAllTree = buildIncludeTree.invoke(keepAllFaker);
        Object keepAllAddressNode = ((java.util.Map<?, ?>) childrenField.get(keepAllTree)).get("address");
        assertSame(address, pruneToIncludedPaths.invoke(keepAllFaker, address, keepAllAddressNode));

        Constructor<?> includeNodeConstructor = includeNodeType.getDeclaredConstructor();
        includeNodeConstructor.setAccessible(true);
        Object emptyNode = includeNodeConstructor.newInstance();
        assertSame(address, pruneToIncludedPaths.invoke(faker, address, emptyNode));

        Object syntheticNode = includeNodeConstructor.newInstance();
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> syntheticChildren =
                (java.util.Map<String, Object>) childrenField.get(syntheticNode);
        Object danglingChild = includeNodeConstructor.newInstance();
        syntheticChildren.put("firstName", danglingChild);
        FixtureUser syntheticUser = new FixtureUser();
        syntheticUser.firstName = "Ada";
        assertSame(syntheticUser, pruneToIncludedPaths.invoke(faker, syntheticUser, syntheticNode));

        Method supportsNestedPruning = ObjectFaker.class.getDeclaredMethod("supportsNestedPruning", Class.class);
        supportsNestedPruning.setAccessible(true);
        assertEquals(Boolean.TRUE, supportsNestedPruning.invoke(null, FixtureAddress.class));
        assertEquals(Boolean.TRUE, supportsNestedPruning.invoke(null, Class.forName("PackageLessFixture")));
        assertEquals(Boolean.FALSE, supportsNestedPruning.invoke(null, String.class));
        assertEquals(Boolean.FALSE, supportsNestedPruning.invoke(null, int.class));
        assertEquals(Boolean.FALSE, supportsNestedPruning.invoke(null, int[].class));
        assertEquals(Boolean.FALSE, supportsNestedPruning.invoke(null, Thread.State.class));

        Method defaultValue = ObjectFaker.class.getDeclaredMethod("defaultValue", Class.class);
        defaultValue.setAccessible(true);
        assertEquals(false, defaultValue.invoke(null, boolean.class));
        assertEquals((byte) 0, defaultValue.invoke(null, byte.class));
        assertEquals((short) 0, defaultValue.invoke(null, short.class));
        assertEquals(0, defaultValue.invoke(null, int.class));
        assertEquals(0L, defaultValue.invoke(null, long.class));
        assertEquals(0f, defaultValue.invoke(null, float.class));
        assertEquals(0d, defaultValue.invoke(null, double.class));
        assertEquals('\0', defaultValue.invoke(null, char.class));
        IllegalArgumentException defaultEx = assertThrows(IllegalArgumentException.class, () -> {
            try {
                defaultValue.invoke(null, void.class);
            } catch (ReflectiveOperationException e) {
                throw e.getCause();
            }
        });
        assertTrue(defaultEx.getMessage().contains("Unsupported primitive type"));

        Method resolveRulePath = ObjectFaker.class.getDeclaredMethod("resolveRulePath", String.class);
        resolveRulePath.setAccessible(true);
        Object path = resolveRulePath.invoke(faker, "address.city");
        Class<?> rulePathType = Class.forName("io.github.frikit.krandom.generator.object.ObjectFaker$RulePath");
        Method fieldName = rulePathType.getDeclaredMethod("fieldName");
        fieldName.setAccessible(true);
        assertEquals("city", fieldName.invoke(path));
    }

    @Test
    @DisplayName("nested include wraps reflective pruning failures")
    void nestedIncludeWrapsReflectivePruningFailures() throws Exception {
        ObjectFaker<FixtureUserWithBrokenAddress> faker =
                new ObjectFaker<>(FixtureUserWithBrokenAddress.class).include("address.city");
        Method applyNestedIncludeRules = ObjectFaker.class.getDeclaredMethod("applyNestedIncludeRules", Object.class);
        applyNestedIncludeRules.setAccessible(true);
        FixtureUserWithBrokenAddress user = new FixtureUserWithBrokenAddress();
        user.address = new BrokenAddressRecord("Rome", "00100");

        ObjectGenerationException ex = assertThrows(ObjectGenerationException.class, () -> {
            try {
                applyNestedIncludeRules.invoke(faker, user);
            } catch (ReflectiveOperationException e) {
                throw e.getCause();
            }
        });

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, context.category());
        assertEquals(GenerationOperation.APPLY_RULE, context.operation());
        assertEquals("FixtureUserWithBrokenAddress.address.postalCode", context.path());
        assertEquals(BrokenAddressRecord.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertEquals(1, context.depth());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertFalse(ex.getMessage().contains("boom"));
    }

    @Test
    @DisplayName("nested ignore wraps reflective clear failures")
    void nestedIgnoreWrapsReflectiveClearFailures() {
        ObjectGenerationException ex = assertThrows(
                ObjectGenerationException.class,
                () -> new ObjectFaker<>(FixtureUserWithBrokenAddress.class)
                        .ignore("address.city")
                        .ruleFor("address", () -> new BrokenAddressRecord("Rome", "00100"))
                        .generate());

        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.ASSIGNMENT, context.category());
        assertEquals(GenerationOperation.APPLY_RULE, context.operation());
        assertEquals("FixtureUserWithBrokenAddress.address.city", context.path());
        assertEquals(BrokenAddressRecord.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertEquals(1, context.depth());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertFalse(ex.getMessage().contains("boom"));
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

    @Test
    @DisplayName("duplicate profile definitions are rejected")
    void duplicateProfileDefinitionsAreRejected() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .profile("minimal", configured -> configured.ruleFor("firstName", () -> "Ada"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> faker.profile("minimal", configured -> configured.ruleFor("lastName", () -> "Lovelace")));

        assertTrue(ex.getMessage().contains("already defined"));
    }

    @Test
    @DisplayName("unknown profiles are rejected")
    void unknownProfilesAreRejected() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> new ObjectFaker<>(FixtureUser.class).useProfile("missing"));

        assertTrue(ex.getMessage().contains("Unknown profile"));
    }

    @Test
    @DisplayName("profiles cannot be applied twice")
    void profilesCannotBeAppliedTwice() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .profile("minimal", configured -> configured.ruleFor("firstName", () -> "Ada"))
                .useProfile("minimal");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.useProfile("minimal"));

        assertTrue(ex.getMessage().contains("already applied"));
    }

    @Test
    @DisplayName("circular profile usage is rejected")
    void circularProfileUsageIsRejected() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .profile("a", configured -> configured.useProfile("b"))
                .profile("b", configured -> configured.useProfile("a"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> faker.useProfile("a"));

        assertTrue(ex.getMessage().contains("already being applied"));
    }

    @Test
    @DisplayName("assertConfigurationIsValid passes when every root field has a rule")
    void assertConfigurationIsValidPassesWhenAllFieldsRuled() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("lastName", () -> "Lovelace")
                .ruleFor("email", () -> "ada@example.com");

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("assertConfigurationIsValid accepts ignored fields")
    void assertConfigurationIsValidAcceptsIgnoredFields() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .ruleFor("firstName", () -> "Ada")
                .ignore("lastName", "email");

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("assertConfigurationIsValid throws listing missing root fields")
    void assertConfigurationIsValidThrowsForMissingRules() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class).ruleFor("firstName", () -> "Ada");

        IllegalStateException ex = assertThrows(IllegalStateException.class, faker::assertConfigurationIsValid);
        assertTrue(ex.getMessage().contains(FixtureUser.class.getName()));
        assertTrue(ex.getMessage().contains("lastName"));
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    @DisplayName("assertConfigurationIsValid only requires rules for included fields")
    void assertConfigurationIsValidOnlyRequiresIncludedFields() {
        ObjectFaker<FixtureUser> faker =
                new ObjectFaker<>(FixtureUser.class).include("firstName").ruleFor("firstName", () -> "Ada");

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("assertConfigurationIsValid still throws for unruled included fields")
    void assertConfigurationIsValidThrowsForUnruledIncludedFields() {
        ObjectFaker<FixtureUser> faker = new ObjectFaker<>(FixtureUser.class)
                .include("firstName", "lastName")
                .ruleFor("firstName", () -> "Ada");

        IllegalStateException ex = assertThrows(IllegalStateException.class, faker::assertConfigurationIsValid);
        assertTrue(ex.getMessage().contains("lastName"));
    }

    @Test
    @DisplayName("assertConfigurationIsValid treats nested rules as covering the root")
    void assertConfigurationIsValidTreatsNestedRulesAsCoveringRoot() {
        ObjectFaker<FixtureUserWithAddress> faker = new ObjectFaker<>(FixtureUserWithAddress.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("lastName", () -> "Lovelace")
                .ruleFor("address.city", () -> "London");

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("assertConfigurationIsValid validates record components")
    void assertConfigurationIsValidValidatesRecordComponents() {
        ObjectFaker<FixtureRecord> faker = new ObjectFaker<>(FixtureRecord.class).ruleFor("firstName", () -> "Ada");

        IllegalStateException ex = assertThrows(IllegalStateException.class, faker::assertConfigurationIsValid);
        assertTrue(ex.getMessage().contains("email"));
    }

    @Test
    @DisplayName("assertConfigurationIsValid passes for fully ruled records")
    void assertConfigurationIsValidPassesForFullyRuledRecords() {
        ObjectFaker<FixtureRecord> faker = new ObjectFaker<>(FixtureRecord.class)
                .ruleFor("firstName", () -> "Ada")
                .ruleFor("email", () -> "ada@example.com");

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("assertConfigurationIsValid skips final fields automatically")
    void assertConfigurationIsValidSkipsFinalFields() {
        ObjectFaker<FinalFieldFixture> faker = new ObjectFaker<>(FinalFieldFixture.class);

        assertSame(faker, faker.assertConfigurationIsValid());
    }

    @Test
    @DisplayName("a nested rule runs after generation, so a required member at the depth limit still fails")
    void nestedRuleDoesNotPreventRequiredMemberFailureAtDepthLimit() {
        ObjectFaker<RequiredTop> faker = new ObjectFaker<>(
                        RequiredTop.class,
                        GeneratorConfig.builder().objectMaxDepth(1).build())
                .ruleFor("mid.leaf", () -> new RequiredLeaf("rule"));

        ObjectGenerationException error = assertThrows(ObjectGenerationException.class, faker::generate);

        assertEquals("RequiredTop.mid.leaf", error.getContext().orElseThrow().path());
    }

    @Test
    @DisplayName("a larger depth limit lets a nested rule set a required member")
    void nestedRuleSetsRequiredMemberWithinDepthLimit() {
        ObjectFaker<RequiredTop> faker = new ObjectFaker<>(
                        RequiredTop.class,
                        GeneratorConfig.builder().objectMaxDepth(2).build())
                .ruleFor("mid.leaf", () -> new RequiredLeaf("rule"));

        assertEquals("rule", faker.generate().mid().leaf().value());
    }

    static final class FixtureUser {
        String firstName;
        String lastName;
        String email;
    }

    static final class FixtureUserWithAddress {
        String firstName;
        String lastName;
        FixtureAddress address;
    }

    static final class FixtureAddress {
        String city;
        String postalCode;
        int houseNumber;
    }

    static final class PrimitiveFixture {
        int age;
    }

    static final class Basket {
        List<String> products = new ArrayList<>();
        String note = "keep";
    }

    static class TaggedBase {
        List<String> tags = new ArrayList<>();
    }

    static final class TaggedChild extends TaggedBase {
        String label;
    }

    static class ContactBase {
        String email = "preset@example.com";
    }

    static final class InheritedContact extends ContactBase {
        String firstName;
        String lastName;
    }

    static final class FixtureUserWithBrokenAddress {
        BrokenAddressRecord address;
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

    static class HierarchyParentFixture {
        static String ignoredStatic = "STATIC";
        String parentValue;
    }

    static final class HierarchyChildFixture extends HierarchyParentFixture {
        String childValue;
    }

    record FixtureRecord(String firstName, String email) {}

    record FixtureRecordWithNestedRecord(String firstName, FixtureAddressRecord address) {}

    record FixtureAddressRecord(String city, String postalCode) {}

    record RequiredLeaf(String value) {}

    record RequiredMid(@NotNull RequiredLeaf leaf) {}

    record RequiredTop(RequiredMid mid) {}

    record BrokenAddressRecord(String city, String postalCode) {

        @Override
        public String city() {
            throw new IllegalStateException("boom");
        }

        @Override
        public String postalCode() {
            throw new IllegalStateException("boom");
        }
    }
}
