/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.location.StreetAddressGenerator;
import io.github.frikit.krandom.generator.user.EmailGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * From 4.0.0 a recipe is {@code krandom-recipe/v2}: it records the four seeding settings that 4.0.0 turns on, on
 * or off, and replay requires them. A {@code krandom-recipe/v1} recipe, which 3.x wrote, is rejected with the
 * release it names.
 */
@DisplayName("krandom-recipe/v2 and the rejection of v1")
class GenerationRecipeV2Test {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static final List<String> FOUR = List.of(
            "address.german-street-layout",
            "advancing-convenience-streams",
            "gaussian.strict-math",
            "object.reseed-built-in-randomizers");

    /** A recipe as 3.2.0 writes it for {@code de_DE} and seed 42, with the German street layout on. */
    static final String V1_RECIPE_FROM_3_2_0 = """
            format=krandom-recipe
            recipe-version=v1
            library-version=3.2.0
            algorithm=java.util.Random-v1
            seed=42
            locale=de-DE
            clock-instant=2026-07-10T12%3A00%3A00Z
            clock-zone=Z
            profile=custom
            safety-policy=legacy-unclassified
            construction-policy=safe-constructors
            provider-dataset-version=builtin-v1
            setting.address.german-street-layout=true
            setting.banking.safety-policy=DISABLED
            setting.business-tax-identifier.safety-policy=DISABLED
            setting.charset=US-ASCII
            setting.child-stream-policy=INDEPENDENT
            setting.collection.max=10
            setting.collection.min=1
            setting.crypto-address.safety-policy=DISABLED
            setting.email.domain-policy=TEST_SAFE_RESERVED_DOMAINS
            setting.identity-document.safety-policy=DISABLED
            setting.national-id.safety-policy=DISABLED
            setting.network.safety-policy=TEST_SAFE_RESERVED
            setting.object.ignore-errors=false
            setting.object.max-depth=5
            setting.object.null-probability=0.0
            setting.object.optional-empty-probability=0.0
            setting.object.override-default-initialization=false
            setting.object.pool-size=10
            setting.object.requested-unique-fields=-
            setting.object.semantic-mode=RELAXED
            setting.object.unique-fields=email%2Cemailaddress%2Cusername%2Cuserhandle%2Cuuid%2Cguid%2Cid
            setting.object.uniqueness-max-attempts=256
            setting.payment.card-safety-policy=TEST_SAFE_NON_ROUTABLE
            setting.phone-number.safety-policy=TEST_SAFE_WHERE_AVAILABLE
            setting.securities-identifier.safety-policy=DISABLED
            setting.string.max=20
            setting.string.min=5
            """;

    static final String V1_MESSAGE_FROM_3_2_0 = "Unsupported recipe version: v1. kRandom 4.x replays only"
            + " krandom-recipe/v2 recipes: a krandom-recipe/v1 recipe was written by kRandom 3.x or earlier, and its"
            + " seeded values differ from 4.0.0. This recipe records library-version 3.2.0: replay it with kRandom"
            + " 3.2.0.";

    private static GeneratorConfig seeded(UnaryOperator<GeneratorConfig.Builder> settings) {
        return settings.apply(GeneratorConfig.builder()
                        .seed(42L)
                        .locale(Locale.GERMANY)
                        .clock(CLOCK))
                .build();
    }

    @Test
    @DisplayName("a recipe is krandom-recipe/v2 and records the four settings, on by default")
    void writesV2WithTheFourSettings() {
        GenerationRecipe recipe =
                seeded(UnaryOperator.identity()).getGenerationRecipe().orElseThrow();

        assertEquals("v2", GenerationRecipe.RECIPE_VERSION);
        assertEquals("v2", recipe.getRecipeVersion());
        assertTrue(recipe.serialize().startsWith("format=krandom-recipe\nrecipe-version=v2\n"), recipe.serialize());
        for (String key : FOUR) {
            assertEquals("true", recipe.getSettings().get(key), key);
        }
        GenerationRecipe off =
                seeded(DefaultFlipPinsTest.ALL_OFF).getGenerationRecipe().orElseThrow();
        for (String key : FOUR) {
            assertEquals("false", off.getSettings().get(key), key);
        }
    }

