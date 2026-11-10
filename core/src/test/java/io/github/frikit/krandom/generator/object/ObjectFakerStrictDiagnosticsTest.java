/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.FixtureRuleProblem.Category;
import io.github.frikit.krandom.generator.object.exception.FixtureRuleException;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("ObjectFaker.strict() fixture rule diagnostics")
class ObjectFakerStrictDiagnosticsTest {

    // ── Shadowed ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a rule replaced by a later rule for the same path is shadowed, whatever the kinds")
    void replacedRulesAreShadowed() {
        ObjectFaker<Account> faker = new ObjectFaker<>(Account.class)
                .ruleFor("name", () -> "first")
                .ruleForContext("name", context -> "second")
                .ruleFor("name", account -> "third");

        List<FixtureRuleProblem> problems = problemsOf(faker);

        assertEquals(2, problems.size(), problems.toString());
        assertProblem(problems.get(0), Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
        assertEquals(
                "replaced by context rule #2 'name', registered later for the same path",
                problems.get(0).reason());
        assertProblem(problems.get(1), Category.SHADOWED, FixtureRuleKind.CONTEXT_RULE, "name", 2);
        assertEquals(
                "replaced by dependent rule #3 'name', registered later for the same path",
                problems.get(1).reason());
    }

    @Test
    @DisplayName("a rule that a profile or a composed model replaces is shadowed, numbered across them")
    void replacementAcrossProfilesAndModelsIsShadowed() {
        ObjectModel<Account> base =
                ObjectModel.of(Account.class).configure(faker -> faker.ruleFor("name", () -> "base"));
        ObjectModel<Account> refined =
                base.and(ObjectModel.of(Account.class).configure(faker -> faker.ruleFor("name", () -> "refined")));
        ObjectFaker<Account> profiled = new ObjectFaker<>(Account.class)
                .profile("named", faker -> faker.ruleFor("name", () -> "profile"))
                .useProfile("named")
                .ruleFor("name", () -> "test");

        for (ObjectFaker<Account> faker : List.of(refined.faker(), profiled)) {
            List<FixtureRuleProblem> problems = problemsOf(faker);
            assertEquals(1, problems.size(), problems.toString());
            assertProblem(problems.getFirst(), Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
            assertTrue(problems.getFirst().reason().startsWith("replaced by rule #2 'name'"));
        }
    }

    @Test
    @DisplayName("repeated includes and ignores count in the numbering but are not problems")
    void repeatedIncludesAndIgnoresAreNumberedOnly() {
        ObjectFaker<Account> faker = new ObjectFaker<>(Account.class)
                .ignore("purchase")
                .ignore("purchase")
                .ruleFor("name", () -> "first")
                .ruleFor("name", () -> "second");

        List<FixtureRuleProblem> problems = problemsOf(faker);

        assertEquals(1, problems.size(), problems.toString());
        assertProblem(problems.getFirst(), Category.SHADOWED, FixtureRuleKind.RULE, "name", 3);
    }

    @Test
    @DisplayName("a nested rule that a later dependent rule on its parent overwrites is shadowed")
    void nestedRuleUnderLaterDependentParentIsShadowed() {
        ObjectFaker<Shop> faker = new ObjectFaker<>(Shop.class)
                .ruleFor("client.location.city", () -> "Paris")
                .ruleFor("client", shop -> new Client());

        FixtureRuleProblem problem = onlyProblem(faker);

        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.RULE, "client.location.city", 1);
        assertEquals("dependent rule #2 'client' runs after it and replaces the whole of 'client'", problem.reason());
    }

    @Test
    @DisplayName("a nested rule overwritten by a nested ancestor that runs later is shadowed")
    void nestedRuleUnderLaterNestedAncestorIsShadowed() {
        ObjectFaker<Shop> plainParent = new ObjectFaker<>(Shop.class)
                .ruleFor("client.location.city", () -> "Paris")
                .ruleFor("client.location", Location::new);
        ObjectFaker<Shop> contextParent = new ObjectFaker<>(Shop.class)
                .ruleForContext("client.location", context -> new Location())
                .ruleFor("client.location.city", () -> "Paris");

        assertProblem(onlyProblem(plainParent), Category.SHADOWED, FixtureRuleKind.RULE, "client.location.city", 1);
        FixtureRuleProblem problem = onlyProblem(contextParent);
        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.RULE, "client.location.city", 2);
        assertTrue(problem.reason().startsWith("context rule #1 'client.location' runs after it"));
    }

    @Test
    @DisplayName("a nested rule that runs after its ancestor's rule is not shadowed")
    void nestedRuleAfterAncestorIsNotShadowed() {
        ObjectFaker<Shop> dependentFirst = new ObjectFaker<>(Shop.class)
                .ruleFor("client", shop -> new Client())
                .ruleFor("client.name", shop -> "Ada");
        ObjectFaker<Shop> rootRuleFirst =
                new ObjectFaker<>(Shop.class).ruleFor("client", Client::new).ruleFor("client.name", () -> "Ada");
        ObjectFaker<Shop> plainBeforeContext = new ObjectFaker<>(Shop.class)
                .ruleFor("client.location", Location::new)
                .ruleForContext("client.location.city", context -> "Paris");

        assertEquals(List.of(), problemsOf(dependentFirst));
        assertEquals(List.of(), problemsOf(rootRuleFirst));
        assertEquals(List.of(), problemsOf(plainBeforeContext));
    }

