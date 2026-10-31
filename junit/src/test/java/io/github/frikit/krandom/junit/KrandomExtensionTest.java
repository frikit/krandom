/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import static io.github.frikit.krandom.junit.EngineTestSupport.draw;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.Krandom;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

class KrandomExtensionTest {

    @KrandomSeed(42L)
    @Test
    void pinnedNumericSeedIsInjectedAndMetaRegistersTheExtension(GeneratorConfig config) {
        assertEquals(42L, config.getSeed().getAsLong());
    }

    @KrandomSeed(text = "checkout-flow")
    @Test
    void pinnedTextSeedDerivesLikeGeneratorConfig(GeneratorConfig config) {
        assertEquals(
                GeneratorConfig.deriveSeed("checkout-flow"), config.getSeed().getAsLong());
        assertEquals("checkout-flow", config.getStringSeed().orElseThrow());
    }

    @KrandomSeed(42L)
    @Test
    void builderInjectionCarriesThePinnedSeed(GeneratorConfig.Builder builder) {
        assertEquals(42L, builder.build().getSeed().getAsLong());
    }

    @KrandomSeed(1234L)
    @Test
    void sameSeedProducesSameData(GeneratorConfig first, GeneratorConfig second) {
        assertEquals(
                Generators.ofFullName(first).generate(),
                Generators.ofFullName(second).generate());
    }

    @KrandomSeed(42L)
    @Test
    void pinnedNumericSeedBuildsTheSession(Krandom fake) {
        assertEquals(42L, fake.getConfig().getSeed().getAsLong());
        assertEquals(EngineTestSupport.newSession(42L).fullName(), fake.fullName());
    }

    @KrandomSeed(text = "checkout-flow")
    @Test
    void pinnedTextSeedBuildsTheSession(Krandom fake) {
        assertEquals("checkout-flow", fake.getConfig().getStringSeed().orElseThrow());
        assertEquals(Krandom.seeded("checkout-flow").fullName(), fake.fullName());
    }

    @KrandomSeed(1234L)
    @Test
    void sessionDrawsWhatASessionBuiltFromTheInjectedConfigDraws(Krandom fake, GeneratorConfig config) {
        assertSame(config, fake.getConfig());
        Krandom rebuilt = Krandom.of(config);
        assertEquals(rebuilt.fullName(), fake.fullName());
        assertEquals(rebuilt.email(), fake.email());
        assertEquals(draw(rebuilt), draw(fake));
    }

    @KrandomSeed(1234L)
    @Test
    void everyKrandomParameterOfOneMethodIsTheSameSession(Krandom first, Krandom second) {
        assertSame(first, second);
    }

    @KrandomSeed(1234L)
    @Test
    void sessionBuiltFromTheInjectedBuilderKeepsTheSeedAndTakesTheChange(
            Krandom fake, GeneratorConfig.Builder builder) {
        Krandom british = Krandom.of(builder.locale(Locale.UK).build());

        assertEquals(Locale.UK, british.getConfig().getLocale());
        assertEquals(fake.getConfig().getSeed(), british.getConfig().getSeed());
        assertEquals(Locale.US, fake.getConfig().getLocale(), "the injected session keeps the test's configuration");
    }

    @KrandomSeed(1234L)
    @Test
    void streamConfigDerivesASessionWithStreamsOfItsOwn(Krandom fake) {
        Krandom derived = Krandom.of(fake.streamConfig("class-fixtures"));

        assertNotEquals(draw(derived), draw(fake));
    }

    @KrandomSeed(5150L)
    @Nested
    class SessionAcrossLifecycleMethods {

        private final Krandom fromConstructor;
        private Krandom fromBeforeEach;
        private int drawnInBeforeEach;

        SessionAcrossLifecycleMethods(Krandom fake) {
            this.fromConstructor = fake;
        }

        @BeforeEach
        void setUp(Krandom fake) {
            fromBeforeEach = fake;
            drawnInBeforeEach = draw(fake);
        }

        @Test
        void theTestMethodContinuesTheStreamsOfBeforeEach(Krandom fake, GeneratorConfig config) {
            assertSame(fromConstructor, fake);
            assertSame(fromBeforeEach, fake);

            int drawnInTest = draw(fake);

            Krandom afresh = Krandom.of(config);
            int first = draw(afresh);
            int second = draw(afresh);
            assertNotEquals(first, second, "the stream must be able to show a restart");
            assertEquals(first, drawnInBeforeEach);
            assertEquals(second, drawnInTest, "a session built again here would repeat the value of @BeforeEach");
        }

        @AfterEach
        void tearDown(Krandom fake) {
            assertSame(fromBeforeEach, fake);
            Krandom afresh = Krandom.of(fake.getConfig());
            draw(afresh);
            draw(afresh);
            assertEquals(draw(afresh), draw(fake), "@AfterEach continues after @BeforeEach and the test method");
        }
    }

    @ExtendWith(KrandomExtension.class)
    @Nested
    class Unpinned {

        @Test
        void unpinnedTestStillRunsWithAConcreteSeed(GeneratorConfig config) {
            assertTrue(config.getSeed().isPresent());
        }

        @Test
        void allInjectionsInOneTestShareTheSameSeed(GeneratorConfig first, GeneratorConfig.Builder second) {
            assertEquals(first.getSeed(), second.build().getSeed());
        }

        @Test
        void unpinnedSessionIsBuiltFromTheConfigurationOfTheTest(Krandom fake, GeneratorConfig config) {
            assertSame(config, fake.getConfig());
            assertTrue(fake.getConfig().getSeed().isPresent());
        }
    }

    @KrandomSeed(7L)
    @Nested
    class ClassLevelSeed {

        @Test
        void classLevelSeedAppliesToUnannotatedTests(GeneratorConfig config) {
            assertEquals(7L, config.getSeed().getAsLong());
        }

        @KrandomSeed(8L)
        @Test
        void methodLevelSeedOverridesClassLevelSeed(GeneratorConfig config) {
            assertEquals(8L, config.getSeed().getAsLong());
        }
    }
}
