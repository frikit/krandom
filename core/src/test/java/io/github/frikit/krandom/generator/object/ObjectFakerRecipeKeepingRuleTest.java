/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.PLAIN_FIRST;
import static io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.PLAIN_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.failure.GenerationFailureDiagnostic;
import io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.Member;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import io.github.frikit.krandom.generator.report.GenerationStep;
import io.github.frikit.krandom.generator.report.SeedSource;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ObjectFaker.ruleForKeepingRecipe keeps the recipe and the other seeded values")
class ObjectFakerRecipeKeepingRuleTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig.Builder seeded() {
        return GeneratorConfig.builder().seed(42L).clock(CLOCK);
    }

    /** The plain value with its nickname replaced, as a rule that leaves the other members gives it. */
    private static String withNickname(String plain, String nickname) {
        String[] parts = plain.split("\\|", -1);
        parts[1] = nickname;
        return String.join("|", parts);
    }

    private static List<String> twice(ObjectFaker<Member> faker) {
        return List.of(
                ObjectEngineDefaultsPinTest.show(faker.generate()), ObjectEngineDefaultsPinTest.show(faker.generate()));
    }

    private static List<String> twice(GeneratorConfig config, UnaryOperator<ObjectFaker<Member>> rules) {
        return twice(rules.apply(new ObjectFaker<>(Member.class, config)));
    }

    public static class Task {
        public String nickname;
        public Runnable job;
    }

    public record Badge(String holder, String code) {}

    @Test
    @DisplayName("a root rule that keeps the recipe changes only its member, on every object")
    void changesOnlyItsMember() {
        GeneratorConfig config = seeded().build();

        assertEquals(
                List.of(withNickname(PLAIN_FIRST, "X"), withNickname(PLAIN_SECOND, "X")),
                twice(config, faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")));
    }

    @Test
    @DisplayName("its report keeps the recipe format and the derived streams, and names the rule")
    void reportKeepsTheRecipe() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        new ObjectFaker<>(
                        Member.class,
                        seeded().generationReportListener(entries::add).build())
                .ruleForKeepingRecipe("nickname", () -> "X")
                .generate();

        assertEquals(
                List.of(Optional.of("krandom-recipe/v1")),
                entries.stream()
                        .map(GenerationReportEntry::recipeFormat)
                        .distinct()
                        .toList());
        GenerationReportEntry nickname = entries.stream()
                .filter(entry -> entry.path().equals("Member.nickname"))
                .findFirst()
                .orElseThrow();
        assertEquals(GenerationStep.OVERRIDE, nickname.step());
        assertEquals(SeedSource.DERIVED_STREAM, nickname.seedSource());
        assertEquals(Optional.of(FixtureRule.registered(FixtureRuleKind.RULE, "nickname", 1)), nickname.matchedRule());
    }

    @Test
    @DisplayName("a failure it passes over names the configuration's recipe, which a plain root rule drops")
    void failureNamesTheRecipe() {
        List<GenerationFailureDiagnostic> diagnostics = new ArrayList<>();
        GeneratorConfig config = seeded().objectIgnoreErrors(true)
                .generationFailureListener(diagnostics::add)
                .build();

        new ObjectFaker<>(Task.class, config)
                .ruleForKeepingRecipe("nickname", () -> "X")
                .generate();
        new ObjectFaker<>(Task.class, config).ruleFor("nickname", () -> "X").generate();

        assertEquals(2, diagnostics.size());
        assertEquals(
                config.getGenerationRecipe().map(GenerationRecipe::serializeForDiagnostics),
                diagnostics.get(0).replayIdentity());
        assertEquals(Optional.empty(), diagnostics.get(1).replayIdentity());
    }

    @Test
    @DisplayName("another root rule, root context rule or root ignore drops the recipe as before")
    void otherRootDecisionsDropTheRecipe() {
        GeneratorConfig config = seeded().build();

        assertEquals(
                twice(config, faker -> faker.ruleFor("nickname", () -> "X").ruleFor("age", () -> 7)),
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")
                                .ruleFor("age", () -> 7)));
        assertEquals(
                twice(config, faker -> faker.ruleFor("nickname", () -> "X").ruleForContext("age", context -> 7)),
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")
                                .ruleForContext("age", context -> 7)));
        assertEquals(
                twice(config, faker -> faker.ruleFor("nickname", () -> "X").ignore("age")),
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")
                                .ignore("age")));
        assertEquals(
                twice(config, faker -> faker.ruleFor("nickname", () -> "X").include("nickname", "age", "address")),
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")
                                .include("nickname", "age", "address")));
    }

    @Test
    @DisplayName("with a configuration that has no portable recipe, it gives what a plain root rule gives")
    void nonPortableConfigurationIsUnchanged() {
        GeneratorConfig excluding = seeded().objectExcludeField("email").build();

        assertEquals(
                twice(excluding, faker -> faker.ruleFor("nickname", () -> "X")),
                twice(excluding, faker -> faker.ruleForKeepingRecipe("nickname", () -> "X")));
    }

    @Test
    @DisplayName("a later rule for the same path replaces it, and it replaces an earlier one")
    void replacesAndIsReplaced() {
        GeneratorConfig config = seeded().build();
        List<String> plainRule = twice(config, faker -> faker.ruleFor("nickname", () -> "X"));
        List<String> keeping = twice(config, faker -> faker.ruleForKeepingRecipe("nickname", () -> "X"));
        Function<Member, String> dependent = member -> "X";

        assertNotEquals(plainRule, keeping);
        assertEquals(
                plainRule,
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "Y")
                                .ruleFor("nickname", () -> "X")));
        assertEquals(
                plainRule,
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "Y")
                                .ruleForContext("nickname", context -> "X")));
        assertEquals(
                keeping,
                twice(
                        config,
                        faker -> faker.ruleFor("nickname", () -> "Y").ruleForKeepingRecipe("nickname", () -> "X")));
        assertEquals(
                keeping,
                twice(
                        config,
                        faker -> faker.ruleForContext("nickname", context -> "Y")
                                .ruleForKeepingRecipe("nickname", () -> "X")));
        // A dependent rule leaves the generation as it is, as it always did.
        assertEquals(
                keeping,
                twice(
                        config,
                        faker -> faker.ruleForKeepingRecipe("nickname", () -> "Y")
                                .ruleFor("nickname", dependent)));
    }

    @Test
    @DisplayName("on a nested path it is a nested rule, which keeps the recipe already")
    void nestedPathIsANestedRule() {
        GeneratorConfig config = seeded().build();

        assertEquals(
                twice(config, faker -> faker.ruleFor("address.city", () -> "C")),
                twice(config, faker -> faker.ruleForKeepingRecipe("address.city", () -> "C")));
    }

    @Test
    @DisplayName("a profile that fails rolls it back, and one that succeeds keeps it")
    void profilesRollItBack() {
        GeneratorConfig config = seeded().build();
        ObjectFaker<Member> faker = new ObjectFaker<>(Member.class, config)
                .ruleFor("nickname", () -> "X")
                .profile("keeping", profile -> profile.ruleForKeepingRecipe("nickname", () -> "X"))
                .profile("failing", profile -> {
                    profile.ruleForKeepingRecipe("nickname", () -> "X");
                    throw new IllegalStateException("profile failed");
                });

        try {
            faker.useProfile("failing");
        } catch (IllegalStateException expected) {
            // The configuration before the profile is restored.
        }
        assertEquals(twice(config, other -> other.ruleFor("nickname", () -> "X")), twice(faker));
        assertEquals(
                List.of(withNickname(PLAIN_FIRST, "X"), withNickname(PLAIN_SECOND, "X")),
                twice(new ObjectFaker<>(Member.class, config)
                        .ruleFor("nickname", () -> "X")
                        .profile("keeping", profile -> profile.ruleForKeepingRecipe("nickname", () -> "X"))
                        .useProfile("keeping")));
    }

    @Test
    @DisplayName("the accessor and property-path forms target the same member")
    void accessorAndPathForms() {
        GeneratorConfig config = seeded().build();
        Badge plain = new ObjectFaker<>(Badge.class, config).generate();

        Badge byAccessor = new ObjectFaker<>(Badge.class, config)
                .ruleForKeepingRecipe(Badge::holder, () -> "H")
                .generate();
        Badge byPath = new ObjectFaker<>(Badge.class, config)
                .ruleForKeepingRecipe(PropertyPath.of(Badge::holder), () -> "H")
                .generate();

        assertEquals(new Badge("H", plain.code()), byAccessor);
        assertEquals(byAccessor, byPath);
    }
}