    @Test
    @DisplayName("a root rule that a GeneratorConfig context field override comes before is shadowed")
    void rootRuleBehindConfigContextFieldOverrideIsShadowed() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(Account.class, "name", (ContextualGenerator<String>) context -> "config")
                .build();

        FixtureRuleProblem problem =
                onlyProblem(new ObjectFaker<>(Account.class, config).ruleFor("name", () -> "rule"));

        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
        assertEquals("the GeneratorConfig context field override for Account.name comes first", problem.reason());
    }

    @Test
    @DisplayName("a root rule that a GeneratorConfig context predicate override comes before is shadowed")
    void rootRuleBehindConfigContextPredicateOverrideIsShadowed() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(named("name"), (ContextualGenerator<String>) context -> "config")
                .build();

        FixtureRuleProblem problem =
                onlyProblem(new ObjectFaker<>(Account.class, config).ruleFor("name", () -> "rule"));

        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
        assertEquals(
                "a GeneratorConfig context predicate override that matches Account.name comes first", problem.reason());
    }

    @Test
    @DisplayName("a GeneratorConfig field override that a root rule replaces is shadowed")
    void configFieldOverrideReplacedByRootRuleIsShadowed() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(Account.class, "name", () -> "config")
                .build();

        FixtureRuleProblem problem =
                onlyProblem(new ObjectFaker<>(Account.class, config).ruleFor("name", () -> "rule"));

        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.FIELD_OVERRIDE, "name", -1);
        assertEquals("replaced by rule #1 'name', which ObjectFaker installs for Account.name", problem.reason());
        assertEquals("GeneratorConfig field override at 'name'", problem.rule().describe());
    }

    @Test
    @DisplayName("GeneratorConfig field overrides that a root context rule replaces or comes before are shadowed")
    void configFieldOverridesBehindRootContextRuleAreShadowed() {
        GeneratorConfig both = GeneratorConfig.builder()
                .objectOverride(Account.class, "name", () -> "plain")
                .objectOverride(Account.class, "name", (ContextualGenerator<String>) context -> "context")
                .build();
        GeneratorConfig plainOnly = GeneratorConfig.builder()
                .objectOverride(Account.class, "name", () -> "plain")
                .build();

        List<FixtureRuleProblem> problems =
                problemsOf(new ObjectFaker<>(Account.class, both).ruleForContext("name", context -> "rule"));

        assertEquals(2, problems.size(), problems.toString());
        assertProblem(problems.get(0), Category.SHADOWED, FixtureRuleKind.CONTEXT_FIELD_OVERRIDE, "name", -1);
        assertEquals(
                "replaced by context rule #1 'name', which ObjectFaker installs for Account.name",
                problems.get(0).reason());
        assertProblem(problems.get(1), Category.SHADOWED, FixtureRuleKind.FIELD_OVERRIDE, "name", -1);
        assertEquals(
                "context rule #1 'name', which ObjectFaker installs for Account.name, comes first",
                problems.get(1).reason());
        assertEquals(
                1,
                problemsOf(new ObjectFaker<>(Account.class, plainOnly).ruleForContext("name", context -> "rule"))
                        .size());
    }

    @Test
    @DisplayName("GeneratorConfig field overrides that a nested or dependent rule overwrites are shadowed")
    void configFieldOverridesOverwrittenAfterGenerationAreShadowed() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(Client.class, "name", () -> "plain")
                .objectOverride(Client.class, "name", (ContextualGenerator<String>) context -> "context")
                .objectOverride(Account.class, "name", () -> "root")
                .build();

        List<FixtureRuleProblem> nested =
                problemsOf(new ObjectFaker<>(Shop.class, config).ruleFor("client.name", () -> "rule"));
        List<FixtureRuleProblem> dependent =
                problemsOf(new ObjectFaker<>(Account.class, config).ruleFor("name", account -> "rule"));

        assertEquals(2, nested.size(), nested.toString());
        assertProblem(nested.get(0), Category.SHADOWED, FixtureRuleKind.CONTEXT_FIELD_OVERRIDE, "client.name", -1);
        assertEquals(
                "overwritten by rule #1 'client.name', which runs after generation",
                nested.get(0).reason());
        assertProblem(nested.get(1), Category.SHADOWED, FixtureRuleKind.FIELD_OVERRIDE, "client.name", -1);
        assertEquals(1, dependent.size(), dependent.toString());
        assertProblem(dependent.getFirst(), Category.SHADOWED, FixtureRuleKind.FIELD_OVERRIDE, "name", -1);
    }

    @Test
    @DisplayName("a requested unique name that a nested or dependent rule sets is shadowed; a root rule is checked")
    void requestedUniqueNameBypassedAfterGenerationIsShadowed() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectUniqueField("city")
                .objectUniqueField("title")
                .build();

        FixtureRuleProblem problem = onlyProblem(new ObjectFaker<>(Shop.class, config)
                .ruleFor("client.location.city", () -> "Paris")
                .ruleFor("title", () -> "Main"));

        assertProblem(problem, Category.SHADOWED, FixtureRuleKind.UNIQUE_FIELD, "client.location.city", -1);
        assertEquals(
                "the values of rule #1 'client.location.city' are not checked for uniqueness, because the rule runs"
                        + " after generation",
                problem.reason());
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Shop.class).ruleFor("client.email", () -> "same@example.test")),
                "email is unique by default, not requested");
    }

    // ── Unused ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a rule below a path that a later ignore clears is unused")
    void ruleUnderLaterIgnoreIsUnused() {
        ObjectFaker<Shop> faker = new ObjectFaker<>(Shop.class)
                .ruleFor("client.location.city", () -> "Paris")
                .ignore("client.location");

        FixtureRuleProblem problem = onlyProblem(faker);

        assertProblem(problem, Category.UNUSED, FixtureRuleKind.RULE, "client.location.city", 1);
        assertEquals("ignore #2 'client.location' clears 'client.location' after the rules run", problem.reason());
    }

    @Test
    @DisplayName("a root rule on a member that the GeneratorConfig excludes is unused, and not ambiguous")
    void rootRuleOnExcludedMemberIsUnused() {
        GeneratorConfig config =
                GeneratorConfig.builder().objectExcludeField("id").build();

        FixtureRuleProblem byConfig = onlyProblem(new ObjectFaker<>(Account.class, config).ruleFor("id", () -> "x"));
        FixtureRuleProblem byAnnotation =
                onlyProblem(new ObjectFaker<>(AnnotatedRecord.class).ruleFor("secret", () -> "x"));

        assertProblem(byConfig, Category.UNUSED, FixtureRuleKind.RULE, "id", 1);
        assertEquals(
                "the GeneratorConfig excludes BaseEntity.id, so generation never asks the rule for a value",
                byConfig.reason());
        assertProblem(byAnnotation, Category.UNUSED, FixtureRuleKind.RULE, "secret", 1);
    }

    @Test
    @DisplayName("root rules and root ignores are unused for generate() when a type override builds the root")
    void rootDecisionsUnderRootTypeOverrideAreUnusedForGenerate() {
        for (GeneratorConfig config : List.of(
                GeneratorConfig.builder()
                        .objectOverride(Account.class, Account::new)
                        .build(),
                GeneratorConfig.builder()
                        .objectOverride(Account.class, (ContextualGenerator<Account>) context -> new Account())
                        .build())) {
            ObjectFaker<Account> faker = new ObjectFaker<>(Account.class, config)
                    .ruleFor("name", () -> "rule")
                    .ruleFor("id", account -> "dependent")
                    .ignore("purchase")
                    .strict();

            FixtureRuleException failure = assertThrows(FixtureRuleException.class, faker::generate);

            assertEquals(2, failure.getProblems().size(), failure.getProblems().toString());
            assertProblem(failure.getProblems().get(0), Category.UNUSED, FixtureRuleKind.RULE, "name", 1);
            assertEquals(
                    "generate() builds Account with the GeneratorConfig type override, which applies no root rule or"
                            + " root ignore",
                    failure.getProblems().get(0).reason());
            assertProblem(failure.getProblems().get(1), Category.UNUSED, FixtureRuleKind.IGNORE, "purchase", 3);
            assertEquals("rule", faker.populate(new Account()).name, "populate never builds the root");
        }
        GeneratorConfig shopFactory =
                GeneratorConfig.builder().objectOverride(Shop.class, Shop::new).build();
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Shop.class, shopFactory).ignore("client.name")));
    }

    @Test
    @DisplayName("an ignore of a member past objectMaxDepth is unused")
    void ignorePastMaxDepthIsUnused() {
        GeneratorConfig config = GeneratorConfig.builder().objectMaxDepth(1).build();

        FixtureRuleProblem problem = onlyProblem(new ObjectFaker<>(Shop.class, config)
                .ruleFor("title", () -> "Main")
                .ruleFor("client.office.city", () -> "Lyon")
                .ignore("client.location.city"));

        assertProblem(problem, Category.UNUSED, FixtureRuleKind.IGNORE, "client.location.city", 3);
        assertEquals(
                "'client.location' would hold an object at depth 2, past objectMaxDepth 1, so it is always null",
                problem.reason());
    }

    @Test
    @DisplayName("an ignore of a nested object at objectMaxDepth is unused")
    void ignoreOfObjectAtMaxDepthIsUnused() {
        GeneratorConfig config = GeneratorConfig.builder().objectMaxDepth(1).build();

        assertProblem(
                onlyProblem(new ObjectFaker<>(Shop.class, config).ignore("client.location")),
                Category.UNUSED,
                FixtureRuleKind.IGNORE,
                "client.location",
                1);
    }

    @Test
    @DisplayName("an ignore whose path a rule, an override or an initializer supplies is used")
    void ignoreOnSuppliedPathIsUsed() {
        GeneratorConfig shallow = GeneratorConfig.builder().objectMaxDepth(1).build();
        GeneratorConfig typeOverride = GeneratorConfig.builder()
                .objectMaxDepth(1)
                .objectOverride(Location.class, Location::new)
                .build();
        GeneratorConfig fieldOverride = GeneratorConfig.builder()
                .objectMaxDepth(1)
                .objectOverride(Client.class, "location", Location::new)
                .build();

        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Shop.class, shallow)
                        .ruleFor("client.location.street", () -> "Main")
                        .ignore("client.location.city")));
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Shop.class, shallow)
                        .ruleFor("client", Client::new)
                        .ignore("client.location.city")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Shop.class, typeOverride).ignore("client.location.city")));
        assertEquals(
                List.of(), problemsOf(new ObjectFaker<>(Shop.class, fieldOverride).ignore("client.location.city")));
        assertEquals(
                List.of(), problemsOf(new ObjectFaker<>(PresetShop.class, shallow).ignore("client.location.city")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Shop.class).ignore("client.location.city")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Trip.class, shallow).ignore("stop.city")));
    }

    @Test
    @DisplayName("an ignore below a member that include mode leaves out is unused")
    void ignoreBelowRootLeftOutByIncludeModeIsUnused() {
        FixtureRuleProblem problem =
                onlyProblem(new ObjectFaker<>(Shop.class).include("title").ignore("client.name"));

        assertProblem(problem, Category.UNUSED, FixtureRuleKind.IGNORE, "client.name", 2);
        assertEquals("include mode leaves out 'client', so nothing below it is generated", problem.reason());
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Shop.class).include("client").ignore("client.name")));
    }

    @Test
    @DisplayName("an ignore below or of a member that the GeneratorConfig excludes is unused unless it starts set")
    void ignoreOfExcludedMemberIsUnused() {
        GeneratorConfig parentExcluded =
                GeneratorConfig.builder().objectExcludeField("location").build();
        GeneratorConfig leafExcluded =
                GeneratorConfig.builder().objectExcludeField("city").build();

        FixtureRuleProblem parent =
                onlyProblem(new ObjectFaker<>(Shop.class, parentExcluded).ignore("client.location.city"));
        FixtureRuleProblem leaf =
                onlyProblem(new ObjectFaker<>(Shop.class, leafExcluded).ignore("client.location.city"));

        assertEquals(
                "the GeneratorConfig excludes 'client.location', so nothing below it is generated", parent.reason());
        assertEquals(
                "the GeneratorConfig excludes 'client.location.city', so it always keeps its default", leaf.reason());
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(PresetClientShop.class, parentExcluded).ignore("client.location.city")),
                "an initializer keeps an excluded member");
    }

    @Test
    @DisplayName("an ignore through an abstract or sealed member, or below an abstract root, is used")
    void ignoreThroughAbstractTypeIsUsed() {
        GeneratorConfig shallow = GeneratorConfig.builder().objectMaxDepth(1).build();

        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Garage.class, shallow).ignore("vehicle.plate")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Shape.class, shallow).ignore("origin.city")));
    }

    // ── Ambiguous ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("a root rule on an inherited member also applies in a nested object of another subclass")
    void rootRuleOnInheritedMemberIsAmbiguous() {
        FixtureRuleProblem problem = onlyProblem(new ObjectFaker<>(Account.class).ruleFor("id", () -> "x"));

        assertProblem(problem, Category.AMBIGUOUS, FixtureRuleKind.RULE, "id", 1);
        assertEquals(
                "ObjectFaker installs it for BaseEntity.id wherever generation reaches that member, so it also"
                        + " applies at 'purchase.id'",
                problem.reason());
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Account.class).ruleFor("id", account -> "x")));
    }

    @Test
    @DisplayName("root rules, root ignores and include mode also apply in a nested object of the root's type")
    void rootDecisionsAlsoApplyInNestedObjectOfRootType() {
        FixtureRuleProblem rule = onlyProblem(new ObjectFaker<>(Employee.class).ruleFor("name", () -> "x"));
        FixtureRuleProblem context =
                onlyProblem(new ObjectFaker<>(NodeRecord.class).ruleForContext("label", ctx -> "x"));
        FixtureRuleProblem ignore = onlyProblem(new ObjectFaker<>(Account.class).ignore("id"));
        FixtureRuleProblem include = onlyProblem(new ObjectFaker<>(Account.class).include("name", "purchase"));
        List<FixtureRuleProblem> leftOut = problemsOf(new ObjectFaker<>(FrozenAccount.class)
                .include("name")
                .ignore("purchase")
                .ruleFor("name", () -> "x"));

        assertEquals("'manager.name'", rule.reason().substring(rule.reason().indexOf('\'')));
        assertTrue(context.reason().endsWith("applies at 'child.label'"), context.reason());
        assertProblem(ignore, Category.AMBIGUOUS, FixtureRuleKind.IGNORE, "id", 1);
        assertProblem(include, Category.AMBIGUOUS, FixtureRuleKind.INCLUDE, "id", 1);
        assertEquals(
                "include mode leaves out BaseEntity.id wherever generation reaches that member, so it also applies"
                        + " at 'purchase.id'",
                include.reason());
        assertEquals(List.of(), leftOut, "an ignored or final root member is not left out by include mode");
    }

    @Test
    @DisplayName("a member kept at its initializer is not followed, unless initializers are overridden")
    void initializedMemberIsNotFollowed() {
        GeneratorConfig overriding = GeneratorConfig.builder()
                .objectOverrideDefaultInitialization(true)
                .build();

        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Cart.class).ruleFor("id", () -> "x")));
        assertTrue(onlyProblem(new ObjectFaker<>(Cart.class, overriding).ruleFor("id", () -> "x"))
                .reason()
                .endsWith("applies at 'purchases[].id'"));
    }

    @Test
    @DisplayName("containers, constructor arguments, sealed members and mapped members are followed")
    void containersConstructorsAndSubtypesAreFollowed() {
        GeneratorConfig mapped = GeneratorConfig.builder()
                .objectSubtype(Payment.class, CardPayment.class)
                .build();

        FixtureRuleProblem ledger = onlyProblem(new ObjectFaker<>(Ledger.class).ruleFor("id", () -> "x"));
        FixtureRuleProblem billing = onlyProblem(new ObjectFaker<>(Billing.class).ruleFor("id", () -> "x"));
        FixtureRuleProblem pet = onlyProblem(new ObjectFaker<>(Pet.class).ruleFor("id", () -> "x"));
        FixtureRuleProblem payment = onlyProblem(new ObjectFaker<>(PaidOrder.class, mapped).ruleFor("id", () -> "x"));

        assertTrue(
                ledger.reason()
                        .endsWith("applies at 'byCode.value.id', 'byPurchase.key.id', 'history[].id', "
                                + "'latest.value.id', 'list[].id' and 2 more"),
                ledger.reason());
        assertTrue(billing.reason().endsWith("applies at 'invoice.purchase.id'"), billing.reason());
        assertTrue(pet.reason().endsWith("applies at 'animal.id'"), pet.reason());
        assertTrue(payment.reason().endsWith("applies at 'payment.id'"), payment.reason());
    }

    @Test
    @DisplayName("a sealed root's choices are followed, and a mapped or plain abstract root is generated as such")
    void sealedAndAbstractRootsAreFollowed() {
        GeneratorConfig mapped = GeneratorConfig.builder()
                .objectSubtype(Vehicle.class, Bike.class)
                .build();

        FixtureRuleProblem sealed = onlyProblem(new ObjectFaker<>(Vehicle.class).ruleFor("plate", () -> "x"));

        assertTrue(sealed.reason().endsWith("applies at 'towed.plate'"), sealed.reason());
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Vehicle.class, mapped).ruleFor("plate", () -> "x")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Shape.class).ruleFor("name", () -> "x")));
    }

    @Test
    @DisplayName("a deeper nested object of another subclass is followed within objectMaxDepth only")
    void routesStopAtMaxDepth() {
        GeneratorConfig shallow = GeneratorConfig.builder().objectMaxDepth(1).build();
        GeneratorConfig deep = GeneratorConfig.builder().objectMaxDepth(2).build();

        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Envelope.class, shallow).ruleFor("id", () -> "x")));
        assertTrue(onlyProblem(new ObjectFaker<>(Envelope.class, deep).ruleFor("id", () -> "x"))
                .reason()
                .endsWith("applies at 'middle.purchase.id'"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("decidedPurchases")
    @DisplayName("a member that an override, an exclusion or a rule decides is not followed")
    void decidedMemberIsNotFollowed(String description, Class<?> type, GeneratorConfig config) {
        assertEquals(List.of(), problemsOf(fakerFor(type, config).ruleFor("id", () -> "x")), description);
    }

    static Stream<Arguments> decidedPurchases() {
        Predicate<Field> purchaseField = named("purchase");
        return Stream.of(
                Arguments.of(
                        "field override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(Account.class, "purchase", () -> null)
                                .build()),
                Arguments.of(
                        "context field override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(Account.class, "purchase", (ContextualGenerator<Purchase>) c -> null)
                                .build()),
                Arguments.of(
                        "predicate override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(purchaseField, () -> null)
                                .build()),
                Arguments.of(
                        "context predicate override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(purchaseField, (ContextualGenerator<Purchase>) c -> null)
                                .build()),
                Arguments.of(
                        "type override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(Purchase.class, Purchase::new)
                                .build()),
                Arguments.of(
                        "context type override",
                        Account.class,
                        GeneratorConfig.builder()
                                .objectOverride(Purchase.class, (ContextualGenerator<Purchase>) c -> new Purchase())
                                .build()),
                Arguments.of(
                        "exclusion",
                        Account.class,
                        GeneratorConfig.builder().objectExcludeField("purchase").build()),
                Arguments.of("@Randomizer", RandomizedAccount.class, GeneratorConfig.defaults()),
                Arguments.of(
                        "constructor argument excluded by name",
                        InvoiceHolder.class,
                        GeneratorConfig.builder().objectExcludeField("purchase").build()),
                Arguments.of(
                        "constructor argument whose field is excluded",
                        InvoiceHolder.class,
                        GeneratorConfig.builder()
                                .objectExclude(field -> field.getDeclaringClass() == Invoice.class)
                                .build()),
                Arguments.of(
                        "by-name override of a constructor argument",
                        InvoiceHolder.class,
                        GeneratorConfig.builder()
                                .objectOverride(Invoice.class, "purchase", () -> null)
                                .build()));
    }

    @Test
    @DisplayName("a member that a root rule or root ignore of the faker decides is not followed")
    void memberDecidedByFakerIsNotFollowed() {
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Account.class)
                        .ruleFor("id", () -> "x")
                        .ruleFor("purchase", () -> null)));
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(Account.class)
                        .ruleFor("id", () -> "x")
                        .ignore("purchase")));
        assertEquals(
                List.of(),
                problemsOf(new ObjectFaker<>(
                                Account.class,
                                GeneratorConfig.builder()
                                        .objectExcludeField("id")
                                        .build())
                        .ignore("id")),
                "an ignore of a member the GeneratorConfig already excludes applies nowhere else");
    }

    @Test
    @DisplayName("types that generation cannot follow or build are left out of the routes")
    void unfollowableTypesAreLeftOut() {
        GeneratorConfig unsafe = GeneratorConfig.builder()
                .objectConstructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                .build();
        GeneratorConfig coveredCat =
                GeneratorConfig.builder().objectOverride(Cat.class, Cat::new).build();

        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Unfollowable.class).ruleFor("id", () -> "x")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Exploding.class).ruleFor("id", () -> "x")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Pet.class, coveredCat).ruleFor("id", () -> "x")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(GenericHolder.class).ruleFor("id", () -> "x")));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(GenericBox.class).ruleFor("values", () -> null)));
        assertTrue(onlyProblem(new ObjectFaker<>(TwoWayHolder.class, unsafe).ruleFor("id", () -> "x"))
                .reason()
                .endsWith("applies at 'twoWays.id', 'twoWays.purchase.id'"));
    }

    @Test
    @DisplayName("a root rule that fills a constructor argument of its name is not behind a context predicate override")
    void rootRuleFillingConstructorArgumentIsNotBehindPredicate() {
        GeneratorConfig config = GeneratorConfig.builder()
                .objectOverride(named("name"), (ContextualGenerator<String>) context -> "config")
                .build();
        ObjectFaker<NamedByConstructor> own =
                new ObjectFaker<>(NamedByConstructor.class, config).ruleFor("name", () -> "rule");
        ObjectFaker<InheritedNameByConstructor> inherited =
                new ObjectFaker<>(InheritedNameByConstructor.class, config).ruleFor("name", () -> "rule");

        assertEquals(List.of(), problemsOf(own));
        assertEquals("rule", own.generate().name);
        assertProblem(onlyProblem(inherited), Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
        assertEquals("config", inherited.generate().name);
        ObjectFaker<RenamedByConstructor> renamed =
                new ObjectFaker<>(RenamedByConstructor.class, config).ruleFor("name", () -> "rule");
        assertProblem(onlyProblem(renamed), Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
        assertEquals("config", renamed.generate().name);
    }

    @Test
    @DisplayName("an ignore that generate() never gives a value is no problem for populate(existing)")
    void ignoreWithoutGeneratedValueStillAppliesToPopulate() {
        ObjectFaker<Shop> faker = new ObjectFaker<>(
                        Shop.class, GeneratorConfig.builder().objectMaxDepth(1).build())
                .ruleFor("title", () -> "Main")
                .ignore("client.location.city")
                .strict();
        Shop existing = new Shop();
        existing.client = new Client();
        existing.client.location = new Location();
        existing.client.location.city = "Paris";

        assertThrows(FixtureRuleException.class, faker::generate);
        assertNull(faker.populate(existing).client.location.city);
    }

    @Test
    @DisplayName("a collection that stays empty, a @Null member and conflicting constraints are not followed")
    void emptyCollectionsAndNullMembersAreNotFollowed() {
        GeneratorConfig empty = GeneratorConfig.builder()
                .objectOverrideDefaultInitialization(true)
                .collectionSize(0, 0)
                .build();

        assertEquals(List.of(), problemsOf(new ObjectFaker<>(Cart.class, empty).ruleFor("id", () -> "x")));
        assertTrue(onlyProblem(new ObjectFaker<>(SizedCart.class, empty).ruleFor("id", () -> "x"))
                .reason()
                .endsWith("applies at 'purchases[].id'"));
        assertEquals(List.of(), problemsOf(new ObjectFaker<>(NullPurchaseHolder.class).ruleFor("id", () -> "x")));
    }

    @Test
    @DisplayName("generic specializations of one class are followed apart, as the cycle guard keys them")
    void genericSpecializationsAreFollowedApart() {
        assertTrue(onlyProblem(new ObjectFaker<>(WrapperHolder.class).ruleFor("id", () -> "x"))
                .reason()
                .endsWith("applies at 'wrapper.inner.value.id'"));
    }

    // ── Strict mode ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("ObjectFaker.strict() reports every problem together, with no generated value")
    void strictReportsEveryProblemTogether() {
        String value = "VALUE-7f3a";
        ObjectFaker<Shop> faker = new ObjectFaker<>(Shop.class)
                .ruleFor("client.location.city", () -> value)
                .ruleFor("client.location.city", () -> value)
                .ruleFor("client", shop -> new Client())
                .strict();

        FixtureRuleException failure = assertThrows(FixtureRuleException.class, faker::generate);

        assertEquals(List.of("title"), failure.getUndecidedProperties());
        assertEquals(2, failure.getProblems().size());
        assertEquals(
                "ObjectFaker for " + Shop.class.getName() + " has 3 fixture rule problems:\n"
                        + "- UNDECIDED root properties [title]: each needs a rule, an include or an ignore\n"
                        + "- SHADOWED rule #1 'client.location.city': replaced by rule #2 'client.location.city',"
                        + " registered later for the same path\n"
                        + "- SHADOWED rule #2 'client.location.city': dependent rule #3 'client' runs after it and"
                        + " replaces the whole of 'client'",
                failure.getMessage());
        assertFalse(failure.getMessage().contains(value));
        assertThrows(FixtureRuleException.class, () -> faker.populate(new Shop()));
    }

    @Test
    @DisplayName("without ObjectFaker.strict(), the same rules generate as before")
    void withoutStrictTheRulesGenerateAsBefore() {
        Shop shop = new ObjectFaker<>(Shop.class)
                .ruleFor("title", () -> "first")
                .ruleFor("title", () -> "second")
                .ruleFor("client.location.city", () -> "Paris")
                .ruleFor("client", generated -> new Client())
                .generate();

        assertEquals("second", shop.title);
        assertNull(shop.client.location, "the dependent rule on the parent runs last");
    }

    @Test
    @DisplayName("the check runs once for each configuration, and again after a new registration")
    void checkRunsOncePerConfiguration() {
        ObjectFaker<CountedHolder> faker = new ObjectFaker<>(CountedHolder.class).ruleFor("name", () -> "x");
        int before = CountedHolder.constructions;

        assertSame(faker, faker.assertConfigurationIsValid());
        assertSame(faker, faker.assertConfigurationIsValid());
        assertEquals(before + 1, CountedHolder.constructions);

        faker.ruleFor("name", () -> "y");
        assertThrows(FixtureRuleException.class, faker::assertConfigurationIsValid);
        assertEquals(before + 2, CountedHolder.constructions);
    }

    @Test
    @DisplayName("a failed profile restores the registrations it added")
    void failedProfileRestoresRegistrations() {
        ObjectFaker<Account> faker = new ObjectFaker<>(Account.class).profile("broken", configured -> {
            configured.ruleFor("name", () -> "profile");
            throw new IllegalStateException("broken profile");
        });

        assertThrows(IllegalStateException.class, () -> faker.useProfile("broken"));
        faker.ruleFor("name", () -> "first").ruleFor("name", () -> "second");

        assertProblem(onlyProblem(faker), Category.SHADOWED, FixtureRuleKind.RULE, "name", 1);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static List<FixtureRuleProblem> problemsOf(ObjectFaker<?> faker) {
        try {
            faker.assertConfigurationIsValid();
            return List.of();
        } catch (FixtureRuleException failure) {
            return failure.getProblems();
        }
    }

    private static FixtureRuleProblem onlyProblem(ObjectFaker<?> faker) {
        List<FixtureRuleProblem> problems = problemsOf(faker);
        assertEquals(1, problems.size(), problems.toString());
        return problems.getFirst();
    }

    private static void assertProblem(
            FixtureRuleProblem problem, Category category, FixtureRuleKind kind, String path, int registration) {
        assertEquals(category, problem.category(), problem.describe());
        assertEquals(kind, problem.rule().kind(), problem.describe());
        assertEquals(path, problem.rule().path(), problem.describe());
        assertEquals(registration, problem.rule().registration(), problem.describe());
    }

    private static Predicate<Field> named(String name) {
        return field -> field.getName().equals(name);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ObjectFaker<Object> fakerFor(Class<?> type, GeneratorConfig config) {
        return new ObjectFaker(type, config);
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    static class BaseEntity {
        String id;
    }

    static final class Account extends BaseEntity {
        String name;
        Purchase purchase;
    }

    static final class Purchase extends BaseEntity {
        String note;
    }

    static final class Employee {
        String name;
        Employee manager;
    }

    record NodeRecord(String label, NodeRecord child) {}

    static final class Shop {
        String title;
        Client client;
    }

    static final class Client {
        String name;
        String email;
        Location location;
        Location office;
    }

    static final class Location {
        String city;
        String street;
    }

    static final class PresetShop {
        Client client = new Client();
    }

    static final class PresetClient {
        Location location = new Location();
    }

    static final class PresetClientShop {
        PresetClient client;
    }

    record AnnotatedRecord(@Exclude String secret, String visible) {}

    record Trip(Location stop) {}

    static final class Cart extends BaseEntity {
        List<Purchase> purchases = new ArrayList<>();
    }

    static final class Ledger extends BaseEntity {
        Map<String, Purchase> byCode;
        Map<Purchase, String> byPurchase;
        Purchase[] history;
        Optional<Purchase> latest;
        List<Purchase> list;
        Queue<Purchase> queue;
        Set<Purchase> set;
        Status status;
    }

    enum Status {
        OPEN
    }

    static final class Invoice {
        final Purchase purchase;
        String note;

        Invoice(Purchase purchase, String code) {
            this.purchase = purchase;
            this.note = code;
        }
    }

    static final class Billing extends BaseEntity {
        Invoice invoice;
    }

    static final class InvoiceHolder extends BaseEntity {
        Invoice invoice;
    }

    sealed interface Animal permits Cat, Dog, Mood {}

    sealed interface Broken permits Ghost {}

    static final class Cat extends BaseEntity implements Animal {
        String meow;
    }

    record Dog(String bark) implements Animal {}

    enum Mood implements Animal {
        CALM
    }

    abstract static non-sealed class Ghost implements Broken {}

    static final class Pet extends BaseEntity {
        Animal animal;
    }

    interface Payment {}

    static final class CardPayment extends BaseEntity implements Payment {
        String last4;
    }

    static final class PaidOrder extends BaseEntity {
        Payment payment;
    }

    abstract static sealed class Vehicle permits Car, Bike {
        String plate;
    }

    static final class Car extends Vehicle {
        Vehicle towed;
    }

    static final class Bike extends Vehicle {}

    static final class Garage {
        Vehicle vehicle;
    }

    abstract static class Shape {
        String name;
        Location origin;
    }

    static final class Middle {
        Purchase purchase;
    }

    static final class Envelope extends BaseEntity {
        Middle middle;
    }

    static final class Placeholder implements Generator<Purchase> {
        @Override
        public Purchase generate() {
            return null;
        }
    }

    static final class RandomizedAccount extends BaseEntity {
        @Randomizer(Placeholder.class)
        Purchase purchase;
    }

    static final class TwoWays extends BaseEntity {
        Purchase purchase;

        TwoWays(String first) {}

        TwoWays(int second) {}
    }

    static final class TwoWayHolder extends BaseEntity {
        TwoWays twoWays;
    }

    final class InnerPurchase extends BaseEntity {}

    @kotlin.Metadata
    static final class KotlinLike extends BaseEntity {
        final String value = "kept";
    }

    static final class Exploding extends BaseEntity {
        Purchase purchase;

        Exploding() {
            throw new IllegalStateException("no prototype");
        }
    }

    static final class GenericBox<T> {
        T value;
        List<T> values;
        Optional<T> maybe;
        GenericBox<T> self;
    }

    static final class GenericHolder extends BaseEntity {
        @SuppressWarnings("rawtypes")
        GenericBox raw;

        GenericBox<String> box;
        InnerPurchase inner;
        KotlinLike kotlin;
        ObjectConstructionPolicyTest.AdapterFixture adapted;
        // Compiled into core without parameter names, so its constructor's arguments have positions only.
        ObjectModel<String> model;
        Function<String, Purchase> function;
        Flexible flexible;
    }

    static final class Flexible {
        String label;

        Flexible() {}

        Flexible(String label) {
            this.label = label;
        }
    }

    static final class CountedHolder {
        static int constructions;
        String name;

        CountedHolder() {
            constructions++;
        }
    }

    static final class Unfollowable extends BaseEntity {
        Broken broken;
        TwoWays twoWays;
    }

    static final class NamedByConstructor {
        String name;

        NamedByConstructor(String name) {
            this.name = name;
        }
    }

    static class NameBase {
        String name;
    }

    static final class RenamedByConstructor {
        String name;

        RenamedByConstructor(String label) {
            this.name = label;
        }
    }

    static final class InheritedNameByConstructor extends NameBase {
        InheritedNameByConstructor(String name) {
            this.name = name;
        }
    }

    static final class SizedCart extends BaseEntity {
        @Size(min = 1) List<Purchase> purchases;

        @Size(min = 3, max = 1) List<Purchase> conflicting;

        @Size(max = 5) List<Purchase> capped;
    }

    static final class NullPurchaseHolder extends BaseEntity {
        @Null Purchase purchase;
    }

    static final class Wrapper<T> {
        T value;
        Wrapper<Purchase> inner;
    }

    static final class WrapperHolder extends BaseEntity {
        Wrapper<String> wrapper;
    }

    static final class FrozenAccount extends BaseEntity {
        final String code = "fixed";
        String name;
        Purchase purchase;
    }
}
