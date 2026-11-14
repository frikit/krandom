/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.FixtureRuleProblem.Category;
import io.github.frikit.krandom.generator.object.ObjectFakerStrictDiagnosticsTest.ConstructedEnvelope;
import io.github.frikit.krandom.generator.object.ObjectFakerStrictDiagnosticsTest.ConstructedPurchaseAccount;
import io.github.frikit.krandom.generator.object.ObjectFakerStrictDiagnosticsTest.Middle;
import io.github.frikit.krandom.generator.object.ObjectFakerStrictDiagnosticsTest.NodeRecord;
import io.github.frikit.krandom.generator.object.exception.FixtureRuleException;
import jakarta.validation.constraints.Null;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectFaker.followConstructorArguments makes the check follow constructor arguments and records")
class ObjectFakerStrictConstructorArgumentsTest {

    private static List<FixtureRuleProblem> problemsOf(ObjectFaker<?> faker) {
        try {
            faker.assertConfigurationIsValid();
            return List.of();
        } catch (FixtureRuleException failure) {
            return failure.getProblems();
        }
    }

    private static void assertAmbiguousAt(ObjectFaker<?> faker, String path, String route) {
        List<FixtureRuleProblem> problems = problemsOf(faker);
        assertEquals(1, problems.size(), problems.toString());
        FixtureRuleProblem problem = problems.get(0);
        assertEquals(Category.AMBIGUOUS, problem.category(), problem.describe());
        assertEquals(FixtureRuleKind.RULE, problem.rule().kind(), problem.describe());
        assertEquals(path, problem.rule().path(), problem.describe());
        assertTrue(problem.reason().endsWith("applies at '" + route + "'"), problem.reason());
    }

    private static ObjectFaker<ConstructedPurchaseAccount> account(
            GeneratorConfig config, UnaryOperator<ObjectFaker<ConstructedPurchaseAccount>> option) {
        return option.apply(new ObjectFaker<>(ConstructedPurchaseAccount.class, config)
                .ruleFor("id", () -> "x")
                .ruleFor("name", () -> "n")
                .ruleFor("purchase.note", () -> "p"));
    }

    private static ObjectFaker<ConstructedEnvelope> envelope(UnaryOperator<ObjectFaker<ConstructedEnvelope>> option) {
        return option.apply(new ObjectFaker<>(
                        ConstructedEnvelope.class,
                        GeneratorConfig.builder().objectMaxDepth(1).build())
                .ruleFor("id", () -> "x")
                .ruleFor("middle.purchase.note", () -> "n"));
    }

    private static ObjectFaker<NodeRecord> node(UnaryOperator<ObjectFaker<NodeRecord>> option) {
        return option.apply(new ObjectFaker<>(
                        NodeRecord.class,
                        GeneratorConfig.builder().objectNullProbability(1.0).build())
                .ruleFor("label", () -> "x")
                .ruleFor("child.child.label", () -> "y"));
    }

    @Test
    @DisplayName("an argument that the configuration excludes is left null, so the parent a rule builds is followed")
    void excludedArgumentIsFollowed() {
        GeneratorConfig excluded =
                GeneratorConfig.builder().objectExcludeField("purchase").build();

        // Generation passes null, which the constructor keeps; the nested rule builds the purchase as a root.
        assertEquals("x", account(excluded, faker -> faker).generate().purchase.id);
        assertEquals(List.of(), problemsOf(account(excluded, faker -> faker)));
        assertAmbiguousAt(account(excluded, ObjectFaker::followConstructorArguments), "id", "purchase.id");
    }

    @Test
    @DisplayName("an argument that generation leaves null is followed, past objectMaxDepth and at a null draw")
    void argumentLeftNullIsFollowed() {
        GeneratorConfig absent =
                GeneratorConfig.builder().objectNullProbability(1.0).build();

        assertEquals("x", account(absent, faker -> faker).generate().purchase.id);
        assertEquals(List.of(), problemsOf(account(absent, faker -> faker)));
        assertAmbiguousAt(account(absent, ObjectFaker::followConstructorArguments), "id", "purchase.id");

        assertEquals("x", envelope(faker -> faker).generate().middle.purchase.id);
        assertEquals(List.of(), problemsOf(envelope(faker -> faker)));
        assertAmbiguousAt(envelope(ObjectFaker::followConstructorArguments), "id", "middle.purchase.id");
    }

    @Test
    @DisplayName("a record component that generation leaves null is followed")
    void recordComponentIsFollowed() {
        NodeRecord generated = node(faker -> faker).generate();

        assertEquals("x", generated.child().label());
        assertEquals("y", generated.child().child().label());
        assertEquals(List.of(), problemsOf(node(faker -> faker)));
        // The root rule also reaches the child that the nested rule builds below, before the rule overwrites it.
        assertAmbiguousAt(node(ObjectFaker::followConstructorArguments), "label", "child.label', 'child.child.label");
    }

    @Test
    @DisplayName("strict() fails on the problem it finds, and only with the option")
    void strictFails() {
        GeneratorConfig absent =
                GeneratorConfig.builder().objectNullProbability(1.0).build();
        ObjectFaker<ConstructedPurchaseAccount> faker = account(absent, ObjectFaker::followConstructorArguments);

        // The option affects only the check, so generation without strict() is unchanged.
        assertEquals("x", faker.generate().purchase.id);
        FixtureRuleException failure =
                assertThrows(FixtureRuleException.class, () -> faker.strict().generate());
        assertEquals(1, failure.getProblems().size());
        assertEquals("x", account(absent, ObjectFaker::strict).generate().purchase.id);
    }

