/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.frikit.krandom.generator.base.ByteGenerator;
import io.github.frikit.krandom.generator.base.NormalDistributionGenerator;
import io.github.frikit.krandom.generator.finance.CurrencyGenerator;
import io.github.frikit.krandom.generator.finance.MoneyGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.object.Randomizer;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import io.github.frikit.krandom.generator.user.FullNameGenerator;
import io.github.frikit.krandom.generator.user.Gender;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded values of the default configuration that the 3.2 seeding opt-ins must leave as they are, recorded on
 * {@code main} before the opt-ins existed: convenience methods that restart their child stream, Gaussian draws
 * with {@link Math}, a {@code @Randomizer} left on its own source, and the recipe of a default configuration.
 */
@DisplayName("with no opt-in, seeded values and recipes stay as 3.1 gave them")
class SeedingDefaultsPinTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig seeded(long seed) {
        return GeneratorConfig.builder().seed(seed).clock(CLOCK).build();
    }

    @Test
    @DisplayName("generateCompanyEmail restarts its stream, so a seeded generator repeats one address")
    void companyEmailRepeats() {
        EmailGenerator emails = new EmailGenerator(seeded(42L));
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            values.add(emails.generateCompanyEmail());
        }
        assertEquals(
                List.of(
                        "aharris@advancedassociatesco.test",
                        "aharris@advancedassociatesco.test",
                        "aharris@advancedassociatesco.test"),
                values);
    }

    @Test
    @DisplayName("the middle-name methods restart their stream, so a seeded generator repeats one middle name")
    void middleNameRepeats() {
        FullNameGenerator names = new FullNameGenerator(seeded(42L));
        List<String> values = List.of(
                names.generateWithMiddleName(),
                names.generateWithMiddleName(),
                names.generateWithMiddleName(Gender.FEMALE),
                names.generateWithMiddleInitial(),
                names.generateWithMiddleInitial(),
                names.generateWithMiddleInitial(Gender.MALE));
        assertEquals(
                List.of(
                        "Caroline Hazel Tran",
                        "Steven Hazel Mitchell",
                        "Olivia Ellie Taylor",
                        "Carter H. Green",
                        "Stephanie H. Reyes",
                        "Hunter D. Bell"),
                values);
    }

    @Test
    @DisplayName("generatePriceTag restarts its stream, so each call gives what a new money generator gives")
    void priceTagRestarts() {
        GeneratorConfig config = seeded(42L);
        CurrencyGenerator currencies = new CurrencyGenerator(config);
        for (int i = 0; i < 3; i++) {
            assertEquals(new MoneyGenerator(config.forChildStream("money")).generate(), currencies.generatePriceTag());
            assertEquals(
                    new MoneyGenerator(config.forChildStream("money")).generate(java.util.Locale.GERMANY),
                    currencies.generatePriceTag(java.util.Locale.GERMANY));
        }
    }

    @Test
    @DisplayName("a seeded normal distribution draws Box-Muller with Math.log, Math.cos and Math.sin")
    void gaussianUsesMath() {
        for (long seed = 0; seed < 200; seed++) {
            NormalDistributionGenerator standard = new NormalDistributionGenerator(seeded(seed));
            NormalDistributionGenerator scaled = new NormalDistributionGenerator(100.0, 15.0, seeded(seed));
            assertEquals(mathBoxMuller(seed, 0.0, 1.0, 6), standard.generateList(6), "seed " + seed);
            assertEquals(mathBoxMuller(seed, 100.0, 15.0, 6), scaled.generateList(6), "seed " + seed);
        }
    }

    /** The transform as 3.1 computes it, with the platform's {@link Math}, over the seed's own {@link Random}. */
    static List<Double> mathBoxMuller(long seed, double mean, double standardDeviation, int count) {
        Random random = new Random(seed);
        List<Double> values = new ArrayList<>();
        while (values.size() < count) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            while (u1 == 0.0) {
                u1 = random.nextDouble();
            }
            double magnitude = Math.sqrt(-2.0 * Math.log(u1));
            values.add(mean + magnitude * Math.cos(2.0 * Math.PI * u2) * standardDeviation);
            values.add(mean + magnitude * Math.sin(2.0 * Math.PI * u2) * standardDeviation);
        }
        return values.subList(0, count);
    }

    public static class WithRandomizers {
        @Randomizer(ByteGenerator.class)
        public byte seedable;

        @Randomizer(UUIDGenerator.class)
        public UUID builtIn;

        public int plain;
        public String text;
    }

    @Test
    @DisplayName("object generation reseeds a Seedable @Randomizer and leaves a built-in that is not Seedable alone")
    void randomizers() {
        List<String> first = new ArrayList<>();
        List<String> second = new ArrayList<>();
        List<UUID> firstBuiltIns = new ArrayList<>();
        List<UUID> secondBuiltIns = new ArrayList<>();
        ObjectGenerator<WithRandomizers> one = new ObjectGenerator<>(WithRandomizers.class, seeded(42L));
        ObjectGenerator<WithRandomizers> two = new ObjectGenerator<>(WithRandomizers.class, seeded(42L));
        for (int i = 0; i < 3; i++) {
            WithRandomizers a = one.generate();
            WithRandomizers b = two.generate();
            first.add(a.seedable + " " + a.plain + " " + a.text);
            second.add(b.seedable + " " + b.plain + " " + b.text);
            firstBuiltIns.add(a.builtIn);
            secondBuiltIns.add(b.builtIn);
        }
        assertEquals(List.of("-31 344561791 iVFiQvpQd", "82 -1919648789 VHPBskAD", "-83 -1030597551 cqShUH"), first);
        assertEquals(first, second);
        // The UUID generator draws from its own unseeded source, so two runs from one seed differ.
        assertNotEquals(firstBuiltIns, secondBuiltIns);
    }

    @Test
    @DisplayName("the recipe of a default seeded configuration records exactly the 3.1 settings")
    void recipeSettings() {
        GenerationRecipe recipe = seeded(42L).getGenerationRecipe().orElseThrow();
        Map<String, String> settings = new TreeMap<>(recipe.getSettings());
        assertEquals(
                "{banking.safety-policy=DISABLED, business-tax-identifier.safety-policy=DISABLED, charset=US-ASCII,"
                        + " child-stream-policy=INDEPENDENT, collection.max=10, collection.min=1,"
                        + " crypto-address.safety-policy=DISABLED, email.domain-policy=TEST_SAFE_RESERVED_DOMAINS,"
                        + " identity-document.safety-policy=DISABLED, national-id.safety-policy=DISABLED,"
                        + " network.safety-policy=TEST_SAFE_RESERVED, object.ignore-errors=false, object.max-depth=5,"
                        + " object.null-probability=0.0, object.optional-empty-probability=0.0,"
                        + " object.override-default-initialization=false, object.pool-size=10,"
                        + " object.requested-unique-fields=-, object.semantic-mode=RELAXED,"
                        + " object.unique-fields=email,emailaddress,username,userhandle,uuid,guid,id,"
                        + " object.uniqueness-max-attempts=256, payment.card-safety-policy=TEST_SAFE_NON_ROUTABLE,"
                        + " phone-number.safety-policy=TEST_SAFE_WHERE_AVAILABLE,"
                        + " securities-identifier.safety-policy=DISABLED, string.max=20, string.min=5}",
                settings.toString());
        String serialized = recipe.serialize();
        assertEquals(
                String.join(
                        "",
                        "format=krandom-recipe\n",
                        "recipe-version=v1\n",
                        "library-version=<version>\n",
                        "algorithm=java.util.Random-v1\n",
                        "seed=42\n",
                        "locale=en-US\n",
                        "clock-instant=2026-07-10T12%3A00%3A00Z\n",
                        "clock-zone=Z\n",
                        "profile=custom\n",
                        "safety-policy=legacy-unclassified\n",
                        "construction-policy=safe-constructors\n",
                        "provider-dataset-version=builtin-v1\n",
                        "setting.banking.safety-policy=DISABLED\n",
                        "setting.business-tax-identifier.safety-policy=DISABLED\n",
                        "setting.charset=US-ASCII\n",
                        "setting.child-stream-policy=INDEPENDENT\n",
                        "setting.collection.max=10\n",
                        "setting.collection.min=1\n",
                        "setting.crypto-address.safety-policy=DISABLED\n",
                        "setting.email.domain-policy=TEST_SAFE_RESERVED_DOMAINS\n",
                        "setting.identity-document.safety-policy=DISABLED\n",
                        "setting.national-id.safety-policy=DISABLED\n",
                        "setting.network.safety-policy=TEST_SAFE_RESERVED\n",
                        "setting.object.ignore-errors=false\n",
                        "setting.object.max-depth=5\n",
                        "setting.object.null-probability=0.0\n",
                        "setting.object.optional-empty-probability=0.0\n",
                        "setting.object.override-default-initialization=false\n",
                        "setting.object.pool-size=10\n",
                        "setting.object.requested-unique-fields=-\n",
                        "setting.object.semantic-mode=RELAXED\n",
                        "setting.object.unique-fields=email%2Cemailaddress%2Cusername%2Cuserhandle%2Cuuid%2Cguid%2Cid\n",
                        "setting.object.uniqueness-max-attempts=256\n",
                        "setting.payment.card-safety-policy=TEST_SAFE_NON_ROUTABLE\n",
                        "setting.phone-number.safety-policy=TEST_SAFE_WHERE_AVAILABLE\n",
                        "setting.securities-identifier.safety-policy=DISABLED\n",
                        "setting.string.max=20\n",
                        "setting.string.min=5\n"),
                serialized.replace(recipe.getLibraryVersion(), "<version>"));
    }
}
