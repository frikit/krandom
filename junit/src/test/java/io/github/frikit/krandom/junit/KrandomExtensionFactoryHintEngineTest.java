/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Verifies the reproduction hint of a failing invocation whose {@code @MethodSource} factory took an
 * unpinned class-scoped configuration: it names the class-scoped seed beside the test's own, so that
 * annotating the class and the test as printed reproduces the factory's arguments and the
 * invocation's data.
 */
class KrandomExtensionFactoryHintEngineTest {

    private static final Pattern CLASS_HINT = Pattern.compile("krandom: test '([^']+)' failed with seed (-?\\d+)\\."
            + " Annotate it with @KrandomSeed\\((-?\\d+)L\\) and its class with @KrandomSeed\\((-?\\d+)L\\) to"
            + " reproduce this run\\.");
    private static final Pattern CLASS_SCOPED_SEED =
            Pattern.compile("krandom: class-scoped configuration of '([^']+)' .*? used seed (-?\\d+);");

    @Test
    void followingThePrintedHintReproducesTheFactoryArgumentsAndTheInvocationData() {
        HintFixture.VALUES.clear();
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(HintFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher hint = CLASS_HINT.matcher(run.stderr());
        assertTrue(hint.find(), run.stderr());
        long testSeed = Long.parseLong(hint.group(3));
        long classSeed = Long.parseLong(hint.group(4));
        assertEquals(hint.group(2), hint.group(3));
        assertEquals(
                EngineTestSupport.singleReportEntry(run.results(), KrandomExtension.REPORT_ENTRY_KEY),
                Long.toString(testSeed));
        assertEquals(classSeedOf(run.stderr(), "HintFixture"), classSeed);

        // @KrandomSeed(v) gives the configuration that GeneratorConfig.builder().seed(v) builds, which the
        // annotated fixture below shows, so annotating the class and the test as printed draws these values.
        assertEquals(
                List.of(
                        "factory " + EngineTestSupport.draw(EngineTestSupport.newSession(classSeed)),
                        "invocation " + EngineTestSupport.draw(EngineTestSupport.newSession(testSeed))),
                HintFixture.VALUES);
    }

    @Test
    void annotatingTheClassAndTheTestGivesTheSessionsOfTheirSeeds() {
        AnnotatedFixture.VALUES.clear();
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(AnnotatedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        assertEquals(
                List.of(
                        "factory " + EngineTestSupport.draw(EngineTestSupport.newSession(7L)),
                        "invocation " + EngineTestSupport.draw(EngineTestSupport.newSession(11L))),
                AnnotatedFixture.VALUES);
    }

    @Test
    void aNestedFactoryNamesTheSeedOfItsOwnClass() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(NestedFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        Matcher hint = CLASS_HINT.matcher(run.stderr());
        assertTrue(hint.find(), run.stderr());
        assertEquals(classSeedOf(run.stderr(), "Inner"), Long.parseLong(hint.group(4)));
    }

    @Test
    void theHintIsUnchangedWhenTheFactoryTakesNoKrandomParameter() {
        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(ConstantFactoryFixture.class);

        assertTestSeedHintOnly(run);
    }

    @Test
    void theHintIsUnchangedWhenAClassRecipePinsTheFactory() {
        String recipe = GeneratorConfig.builder()
                .seed(5L)
                .build()
                .getGenerationRecipe()
                .orElseThrow()
                .serialize();
        String classRecipe = HintFixture.class.getCanonicalName() + "=base64:"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(recipe.getBytes(StandardCharsets.UTF_8));

        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(
                HintFixture.class, Map.of("krandom.junit.class-recipe", classRecipe));

        assertTestSeedHintOnly(run);
    }

    private static void assertTestSeedHintOnly(EngineTestSupport.Captured run) {
        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        String seed = EngineTestSupport.singleReportEntry(run.results(), KrandomExtension.REPORT_ENTRY_KEY);
        assertTrue(
                run.stderr()
                        .contains(" failed with seed " + seed + ". Annotate it with @KrandomSeed(" + seed
                                + "L) to reproduce this run."),
                run.stderr());
    }

    private static long classSeedOf(String stderr, String simpleName) {
        Matcher line = CLASS_SCOPED_SEED.matcher(stderr);
        while (line.find()) {
            if (line.group(1).equals(simpleName)) {
                return Long.parseLong(line.group(2));
            }
        }
        throw new AssertionError("no class-scoped seed of " + simpleName + " in: " + stderr);
    }

    @Disabled("fixture for KrandomExtensionFactoryHintEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class HintFixture {

        static final List<String> VALUES = new CopyOnWriteArrayList<>();

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add("factory " + value);
            return Stream.of(value);
        }

        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            VALUES.add("invocation " + EngineTestSupport.draw(fake));
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryHintEngineTest")
    @KrandomSeed(7)
    static class AnnotatedFixture {

        static final List<String> VALUES = new CopyOnWriteArrayList<>();

        static Stream<Integer> values(Krandom fake) {
            int value = EngineTestSupport.draw(fake);
            VALUES.add("factory " + value);
            return Stream.of(value);
        }

        @KrandomSeed(11)
        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            VALUES.add("invocation " + EngineTestSupport.draw(fake));
            throw new AssertionError("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryHintEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NestedFixture {

        @BeforeAll
        static void setUp(Krandom fake) {
            fake.email();
        }

        @Nested
        class Inner {

            static Stream<Integer> values(Krandom fake) {
                return Stream.of(EngineTestSupport.draw(fake));
            }

            @ParameterizedTest
            @MethodSource("values")
            void boom(int value) {
                throw new AssertionError("intentional fixture failure");
            }
        }
    }

    @Disabled("fixture for KrandomExtensionFactoryHintEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ConstantFactoryFixture {

        @BeforeAll
        static void setUp(Krandom fake) {
            fake.email();
        }

        static Stream<Integer> values() {
            return Stream.of(1);
        }

        @ParameterizedTest
        @MethodSource("values")
        void boom(int value, Krandom fake) {
            throw new AssertionError("intentional fixture failure");
        }
    }
}
