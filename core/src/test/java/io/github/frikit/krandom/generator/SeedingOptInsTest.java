/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import io.github.frikit.krandom.internal.ConfigAccess;
import io.github.frikit.krandom.internal.Reseeding;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * The 3.2 seeding opt-ins that become the defaults in 4.0: advancing convenience streams, {@link StrictMath}
 * Gaussian draws and reseeded built-in randomizers (the last is also covered in {@code RandomizerReplayTest}).
 */
@DisplayName("the seeding opt-ins of 3.2")
class SeedingOptInsTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig seeded(long seed, UnaryOperator<GeneratorConfig.Builder> optIns) {
        return optIns.apply(GeneratorConfig.builder().seed(seed).clock(CLOCK)).build();
    }

    private static GeneratorConfig replayed(GeneratorConfig config) {
        return GenerationRecipe.parse(config.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
    }

    @Nested
    @DisplayName("configuration and recipe")
    class Settings {

        @Test
        @DisplayName("each opt-in is off by default and on when set, and a copy keeps it")
        void offByDefaultAndCopied() {
            GeneratorConfig defaults = GeneratorConfig.defaults();
            assertFalse(defaults.isAdvancingConvenienceStreams());
            assertFalse(defaults.isStrictMathGaussian());
            assertFalse(defaults.isObjectReseedBuiltInRandomizers());

            GeneratorConfig on = seeded(
                    5L,
                    builder -> builder.advancingConvenienceStreams(true)
                            .strictMathGaussian(true)
                            .objectReseedBuiltInRandomizers(true));
            for (GeneratorConfig copy : List.of(
                    on,
                    on.toBuilder().build(),
                    on.forChildStream("child"),
                    ConfigAccess.withSeed(on, 9L),
                    replayed(on))) {
                assertTrue(copy.isAdvancingConvenienceStreams());
                assertTrue(copy.isStrictMathGaussian());
                assertTrue(copy.isObjectReseedBuiltInRandomizers());
            }

            GeneratorConfig off = on.toBuilder()
                    .advancingConvenienceStreams(false)
                    .strictMathGaussian(false)
                    .objectReseedBuiltInRandomizers(false)
                    .build();
            assertFalse(off.isAdvancingConvenienceStreams());
            assertFalse(off.isStrictMathGaussian());
            assertFalse(off.isObjectReseedBuiltInRandomizers());
        }

        @Test
        @DisplayName("a recipe records each opt-in only when it is on, and replays it")
        void recipeRecordsEachOptInWhenOn() {
            Map<String, String> none = seeded(5L, builder -> builder)
                    .getGenerationRecipe()
                    .orElseThrow()
                    .getSettings();
            assertFalse(none.containsKey("advancing-convenience-streams"));
            assertFalse(none.containsKey("gaussian.strict-math"));
            assertFalse(none.containsKey("object.reseed-built-in-randomizers"));

            List<UnaryOperator<GeneratorConfig.Builder>> each = List.of(
                    builder -> builder.advancingConvenienceStreams(true),
                    builder -> builder.strictMathGaussian(true),
                    builder -> builder.objectReseedBuiltInRandomizers(true));
            List<String> keys = List.of(
                    "advancing-convenience-streams", "gaussian.strict-math", "object.reseed-built-in-randomizers");
            for (int i = 0; i < each.size(); i++) {
                GeneratorConfig config = seeded(5L, each.get(i));
                GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();
                assertEquals("true", recipe.getSettings().get(keys.get(i)), keys.get(i));
                for (String other : keys) {
                    assertEquals(other.equals(keys.get(i)), recipe.getSettings().containsKey(other), other);
                }
                assertTrue(recipe.serialize().contains("\nsetting." + keys.get(i) + "=true\n"), keys.get(i));
                GeneratorConfig replay = replayed(config);
                assertEquals(
                        List.of(
                                config.isAdvancingConvenienceStreams(),
                                config.isStrictMathGaussian(),
                                config.isObjectReseedBuiltInRandomizers()),
                        List.of(
                                replay.isAdvancingConvenienceStreams(),
                                replay.isStrictMathGaussian(),
                                replay.isObjectReseedBuiltInRandomizers()),
                        keys.get(i));
            }
        }

        @Test
        @DisplayName("a recipe that names an opt-in as false replays it off, and any other value is rejected")
        void recipeValues() {
            String recorded = seeded(5L, builder -> builder)
                    .getGenerationRecipe()
                    .orElseThrow()
                    .serialize();
            GeneratorConfig off = GenerationRecipe.parse(recorded
                            + "setting.advancing-convenience-streams=false\n"
                            + "setting.gaussian.strict-math=false\n"
                            + "setting.object.reseed-built-in-randomizers=false\n")
                    .toGeneratorConfig();
            assertFalse(off.isAdvancingConvenienceStreams());
            assertFalse(off.isStrictMathGaussian());
            assertFalse(off.isObjectReseedBuiltInRandomizers());

            for (String key : List.of(
                    "advancing-convenience-streams", "gaussian.strict-math", "object.reseed-built-in-randomizers")) {
                GenerationRecipe bad = GenerationRecipe.parse(recorded + "setting." + key + "=yes\n");
                IllegalArgumentException error = assertThrows(IllegalArgumentException.class, bad::toGeneratorConfig);
                assertEquals("Recipe setting '" + key + "' must be true or false", error.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("advancing convenience streams")
    class AdvancingConvenienceStreams {

        private final UnaryOperator<GeneratorConfig.Builder> advancing =
                builder -> builder.advancingConvenienceStreams(true);

        @Test
        @DisplayName("generateCompanyEmail gives the next address on each call, starting from the one it repeated")
        void companyEmailAdvances() {
            GeneratorConfig config = seeded(42L, advancing);
            List<String> values = companyEmails(new EmailGenerator(config), 6);

            assertEquals("aharris@advancedassociatesco.test", values.get(0));
            assertTrue(new HashSet<>(values).size() > 1, values::toString);
            assertEquals(values, companyEmails(new EmailGenerator(config), 6));
            assertEquals(values, companyEmails(new EmailGenerator(replayed(config)), 6));
        }

        @Test
        @DisplayName("a reseeded email generator starts its company addresses again, as a new build does")
        void companyEmailRestartsOnReseed() {
            EmailGenerator reused = new EmailGenerator(seeded(1L, advancing));
            companyEmails(reused, 3);
            Objects.requireNonNull(Reseeding.reseederOf(reused)).reseed(reused, 42L);

            assertEquals(
                    companyEmails(
                            new EmailGenerator(
                                    seeded(1L, advancing).toBuilder().seed(42L).build()),
                            4),
                    companyEmails(reused, 4));
        }

        @Test
        @DisplayName("the middle-name methods draw the next middle name on each call, from one stream")
        void middleNameAdvances() {
            GeneratorConfig config = seeded(42L, advancing);
            List<String> values = middleNames(new FullNameGenerator(config));

            assertEquals("Caroline Hazel Tran", values.get(0));
            assertEquals("Steven Audrey Mitchell", values.get(1));
            assertNotEquals(middleOf(values.get(0)), middleOf(values.get(1)));
            assertEquals(values, middleNames(new FullNameGenerator(config)));
            assertEquals(values, middleNames(new FullNameGenerator(replayed(config))));
        }

        @Test
        @DisplayName("the middle-name methods share their stream with the middle names of generate(NameOptions)")
        void middleNameStreamIsShared() {
            GeneratorConfig config = seeded(42L, advancing);
            FullNameGenerator mixed = new FullNameGenerator(config);
            FullNameGenerator options = new FullNameGenerator(config);
            FullNameGenerator.NameOptions middle =
                    new FullNameGenerator.NameOptions(true, false, false, false, false, null, null);

            mixed.generateWithMiddleName();
            options.generate(middle);

            assertEquals(middleOf(options.generate(middle)), middleOf(mixed.generateWithMiddleName()));
        }

        @Test
        @DisplayName("generatePriceTag gives the next amount on each call, starting from the one it repeated")
        void priceTagAdvances() {
            GeneratorConfig config = seeded(42L, advancing);
            CurrencyGenerator currencies = new CurrencyGenerator(config);
            MoneyGenerator money = new MoneyGenerator(config.forChildStream("money"));
            List<String> expected = new ArrayList<>();
            List<String> actual = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                expected.add(money.generate());
                actual.add(currencies.generatePriceTag());
                expected.add(money.generate(Locale.GERMANY));
                actual.add(currencies.generatePriceTag(Locale.GERMANY));
            }

            assertEquals(expected, actual);
            assertEquals(new MoneyGenerator(config.forChildStream("money")).generate(), actual.get(0));
            assertTrue(new HashSet<>(actual).size() > 2, actual::toString);

            Objects.requireNonNull(Reseeding.reseederOf(currencies)).reseed(currencies, 42L);
            assertEquals(actual.get(0), currencies.generatePriceTag());
        }

        private List<String> companyEmails(EmailGenerator emails, int count) {
            List<String> values = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                values.add(emails.generateCompanyEmail());
            }
            return values;
        }

        private List<String> middleNames(FullNameGenerator names) {
            return List.of(
                    names.generateWithMiddleName(),
                    names.generateWithMiddleName(),
                    names.generateWithMiddleName(Gender.FEMALE),
                    names.generateWithMiddleInitial(),
                    names.generateWithMiddleInitial(),
                    names.generateWithMiddleInitial(Gender.MALE));
        }

        private String middleOf(String name) {
            return name.split(" ")[1];
        }
    }

    @Nested
    @DisplayName("StrictMath Gaussian draws")
    class StrictMathGaussian {

        private final UnaryOperator<GeneratorConfig.Builder> strict = builder -> builder.strictMathGaussian(true);

        @Test
        @DisplayName("a seeded normal distribution draws Box-Muller with StrictMath")
        void gaussianUsesStrictMath() {
            for (long seed = 0; seed < 200; seed++) {
                GeneratorConfig config = seeded(seed, strict);
                assertEquals(
                        strictBoxMuller(seed, 0.0, 1.0, 6),
                        new NormalDistributionGenerator(config).generateList(6),
                        "seed " + seed);
                assertEquals(
                        strictBoxMuller(seed, 100.0, 15.0, 6),
                        new NormalDistributionGenerator(100.0, 15.0, config).generateList(6),
                        "seed " + seed);
            }
        }

        @Test
        @DisplayName("StrictMath gives the same bits on every platform, so its values can be pinned")
        void strictValuesArePinned() {
            // Seeds 10 and 14 are ones whose sin or cos last bit differs between Math and StrictMath on aarch64.
            assertEquals(
                    List.of(
                            Double.doubleToRawLongBits(-0.0388436147345816),
                            Double.doubleToRawLongBits(0.7916654062154486)),
                    bits(new NormalDistributionGenerator(seeded(10L, strict)).generateList(2)));
            assertEquals(
                    List.of(
                            Double.doubleToRawLongBits(0.7299233329370594),
                            Double.doubleToRawLongBits(-0.31054186561011643)),
                    bits(new NormalDistributionGenerator(seeded(14L, strict)).generateList(2)));
            GeneratorConfig config = seeded(10L, strict);
            assertEquals(
                    new NormalDistributionGenerator(config).generateList(6),
                    new NormalDistributionGenerator(replayed(config)).generateList(6));
        }

        private List<Long> bits(List<Double> values) {
            return values.stream().map(Double::doubleToRawLongBits).toList();
        }

        private List<Double> strictBoxMuller(long seed, double mean, double standardDeviation, int count) {
            Random random = new Random(seed);
            List<Double> values = new ArrayList<>();
            while (values.size() < count) {
                double u1 = random.nextDouble();
                double u2 = random.nextDouble();
                while (u1 == 0.0) {
                    u1 = random.nextDouble();
                }
                double magnitude = StrictMath.sqrt(-2.0 * StrictMath.log(u1));
                values.add(mean + magnitude * StrictMath.cos(2.0 * Math.PI * u2) * standardDeviation);
                values.add(mean + magnitude * StrictMath.sin(2.0 * Math.PI * u2) * standardDeviation);
            }
            return values.subList(0, count);
        }
    }
}
