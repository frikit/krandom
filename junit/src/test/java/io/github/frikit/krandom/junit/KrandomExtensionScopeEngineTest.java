/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static io.github.frikit.krandom.junit.EngineTestSupport.draw;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.AfterParameterizedClassInvocation;
import org.junit.jupiter.params.BeforeParameterizedClassInvocation;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;

/**
 * Verifies that configurations and sessions resolved against a test <em>class</em> (for
 * {@code @BeforeAll} parameters or {@code PER_CLASS} constructors) never leak into test methods,
 * and that constructor injection under the default lifecycle observes the test method's own seed
 * and session.
 */
class KrandomExtensionScopeEngineTest {

    @Test
    void everyTestGetsASessionOfItsOwn() {
        SessionPerTestFixture.SESSIONS.clear();
        SessionPerTestFixture.FIRST_DRAWS.clear();

        EngineExecutionResults results = EngineTestSupport.run(SessionPerTestFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertEquals(2, Set.copyOf(SessionPerTestFixture.SESSIONS).size(), "each test has its own instance");
        int first = draw(EngineTestSupport.newSession(60L));
        assertEquals(
                List.of(first, first),
                SessionPerTestFixture.FIRST_DRAWS,
                "a test starts its streams, whichever test drew before it");
    }

    @Test
    void classCallbacksShareOneSessionThatTestsNeitherReuseNorAdvance() {
        ClassSessionFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(ClassSessionFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertSame(ClassSessionFixture.fromBeforeAll, ClassSessionFixture.fromAfterAll);
        assertEquals(2, Set.copyOf(ClassSessionFixture.TEST_SESSIONS).size());
        assertFalse(ClassSessionFixture.TEST_SESSIONS.contains(ClassSessionFixture.fromBeforeAll));
        Krandom afresh = EngineTestSupport.newSession(70L);
        int first = draw(afresh);
        assertEquals(first, ClassSessionFixture.drawnInBeforeAll);
        assertEquals(
                draw(afresh),
                ClassSessionFixture.drawnInAfterAll,
                "@AfterAll continues after @BeforeAll: tests in between do not advance the class-level session");
        assertEquals(
                List.of(first, first),
                ClassSessionFixture.TEST_DRAWS,
                "a class-level pin gives both scopes one seed, so a test repeats the class-level session's values");
    }

    @Test
    void unpinnedClassLevelAndTestSessionsHaveSeparateSeedsAndAMethodPinApplies() {
        UnpinnedClassSessionFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(UnpinnedClassSessionFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertSame(UnpinnedClassSessionFixture.classConfig, UnpinnedClassSessionFixture.classSession.getConfig());
        assertEquals(
                3,
                Set.of(
                                UnpinnedClassSessionFixture.classConfig
                                        .getSeed()
                                        .orElseThrow(),
                                UnpinnedClassSessionFixture.TEST_SEEDS.get(0),
                                UnpinnedClassSessionFixture.TEST_SEEDS.get(1))
                        .size());
        assertEquals(8L, UnpinnedClassSessionFixture.pinnedSeed);
    }

    @Test
    void perClassConstructorSharesTheClassLevelSession() {
        PerClassSessionFixture.CLASS_SESSIONS.clear();
        PerClassSessionFixture.TEST_SESSIONS.clear();

        EngineExecutionResults results = EngineTestSupport.run(PerClassSessionFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertEquals(3, PerClassSessionFixture.CLASS_SESSIONS.size());
        assertEquals(1, Set.copyOf(PerClassSessionFixture.CLASS_SESSIONS).size());
        assertEquals(2, Set.copyOf(PerClassSessionFixture.TEST_SESSIONS).size());
        assertTrue(Collections.disjoint(PerClassSessionFixture.CLASS_SESSIONS, PerClassSessionFixture.TEST_SESSIONS));
    }

    @Test
    void nestedClassesHaveAClassLevelSessionOfTheirOwnAndEnclosingCallbacksShareTheTestSession() {
        NestedSessionFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(NestedSessionFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertNotSame(NestedSessionFixture.outerClassSession, NestedSessionFixture.innerClassSession);
        assertEquals(2, Set.copyOf(NestedSessionFixture.TEST_SESSIONS).size());
        assertFalse(NestedSessionFixture.TEST_SESSIONS.contains(NestedSessionFixture.outerClassSession));
        assertFalse(NestedSessionFixture.TEST_SESSIONS.contains(NestedSessionFixture.innerClassSession));
        int first = draw(EngineTestSupport.newSession(90L));
        assertEquals(first, NestedSessionFixture.outerClassDraw);
        assertEquals(first, NestedSessionFixture.innerClassDraw, "a nested class inherits the pin, so draws the same");
    }

    @Test
    void factoryMethodAndItsCallbacksShareOneSessionThatDynamicTestsCapture() {
        FactorySessionFixture.SESSIONS.clear();

        EngineExecutionResults results = EngineTestSupport.run(FactorySessionFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(4, FactorySessionFixture.SESSIONS.size());
        assertEquals(1, Set.copyOf(FactorySessionFixture.SESSIONS).size());
    }

    @Test
    void methodSourceFactoriesReceiveTheClassScopedConfigurationAndSessionsOfTheirOwn() {
        FactoryScopeFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(FactoryScopeFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        GeneratorConfig classConfig = FactoryScopeFixture.classSession.getConfig();
        assertEquals(2, FactoryScopeFixture.FACTORY_CONFIGS.size());
        for (GeneratorConfig factoryConfig : FactoryScopeFixture.FACTORY_CONFIGS) {
            assertSame(classConfig, factoryConfig, "a factory receives the class-scoped configuration");
        }
        assertEquals(2, Set.copyOf(FactoryScopeFixture.FACTORY_SESSIONS).size(), "each factory has its own session");
        assertFalse(FactoryScopeFixture.FACTORY_SESSIONS.contains(FactoryScopeFixture.classSession));
        assertTrue(Collections.disjoint(FactoryScopeFixture.FACTORY_SESSIONS, FactoryScopeFixture.TEST_SESSIONS));
        for (Krandom factorySession : FactoryScopeFixture.FACTORY_SESSIONS) {
            assertSame(classConfig, factorySession.getConfig());
        }
        Krandom afresh = Krandom.of(classConfig);
        int first = draw(afresh);
        assertEquals(first, FactoryScopeFixture.drawnInBeforeAll);
        assertEquals(
                List.of(first, first),
                FactoryScopeFixture.FACTORY_DRAWS,
                "a factory starts the class-level streams again, whichever factory drew before it");
        assertEquals(
                draw(afresh),
                FactoryScopeFixture.drawnInAfterAll,
                "@AfterAll continues after @BeforeAll: factories in between do not advance the class-level session");
        assertFalse(
                FactoryScopeFixture.TEST_SEEDS.contains(classConfig.getSeed().orElseThrow()),
                "an unpinned invocation keeps a seed of its own");
    }

    @Test
    void aMethodLevelSeedLeavesTheFactoryWithTheClassLevelSeed() {
        PinnedFactoryFixture.FACTORY_SEEDS.clear();
        PinnedFactoryFixture.TEST_SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(PinnedFactoryFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(2).failed(0));
        assertEquals(List.of(31L), PinnedFactoryFixture.FACTORY_SEEDS);
        assertEquals(List.of(32L, 32L), PinnedFactoryFixture.TEST_SEEDS);
    }

    @Test
    void perClassFactoryReceivesTheConfigurationOfTheConstructor() {
        PerClassFactoryFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(PerClassFactoryFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertSame(PerClassFactoryFixture.constructorConfig, PerClassFactoryFixture.factoryConfig);
        assertNotSame(PerClassFactoryFixture.constructorConfig, PerClassFactoryFixture.testConfig);
    }

    @Test
    void nestedFactoryReceivesTheClassScopedConfigurationOfItsOwnClass() {
        NestedFactoryFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(NestedFactoryFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertSame(NestedFactoryFixture.innerConfig, NestedFactoryFixture.factoryConfig);
        assertNotSame(NestedFactoryFixture.outerConfig, NestedFactoryFixture.factoryConfig);
    }

    @Test
    void parameterizedClassCallbacksShareTheClassScopedConfigurationAndTestsKeepTheirOwn() {
        ParameterizedClassScopeFixture.reset();

        EngineExecutionResults results = EngineTestSupport.run(ParameterizedClassScopeFixture.class);

        results.containerEvents().assertStatistics(stats -> stats.failed(0));
        results.testEvents().assertStatistics(stats -> stats.succeeded(4).failed(0));
        assertEquals(
                6,
                ParameterizedClassScopeFixture.CLASS_CONFIGS.size(),
                "the class factory, @BeforeAll, and each invocation's callbacks and @ParameterizedTest factory"
                        + " resolve a configuration");
        assertEquals(1, Set.copyOf(ParameterizedClassScopeFixture.CLASS_CONFIGS).size());
        GeneratorConfig classConfig = ParameterizedClassScopeFixture.CLASS_CONFIGS.get(0);
        assertSame(classConfig, ParameterizedClassScopeFixture.classSession.getConfig());
        assertSame(
                ParameterizedClassScopeFixture.classSession,
                ParameterizedClassScopeFixture.factorySession,
                "JUnit calls the factory of a parameterized class with the context of @BeforeAll");
        assertEquals(
                2,
                Set.copyOf(ParameterizedClassScopeFixture.INVOCATION_SESSIONS).size());
        assertFalse(ParameterizedClassScopeFixture.INVOCATION_SESSIONS.contains(
                ParameterizedClassScopeFixture.classSession));
        assertEquals(4, Set.copyOf(ParameterizedClassScopeFixture.TEST_CONFIGS).size());
        assertFalse(ParameterizedClassScopeFixture.TEST_CONFIGS.contains(classConfig));
    }

    @Test
    void beforeAllInjectionDoesNotOverrideMethodSeeds() {
        BeforeAllInjectionFixture.CLASS_SEEDS.clear();
        BeforeAllInjectionFixture.METHOD_SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(BeforeAllInjectionFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(
                2,
                BeforeAllInjectionFixture.CLASS_SEEDS.size(),
                "@BeforeAll and @AfterAll share one class-scoped configuration");
        assertEquals(1, Set.copyOf(BeforeAllInjectionFixture.CLASS_SEEDS).size());
        assertEquals(
                2,
                Set.copyOf(BeforeAllInjectionFixture.METHOD_SEEDS).size(),
                "unpinned tests must not reuse the class-scoped seed stored for @BeforeAll");
    }

    @Test
    void constructorInjectionObservesTheTestMethodSeed() {
        ConstructorInjectionFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(ConstructorInjectionFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(
                2,
                Set.copyOf(ConstructorInjectionFixture.SEEDS).size(),
                "each test instance must be constructed with its own test method's seed");
    }

    @Test
    void nestedClassesDoNotLeakEnclosingClassScopedSeeds() {
        NestedOuterFixture.SEEDS.clear();

        EngineExecutionResults results = EngineTestSupport.run(NestedOuterFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(3).failed(0));
        assertEquals(
                2,
                Set.copyOf(NestedOuterFixture.SEEDS).size(),
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
        assertEquals(
                2,
                Set.copyOf(PerClassUnpinnedFixture.SEEDS).size(),
                "PER_CLASS constructor injection must not pin every test method to the class seed");
    }

    @Test
    void failureReportsTheSeedTheFailingMethodActuallyUsed() {
        BeforeAllFailingFixture.reset();

        EngineTestSupport.Captured run = EngineTestSupport.runCapturingStderr(BeforeAllFailingFixture.class);

        run.results().testEvents().assertStatistics(stats -> stats.failed(1));
        long reported =
                Long.parseLong(EngineTestSupport.singleReportEntry(run.results(), KrandomExtension.REPORT_ENTRY_KEY));
        assertEquals(BeforeAllFailingFixture.methodSeed, reported);
        assertNotEquals(
                BeforeAllFailingFixture.classSeed,
                reported,
                "the class-scoped @BeforeAll seed must not be reported as the test seed");
        assertEquals(
                new Random(reported).nextLong(),
                BeforeAllFailingFixture.methodValue,
                "pinning the reported seed must reproduce the failing test's data");
        assertTrue(run.stderr().contains("Annotate it with @KrandomSeed(" + reported + "L)"), run.stderr());
        assertTrue(
                run.stderr().contains("class-scoped configuration")
                        && run.stderr().contains(Long.toString(BeforeAllFailingFixture.classSeed)),
                "the class-scoped seed used by @BeforeAll must be reported separately, was: " + run.stderr());
    }

    @Test
    void methodLevelSeedIsHonouredNextToBeforeAllInjection() {
        PinnedBesideBeforeAllFixture.RECORDED.clear();

        EngineExecutionResults results = EngineTestSupport.run(PinnedBesideBeforeAllFixture.class);

        results.testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
        assertEquals(
                List.of(new Random(4242L).nextLong()),
                PinnedBesideBeforeAllFixture.RECORDED,
                "@KrandomSeed on the method must reproduce the same data as a pinned configuration");
    }

    // ── Fixtures: disabled in the regular suite, executed only via EngineTestKit ────────────

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @KrandomSeed(60L)
    static class SessionPerTestFixture {

        static final List<Krandom> SESSIONS = new CopyOnWriteArrayList<>();
        static final List<Integer> FIRST_DRAWS = new CopyOnWriteArrayList<>();

        @Test
        void first(Krandom fake) {
            record(fake);
        }

        @Test
        void second(Krandom fake) {
            record(fake);
        }

        /** Draws more than it records, so that a test following in a shared session would see later values. */
        private static void record(Krandom fake) {
            SESSIONS.add(fake);
            FIRST_DRAWS.add(draw(fake));
            draw(fake);
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @KrandomSeed(70L)
    static class ClassSessionFixture {

        static final List<Krandom> TEST_SESSIONS = new CopyOnWriteArrayList<>();
        static final List<Integer> TEST_DRAWS = new CopyOnWriteArrayList<>();
        static Krandom fromBeforeAll;
        static Krandom fromAfterAll;
        static int drawnInBeforeAll;
        static int drawnInAfterAll;

        static void reset() {
            TEST_SESSIONS.clear();
            TEST_DRAWS.clear();
            fromBeforeAll = null;
            fromAfterAll = null;
            drawnInBeforeAll = 0;
            drawnInAfterAll = 0;
        }

        @BeforeAll
        static void setUp(Krandom fake) {
            fromBeforeAll = fake;
            drawnInBeforeAll = draw(fake);
        }

        @AfterAll
        static void tearDown(Krandom fake) {
            fromAfterAll = fake;
            drawnInAfterAll = draw(fake);
        }

        @Test
        void first(Krandom fake) {
            TEST_SESSIONS.add(fake);
            TEST_DRAWS.add(draw(fake));
        }

        @Test
        void second(Krandom fake) {
            TEST_SESSIONS.add(fake);
            TEST_DRAWS.add(draw(fake));
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class UnpinnedClassSessionFixture {

        static final List<Long> TEST_SEEDS = new CopyOnWriteArrayList<>();
        static Krandom classSession;
        static GeneratorConfig classConfig;
        static long pinnedSeed;

        static void reset() {
            TEST_SEEDS.clear();
            classSession = null;
            classConfig = null;
            pinnedSeed = 0L;
        }

        @BeforeAll
        static void setUp(Krandom fake, GeneratorConfig config) {
            classSession = fake;
            classConfig = config;
        }

        @Test
        void first(Krandom fake) {
            TEST_SEEDS.add(fake.getConfig().getSeed().orElseThrow());
        }

        @Test
        void second(Krandom fake) {
            TEST_SEEDS.add(fake.getConfig().getSeed().orElseThrow());
        }

        @KrandomSeed(8L)
        @Test
        void pinned(Krandom fake) {
            pinnedSeed = fake.getConfig().getSeed().orElseThrow();
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @KrandomSeed(80L)
    static class PerClassSessionFixture {

        static final List<Krandom> CLASS_SESSIONS = new CopyOnWriteArrayList<>();
        static final List<Krandom> TEST_SESSIONS = new CopyOnWriteArrayList<>();

        PerClassSessionFixture(Krandom fake) {
            CLASS_SESSIONS.add(fake);
        }

        @BeforeAll
        void setUp(Krandom fake) {
            CLASS_SESSIONS.add(fake);
        }

        @AfterAll
        void tearDown(Krandom fake) {
            CLASS_SESSIONS.add(fake);
        }

        @Test
        void first(Krandom fake) {
            TEST_SESSIONS.add(fake);
        }

        @Test
        void second(Krandom fake) {
            TEST_SESSIONS.add(fake);
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @KrandomSeed(90L)
    static class NestedSessionFixture {

        static final List<Krandom> TEST_SESSIONS = new CopyOnWriteArrayList<>();
        static Krandom outerClassSession;
        static Krandom innerClassSession;
        static int outerClassDraw;
        static int innerClassDraw;

        static void reset() {
            TEST_SESSIONS.clear();
            outerClassSession = null;
            innerClassSession = null;
            outerClassDraw = 0;
            innerClassDraw = 0;
        }

        private final Krandom outerConstructorSession;
        private Krandom outerBeforeEachSession;

        NestedSessionFixture(Krandom fake) {
            this.outerConstructorSession = fake;
        }

        @BeforeAll
        static void outerSetUp(Krandom fake) {
            outerClassSession = fake;
            outerClassDraw = draw(fake);
        }

        @BeforeEach
        void outerBeforeEach(Krandom fake) {
            outerBeforeEachSession = fake;
        }

        @AfterEach
        void outerAfterEach(Krandom fake) {
            assertSame(outerBeforeEachSession, fake);
        }

        @Nested
        class Inner {

            private final Krandom innerConstructorSession;

            Inner(Krandom fake) {
                this.innerConstructorSession = fake;
            }

            @BeforeAll
            static void innerSetUp(Krandom fake) {
                innerClassSession = fake;
                innerClassDraw = draw(fake);
            }

            @Test
            void first(Krandom fake) {
                record(fake);
            }

            @Test
            void second(Krandom fake) {
                record(fake);
            }

            private void record(Krandom fake) {
                assertSame(outerConstructorSession, fake);
                assertSame(innerConstructorSession, fake);
                assertSame(outerBeforeEachSession, fake);
                TEST_SESSIONS.add(fake);
            }
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class FactorySessionFixture {

        static final List<Krandom> SESSIONS = new CopyOnWriteArrayList<>();

        @BeforeEach
        void setUp(Krandom fake) {
            SESSIONS.add(fake);
        }

        @TestFactory
        List<DynamicTest> factory(Krandom fake) {
            SESSIONS.add(fake);
            return List.of(DynamicTest.dynamicTest("captures the factory's session", () -> SESSIONS.add(fake)));
        }

        @AfterEach
        void tearDown(Krandom fake) {
            SESSIONS.add(fake);
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class FactoryScopeFixture {

        static final List<GeneratorConfig> FACTORY_CONFIGS = new CopyOnWriteArrayList<>();
        static final List<Krandom> FACTORY_SESSIONS = new CopyOnWriteArrayList<>();
        static final List<Integer> FACTORY_DRAWS = new CopyOnWriteArrayList<>();
        static final List<Krandom> TEST_SESSIONS = new CopyOnWriteArrayList<>();
        static final List<Long> TEST_SEEDS = new CopyOnWriteArrayList<>();
        static Krandom classSession;
        static int drawnInBeforeAll;
        static int drawnInAfterAll;

        static void reset() {
            FACTORY_CONFIGS.clear();
            FACTORY_SESSIONS.clear();
            FACTORY_DRAWS.clear();
            TEST_SESSIONS.clear();
            TEST_SEEDS.clear();
            classSession = null;
            drawnInBeforeAll = 0;
            drawnInAfterAll = 0;
        }

        private final Krandom constructorSession;
        private Krandom beforeEachSession;

        FactoryScopeFixture(Krandom fake) {
            this.constructorSession = fake;
        }

        @BeforeAll
        static void setUp(Krandom fake) {
            classSession = fake;
            drawnInBeforeAll = draw(fake);
        }

        @AfterAll
        static void tearDown(Krandom fake) {
            assertSame(classSession, fake);
            drawnInAfterAll = draw(fake);
        }

        /** Draws more than it records, so that a factory following in a shared session would see later values. */
        static Stream<Integer> values(Krandom fake, GeneratorConfig config) {
            FACTORY_CONFIGS.add(config);
            FACTORY_SESSIONS.add(fake);
            FACTORY_DRAWS.add(draw(fake));
            draw(fake);
            return Stream.of(1);
        }

        @BeforeEach
        void beforeEach(Krandom fake) {
            beforeEachSession = fake;
        }

        @ParameterizedTest
        @MethodSource("values")
        void first(int ignored, Krandom fake) {
            record(fake);
        }

        @ParameterizedTest
        @MethodSource("values")
        void second(int ignored, Krandom fake) {
            record(fake);
        }

        private void record(Krandom fake) {
            assertSame(constructorSession, fake, "the constructor belongs to the invocation it is called for");
            assertSame(beforeEachSession, fake, "@BeforeEach belongs to the invocation it runs for");
            TEST_SESSIONS.add(fake);
            TEST_SEEDS.add(fake.getConfig().getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @KrandomSeed(31L)
    static class PinnedFactoryFixture {

        static final List<Long> FACTORY_SEEDS = new CopyOnWriteArrayList<>();
        static final List<Long> TEST_SEEDS = new CopyOnWriteArrayList<>();

        static Stream<Integer> values(GeneratorConfig config) {
            FACTORY_SEEDS.add(config.getSeed().orElseThrow());
            return Stream.of(1, 2);
        }

        @KrandomSeed(32L)
        @ParameterizedTest
        @MethodSource("values")
        void pinned(int ignored, GeneratorConfig config) {
            TEST_SEEDS.add(config.getSeed().orElseThrow());
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @ExtendWith(KrandomExtension.class)
    static class PerClassFactoryFixture {

        static GeneratorConfig constructorConfig;
        static GeneratorConfig factoryConfig;
        static GeneratorConfig testConfig;

        static void reset() {
            constructorConfig = null;
            factoryConfig = null;
            testConfig = null;
        }

        PerClassFactoryFixture(GeneratorConfig config) {
            constructorConfig = config;
        }

        Stream<Integer> values(GeneratorConfig config) {
            factoryConfig = config;
            return Stream.of(1);
        }

        @ParameterizedTest
        @MethodSource("values")
        void test(int ignored, GeneratorConfig config) {
            testConfig = config;
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    static class NestedFactoryFixture {

        static GeneratorConfig outerConfig;
        static GeneratorConfig innerConfig;
        static GeneratorConfig factoryConfig;

        static void reset() {
            outerConfig = null;
            innerConfig = null;
            factoryConfig = null;
        }

        @BeforeAll
        static void outerSetUp(GeneratorConfig config) {
            outerConfig = config;
        }

        @Nested
        class Inner {

            @BeforeAll
            static void innerSetUp(GeneratorConfig config) {
                innerConfig = config;
            }

            static Stream<Integer> values(GeneratorConfig config) {
                factoryConfig = config;
                return Stream.of(1);
            }

            @ParameterizedTest
            @MethodSource("values")
            void test(int ignored, GeneratorConfig config) {
                assertNotSame(innerConfig, config);
            }
        }
    }

    @Disabled("fixture for KrandomExtensionScopeEngineTest")
    @ExtendWith(KrandomExtension.class)
    @ParameterizedClass
    @MethodSource("values")
    static class ParameterizedClassScopeFixture {

        static final List<GeneratorConfig> CLASS_CONFIGS = new CopyOnWriteArrayList<>();
        static final List<Krandom> INVOCATION_SESSIONS = new CopyOnWriteArrayList<>();
        static final List<GeneratorConfig> TEST_CONFIGS = new CopyOnWriteArrayList<>();
        static Krandom classSession;
        static Krandom factorySession;
        static Krandom invocationSession;

        static void reset() {
            CLASS_CONFIGS.clear();
            INVOCATION_SESSIONS.clear();
            TEST_CONFIGS.clear();
            classSession = null;
            factorySession = null;
            invocationSession = null;
        }

        @Parameter
        int value;

        private final GeneratorConfig constructorConfig;

        ParameterizedClassScopeFixture(GeneratorConfig config) {
            this.constructorConfig = config;
        }

        static Stream<Integer> values(GeneratorConfig config, Krandom fake) {
            CLASS_CONFIGS.add(config);
            factorySession = fake;
            return Stream.of(1, 2);
        }

        @BeforeAll
        static void setUp(GeneratorConfig config, Krandom fake) {
            CLASS_CONFIGS.add(config);
            classSession = fake;
        }

        @BeforeParameterizedClassInvocation(injectArguments = false)
        static void beforeInvocation(GeneratorConfig config, Krandom fake) {
            CLASS_CONFIGS.add(config);
            INVOCATION_SESSIONS.add(fake);
            invocationSession = fake;
        }

        @AfterParameterizedClassInvocation(injectArguments = false)
        static void afterInvocation(Krandom fake) {
            assertSame(invocationSession, fake, "the callbacks of one invocation share its session");
        }

        @Test
        void test(GeneratorConfig config) {
            assertSame(constructorConfig, config, "the constructor belongs to the test it is called for");
            TEST_CONFIGS.add(config);
        }

        static Stream<String> letters(GeneratorConfig config) {
            CLASS_CONFIGS.add(config);
            return Stream.of("a");
        }

        @ParameterizedTest
        @MethodSource("letters")
        void parameterized(String letter, GeneratorConfig config) {
            assertSame(constructorConfig, config, "the constructor belongs to the invocation it is called for");
            TEST_CONFIGS.add(config);
        }
    }

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
            assertEquals(
                    30L,
                    constructorConfig.getSeed().orElseThrow(),
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