    @Test
    @DisplayName("turning it on after a check checks again, and a failed profile rolls it back")
    void checkRunsAgainAndProfilesRollBack() {
        GeneratorConfig absent =
                GeneratorConfig.builder().objectNullProbability(1.0).build();
        ObjectFaker<ConstructedPurchaseAccount> checked = account(absent, faker -> faker);
        assertEquals(List.of(), problemsOf(checked));

        assertAmbiguousAt(checked.followConstructorArguments(), "id", "purchase.id");

        ObjectFaker<ConstructedPurchaseAccount> rolledBack = account(
                absent,
                faker -> faker.profile("failing", profile -> {
                    profile.followConstructorArguments();
                    throw new IllegalStateException("profile failed");
                }));
        assertThrows(IllegalStateException.class, () -> rolledBack.useProfile("failing"));
        assertEquals(List.of(), problemsOf(rolledBack));
    }

    static class Entity {
        String id;
    }

    static final class Chained extends Entity {
        String note;
        Chained child;
    }

    static final class NullArgumentRoot extends Entity {
        Chained purchase;

        NullArgumentRoot(@Null Chained purchase) {
            this.purchase = purchase;
        }
    }

    @Test
    @DisplayName("an argument whose field generation fills afterwards is not a parent that a rule builds")
    void argumentFilledAgainByItsFieldIsNotFollowed() {
        ObjectFaker<NullArgumentRoot> faker = new ObjectFaker<>(
                        NullArgumentRoot.class,
                        GeneratorConfig.builder().objectMaxDepth(1).build())
                .ruleFor("id", () -> "x")
                .ruleFor("purchase.note", () -> "n");

        // The constructor stores the null that @Null asks for, and generation fills the field again, within
        // objectMaxDepth: the root rule reaches purchase.id, and no rule builds purchase.child.
        NullArgumentRoot generated = faker.generate();
        assertEquals("x", generated.purchase.id);
        assertEquals(null, generated.purchase.child);
        assertAmbiguousAt(faker.followConstructorArguments(), "id", "purchase.id");
    }

    @Test
    @DisplayName("an argument whose field generation fills again, or an override supplies, keeps nothing it left")
    void argumentReplacedByItsFieldKeepsNothing() {
        // With objectOverrideDefaultInitialization generation fills the field again, past objectMaxDepth as before.
        ObjectFaker<ConstructedEnvelope> overriding = new ObjectFaker<>(
                        ConstructedEnvelope.class,
                        GeneratorConfig.builder()
                                .objectMaxDepth(1)
                                .objectOverrideDefaultInitialization(true)
                                .build())
                .ruleFor("id", () -> "x")
                .ruleFor("middle.purchase.note", () -> "n");
        assertEquals("x", overriding.generate().middle.purchase.id);
        assertAmbiguousAt(overriding.followConstructorArguments(), "id", "middle.purchase.id");

        // A predicate override, which reaches the field and not the argument, supplies a value nothing is known of.
        ObjectFaker<ConstructedEnvelope> supplied = new ObjectFaker<>(
                        ConstructedEnvelope.class,
                        GeneratorConfig.builder()
                                .objectMaxDepth(1)
                                .objectOverride(
                                        field -> field.getName().equals("middle")
                                                && field.getDeclaringClass() == ConstructedEnvelope.class,
                                        () -> null)
                                .build())
                .ruleFor("id", () -> "x")
                .ruleFor("middle.purchase.note", () -> "n");
        assertEquals(List.of(), problemsOf(supplied.followConstructorArguments()));
    }

    static final class NewMiddle implements Generator<Middle> {
        @Override
        public Middle generate() {
            return new Middle();
        }
    }

    static final class RandomizedEnvelope extends ObjectFakerStrictDiagnosticsTest.BaseEntity {
        @Randomizer(NewMiddle.class)
        Middle middle;

        RandomizedEnvelope(Middle middle) {
            this.middle = middle;
        }
    }

    static class EnvelopeBase extends ObjectFakerStrictDiagnosticsTest.BaseEntity {
        Middle middle;
    }

    static final class SubEnvelope extends EnvelopeBase {
        SubEnvelope(Middle middle) {
            this.middle = middle;
        }
    }

    @Test
    @DisplayName("a field with a @Randomizer keeps its argument, and one a root rule supplies does not")
    void randomizerKeepsTheArgumentAndARootRuleReplacesIt() {
        GeneratorConfig shallow = GeneratorConfig.builder().objectMaxDepth(1).build();
        ObjectFaker<RandomizedEnvelope> randomized = new ObjectFaker<>(RandomizedEnvelope.class, shallow)
                .ruleFor("id", () -> "x")
                .ruleFor("middle.purchase.note", () -> "n");

        // Generation keeps the middle the constructor stored, and the nested rule builds its purchase.
        assertEquals("x", randomized.generate().middle.purchase.id);
        assertAmbiguousAt(randomized.followConstructorArguments(), "id", "middle.purchase.id");

        // A root rule on the inherited field replaces the middle the constructor stored with a value that nothing
        // is known of, so nothing below it is followed, as below any member that a rule supplies.
        ObjectFaker<SubEnvelope> ruled = new ObjectFaker<>(SubEnvelope.class, shallow)
                .ruleFor("id", () -> "x")
                .ruleFor("middle", Middle::new)
                .ruleFor("middle.purchase.note", () -> "n");
        assertEquals(List.of(), problemsOf(ruled.followConstructorArguments()));
    }
}
