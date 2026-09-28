/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.testkit.engine.EngineExecutionResults;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Verifies that configurations resolved against a test <em>class</em> (for {@code @BeforeAll}
 * parameters or {@code PER_CLASS} constructors) never leak into test methods, and that
 * constructor injection under the default lifecycle observes the test method's own seed.
 */
class KrandomExtensionScopeEngineTest {

    @Test
    void beforeAllInjectionDoesNotOverrideMethodSeeds() {
        BeforeAllInjectionFixture.CLASS_SEEDS.clear();
        BeforeAllInjectionFixture.METHOD_SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(BeforeAllInjectionFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(2, BeforeAllInjectionFixture.CLASS_SEEDS.size(),
                     "@BeforeAll and @AfterAll share one class-scoped configuration");
        assertEquals(1, Set.copyOf(BeforeAllInjectionFixture.CLASS_SEEDS).size());
        assertEquals(2, Set.copyOf(BeforeAllInjectionFixture.METHOD_SEEDS).size(),
                     "unpinned tests must not reuse the class-scoped seed stored for @BeforeAll");
    }

    @Test
    void constructorInjectionObservesTheTestMethodSeed() {
        ConstructorInjectionFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(ConstructorInjectionFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(2, Set.copyOf(ConstructorInjectionFixture.SEEDS).size(),
                     "each test instance must be constructed with its own test method's seed");
    }

    @Test
    void nestedClassesDoNotLeakEnclosingClassScopedSeeds() {
        NestedOuterFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(NestedOuterFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(2, Set.copyOf(NestedOuterFixture.SEEDS).size(),
                     "unpinned nested tests must not reuse an enclosing class-scoped seed");
    }

    @Test
    void perClassLifecycleKeepsClassAndMethodSeedsSeparate() {
        EngineExecutionResults results = EngineTestSupport.run(PerClassPinnedFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
    }

    @Test
    void perClassLifecycleGivesUnpinnedMethodsTheirOwnSeeds() {
        PerClassUnpinnedFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(PerClassUnpinnedFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertEquals(2, Set.copyOf(PerClassUnpinnedFixture.SEEDS).size(),
                     "PER_CLASS constructor injection must not pin every test method to the class seed");
    }

    @Test
    void failureReportsTheSeedTheFailingMethodActuallyUsed() {
        BeforeAllFailingFixture.reset();

        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(BeforeAllFailingFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        long reported = Long.parseLong(EngineTestSupport.singleReportEntry(
            run.results(), KrandomExtension.REPORT_ENTRY_KEY));
        assertEquals(BeforeAllFailingFixture.methodSeed, reported);
        assertNotEquals(BeforeAllFailingFixture.classSeed, reported,
                        "the class-scoped @BeforeAll seed must not be reported as the test seed");
        assertEquals(new Random(reported).nextLong(), BeforeAllFailingFixture.methodValue,
                     "pinning the reported seed must reproduce the failing test's data");
        assertTrue(run.stderr().contains("Annotate it with @KrandomSeed(" + reported + "L)"), run.stderr());
        assertTrue(run.stderr().contains("class-scoped configuration")
                   && run.stderr().contains(Long.toString(BeforeAllFailingFixture.classSeed)),
                   "the class-scoped seed used by @BeforeAll must be reported separately, was: " + run.stderr());
    }

    @Test
    void methodLevelSeedIsHonouredNextToBeforeAllInjection() {
        PinnedBesideBeforeAllFixture.RECORDED.clear();

        EngineExecutionResults results = EngineTestSupport.run(PinnedBesideBeforeAllFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(List.of(new Random(4242L).nextLong()), PinnedBesideBeforeAllFixture.RECORDED,
                     "@KrandomSeed on the method must reproduce the same data as a pinned configuration");
    }

    // ── Fixtures: disabled in the regular suite, executed only via EngineTestKit ────────────

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class BeforeAllInjectionFixture {

        static final List<Long> CLASS_SEEDS = new CopyOnWriteArrayList<>();
        static final List<Long> METHOD_SEEDS = new CopyOnWriteArrayList<>();

        @BeforeAll
        static void setUp(GeneratorConfig config) {
            CLASS_SEEDS.add(config.getSeed().orElseThrow());
        }

        @AfterAll
        static void tearDown(GeneratorConfig config) {
            CLASS_SEEDS.add(config.getSeed().orElseThrow());
        }

        @KrandomSeed(8L)
        @Test
        void pinned(GeneratorConfig config) {
            assertEquals(8L, config.getSeed().orElseThrow());
        }

        @Test
        void firstUnpinned(GeneratorConfig config) {
            METHOD_SEEDS.add(config.getSeed().orElseThrow());
        }

        @Test
        void secondUnpinned(GeneratorConfig config) {
            METHOD_SEEDS.add(config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class ConstructorInjectionFixture {

        static final List<Long> SEEDS = new CopyOnWriteArrayList<>();

        private final GeneratorConfig constructorConfig;

        ConstructorInjectionFixture(GeneratorConfig config) {
            this.constructorConfig = config;
        }

        @KrandomSeed(11L)
        @Test
        void pinned(GeneratorConfig config) {
            assertEquals(11L, constructorConfig.getSeed().orElseThrow());
            assertEquals(11L, config.getSeed().orElseThrow());
        }

        @Test
        void firstUnpinned(GeneratorConfig config) {
            assertEquals(config.getSeed(), constructorConfig.getSeed());
            SEEDS.add(config.getSeed().orElseThrow());
        }

        @Test
        void secondUnpinned(GeneratorConfig config) {
            assertEquals(config.getSeed(), constructorConfig.getSeed());
            SEEDS.add(config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NestedOuterFixture {

        static final List<Long> SEEDS = new CopyOnWriteArrayList<>();

        @BeforeAll
        static void outerSetUp(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }

        @Nested
        class Inner {

            private final GeneratorConfig constructorConfig;

            Inner(GeneratorConfig config) {
                this.constructorConfig = config;
            }

            @BeforeAll
            static void innerSetUp(GeneratorConfig config) {
                assertTrue(config.getSeed().isPresent());
            }

            @KrandomSeed(21L)
            @Test
            void pinned(GeneratorConfig config) {
                assertEquals(21L, config.getSeed().orElseThrow());
                assertEquals(21L, constructorConfig.getSeed().orElseThrow());
            }

            @Test
            void firstUnpinned(GeneratorConfig config) {
                assertEquals(config.getSeed(), constructorConfig.getSeed());
                SEEDS.add(config.getSeed().orElseThrow());
            }

            @Test
            void secondUnpinned(GeneratorConfig config) {
                assertEquals(config.getSeed(), constructorConfig.getSeed());
                SEEDS.add(config.getSeed().orElseThrow());
            }
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @KrandomSeed(30L)
    static class PerClassPinnedFixture {

        private final GeneratorConfig constructorConfig;

        PerClassPinnedFixture(GeneratorConfig config) {
            this.constructorConfig = config;
        }

        @KrandomSeed(31L)
        @Test
        void methodSeedWins(GeneratorConfig config) {
            assertEquals(31L, config.getSeed().orElseThrow());
            assertEquals(30L, constructorConfig.getSeed().orElseThrow(),
                         "PER_CLASS constructors resolve against the class and use the class-level seed");
        }

        @Test
        void classSeedApplies(GeneratorConfig config) {
            assertEquals(30L, config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(KrandomExtension.class)
    static class PerClassUnpinnedFixture {

        static final List<Long> SEEDS = new CopyOnWriteArrayList<>();

        PerClassUnpinnedFixture(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }

        @Test
        void first(GeneratorConfig config) {
            SEEDS.add(config.getSeed().orElseThrow());
        }

        @Test
        void second(GeneratorConfig config) {
            SEEDS.add(config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class BeforeAllFailingFixture {

        static long classSeed;
        static long methodSeed;
        static long methodValue;

        static void reset() {
            classSeed = 0L;
            methodSeed = 0L;
            methodValue = 0L;
        }

        @BeforeAll
        static void setUp(GeneratorConfig config) {
            classSeed = config.getSeed().orElseThrow();
        }

        @Test
        void boom(GeneratorConfig config) {
            methodSeed = config.getSeed().orElseThrow();
            methodValue = config.createRandom().nextLong();
            fail("intentional fixture failure");
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class PinnedBesideBeforeAllFixture {

        static final List<Long> RECORDED = new CopyOnWriteArrayList<>();

        @BeforeAll
        static void setUp(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }

        @KrandomSeed(4242L)
        @Test
        void pinned(GeneratorConfig config) {
            RECORDED.add(config.createRandom().nextLong());
        }
    }
}