    @Test
    @DisplayName("parsing a v1 recipe fails with the release it records, to replay it with")
    void parsingV1Fails() {
        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(V1_RECIPE_FROM_3_2_0));

        assertEquals(V1_MESSAGE_FROM_3_2_0, error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3.0.0", "3.1.0", "3.2.0-SNAPSHOT", "2.6.0", "development"})
    @DisplayName("the v1 message names whatever library-version the recipe records")
    void v1MessageNamesTheRecordedVersion(String version) {
        String recipe = V1_RECIPE_FROM_3_2_0.replace("library-version=3.2.0", "library-version=" + version);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(recipe));

        assertEquals(V1_MESSAGE_FROM_3_2_0.replace("3.2.0", version), error.getMessage());
    }

    @Test
    @DisplayName("a v1 recipe is rejected before its other lines are checked, with or without a library-version")
    void v1IsRejectedFirst() {
        IllegalArgumentException incomplete = assertThrows(
                IllegalArgumentException.class,
                () -> GenerationRecipe.parse("format=krandom-recipe\nrecipe-version=v1\nlibrary-version=3.1.0\n"));
        assertEquals(V1_MESSAGE_FROM_3_2_0.replace("3.2.0", "3.1.0"), incomplete.getMessage());

        IllegalArgumentException unnamed = assertThrows(
                IllegalArgumentException.class,
                () -> GenerationRecipe.parse("format=krandom-recipe\nrecipe-version=v1\nseed=not-a-number\n"));
        assertEquals(
                "Unsupported recipe version: v1. kRandom 4.x replays only krandom-recipe/v2 recipes: a"
                        + " krandom-recipe/v1 recipe was written by kRandom 3.x or earlier, and its seeded values"
                        + " differ from 4.0.0. This recipe records no library-version: replay it with the 3.x release"
                        + " that wrote it.",
                unnamed.getMessage());
        assertEquals(
                unnamed.getMessage(),
                assertThrows(
                                IllegalArgumentException.class,
                                () -> GenerationRecipe.parse(
                                        "format=krandom-recipe\nrecipe-version=v1\nlibrary-version=\n"))
                        .getMessage());
    }

    @Test
    @DisplayName("a v1 recipe built in code is rejected the same way, and an unknown version as before")
    void builderRejectsV1() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> GenerationRecipe.builder()
                        .recipeVersion("v1")
                        .libraryVersion("3.2.0")
                        .seed(1L)
                        .build());
        assertEquals(V1_MESSAGE_FROM_3_2_0, error.getMessage());

        assertEquals(
                "Unsupported recipe version: v99",
                assertThrows(
                                IllegalArgumentException.class,
                                () -> GenerationRecipe.parse(
                                        V1_RECIPE_FROM_3_2_0.replace("recipe-version=v1", "recipe-version=v99")))
                        .getMessage());
    }

    private static String v1BuilderMessageWithoutARelease(String libraryVersion) {
        return "Unsupported recipe version: v1. kRandom 4.x replays only krandom-recipe/v2 recipes: a"
                + " krandom-recipe/v1 recipe was written by kRandom 3.x or earlier, and its seeded values differ"
                + " from 4.0.0. This recipe records library-version " + libraryVersion + ", which is not a 3.x or"
                + " earlier release, and a library-version of 4 or later cannot have written a v1 recipe: replay it"
                + " with the 3.x release that wrote it.";
    }

    private static IllegalArgumentException buildV1(UnaryOperator<GenerationRecipe.Builder> libraryVersion) {
        return assertThrows(
                IllegalArgumentException.class,
                () -> libraryVersion
                        .apply(GenerationRecipe.builder().recipeVersion("v1").seed(1L))
                        .build());
    }

    @Test
    @DisplayName("a v1 recipe built in code without a library-version does not name the running release to replay it")
    void builderV1WithoutLibraryVersionDoesNotNameTheRunningRelease() {
        String running = GenerationRecipe.currentLibraryVersion();

        IllegalArgumentException error = buildV1(UnaryOperator.identity());

        assertEquals(v1BuilderMessageWithoutARelease(running), error.getMessage());
        assertFalse(error.getMessage().contains("replay it with kRandom"), error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"3", "3.2.0", "3.2.0-SNAPSHOT", "0.9.0", "03.1.0"})
    @DisplayName("a v1 recipe built in code with a 3.x or earlier library-version still names it")
    void builderV1NamesA3xLibraryVersion(String version) {
        IllegalArgumentException error = buildV1(builder -> builder.libraryVersion(version));

        assertEquals(V1_MESSAGE_FROM_3_2_0.replace("3.2.0", version), error.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"4", "4.0.0", "4.0.0-SNAPSHOT", "10.1.0", "30.0", "development", "v3.2.0", "-3.2.0"})
    @DisplayName("a v1 recipe built in code with a 4.x or non-numeric library-version is not told to replay with it")
    void builderV1WithoutA3xLibraryVersionGetsTheSafeWording(String version) {
        IllegalArgumentException error = buildV1(builder -> builder.libraryVersion(version));

        assertEquals(v1BuilderMessageWithoutARelease(version), error.getMessage());
    }

    @Test
    @DisplayName("a v2 recipe without one of the four settings is rejected rather than replayed with a default")
    void v2RequiresTheFourSettings() {
        String recipe = seeded(UnaryOperator.identity())
                .getGenerationRecipe()
                .orElseThrow()
                .serialize();
        for (String key : FOUR) {
            GenerationRecipe without = GenerationRecipe.parse(recipe.replace("setting." + key + "=true\n", ""));
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, without::toGeneratorConfig);
            assertEquals("Recipe requires setting: " + key, error.getMessage());

            GenerationRecipe bad = GenerationRecipe.parse(recipe.replace(key + "=true", key + "=on"));
            assertEquals(
                    "Recipe setting '" + key + "' must be true or false",
                    assertThrows(IllegalArgumentException.class, bad::toGeneratorConfig)
                            .getMessage());
        }
    }

    @Test
    @DisplayName("a v2 recipe replays each setting as recorded, so the 3.x values replay as 3.x values")
    void v2ReplaysAsRecorded() {
        GeneratorConfig off = seeded(DefaultFlipPinsTest.ALL_OFF);
        GeneratorConfig replayOff = GenerationRecipe.parse(
                        off.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertFalse(replayOff.isAdvancingConvenienceStreams());
        assertFalse(replayOff.isStrictMathGaussian());
        assertFalse(replayOff.isObjectReseedBuiltInRandomizers());
        assertFalse(replayOff.isGermanStreetLayout());
        assertEquals(new StreetAddressGenerator(off).generate(), new StreetAddressGenerator(replayOff).generate());
        assertEquals("7389 Schloss Anger", new StreetAddressGenerator(replayOff).generate());

        GeneratorConfig on = seeded(UnaryOperator.identity());
        GeneratorConfig replayOn = GenerationRecipe.parse(
                        on.getGenerationRecipe().orElseThrow().serialize())
                .toGeneratorConfig();
        assertTrue(replayOn.isAdvancingConvenienceStreams());
        assertTrue(replayOn.isStrictMathGaussian());
        assertTrue(replayOn.isObjectReseedBuiltInRandomizers());
        assertTrue(replayOn.isGermanStreetLayout());
        assertEquals("Schlossanger 7389", new StreetAddressGenerator(replayOn).generate());
        EmailGenerator emails = new EmailGenerator(replayOn);
        EmailGenerator original = new EmailGenerator(on);
        for (int i = 0; i < 3; i++) {
            assertEquals(original.generateCompanyEmail(), emails.generateCompanyEmail());
        }
        assertEquals(
                on.getGenerationRecipe().orElseThrow().serialize(),
                replayOn.getGenerationRecipe().orElseThrow().serialize());
    }
}
