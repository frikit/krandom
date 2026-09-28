/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

import io.github.frikit.krandom.generator.object.ObjectConstructionPolicy;
import io.github.frikit.krandom.generator.base.DigitGenerator;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("GenerationRecipe")
class GenerationRecipeTest {

    private static final Instant CLOCK_INSTANT = Instant.parse("2026-07-10T12:34:56Z");
    private static final ZoneId CLOCK_ZONE = ZoneId.of("Europe/London");
    private static final List<String> REQUIRED_POLICY_SETTINGS = List.of(
        "payment.card-safety-policy",
        "banking.safety-policy",
        "business-tax-identifier.safety-policy",
        "crypto-address.safety-policy",
        "securities-identifier.safety-policy",
        "phone-number.safety-policy",
        "national-id.safety-policy",
        "identity-document.safety-policy",
        "email.domain-policy");

    @Test
    @DisplayName("serializes a stable human-readable recipe and parses it exactly")
    void stableSerializationRoundTrip() {
        GenerationRecipe recipe = GenerationRecipe.builder()
                                                    .libraryVersion("2.0.0-rc1")
                                                    .recipeVersion("v1")
                                                    .algorithm("java.util.Random-v1")
                                                    .seed(-42L)
                                                    .locale(Locale.CANADA_FRENCH)
                                                    .clock(CLOCK_INSTANT, CLOCK_ZONE)
                                                    .profile("realistic")
                                                    .safetyPolicy("legacy-unclassified")
                                                    .constructionPolicy(ObjectConstructionPolicy.SAFE_CONSTRUCTORS)
                                                    .providerDatasetVersion("builtin-v1")
                                                    .setting("string.max", "16")
                                                    .setting("string.min", "4")
                                                    .build();

        String serialized = recipe.serialize();

        assertEquals("""
            format=krandom-recipe
            recipe-version=v1
            library-version=2.0.0-rc1
            algorithm=java.util.Random-v1
            seed=-42
            locale=fr-CA
            clock-instant=2026-07-10T12%3A34%3A56Z
            clock-zone=Europe%2FLondon
            profile=realistic
            safety-policy=legacy-unclassified
            construction-policy=safe-constructors
            provider-dataset-version=builtin-v1
            setting.string.max=16
            setting.string.min=4
            """, serialized);
        assertEquals(recipe, GenerationRecipe.parse(serialized));
        assertEquals(recipe.hashCode(), GenerationRecipe.parse(serialized).hashCode());
        assertEquals(serialized, recipe.toString());
        assertNotEquals(recipe, "not a recipe");
        assertNotEquals(recipe, baselineRecipe());
    }

    @Test
    @DisplayName("exposes every recipe field and preserves textual seed metadata")
    void exposesRecipeFields() {
        String seedText = "portable seed";
        GenerationRecipe recipe = GenerationRecipe.builder()
                                                    .libraryVersion("2.0.0")
                                                    .seed(GeneratorConfig.deriveSeed(seedText))
                                                    .seedText(seedText)
                                                    .locale(Locale.GERMANY)
                                                    .clock(CLOCK_INSTANT, CLOCK_ZONE)
                                                    .profile("strict")
                                                    .safetyPolicy("test-safe")
                                                    .constructionPolicy(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS)
                                                    .providerDatasetVersion("provider-v2")
                                                    .setting("object.date-min", "2020-01-01")
                                                    .build();

        assertEquals("2.0.0", recipe.getLibraryVersion());
        assertEquals(GenerationRecipe.RECIPE_VERSION, recipe.getRecipeVersion());
        assertEquals(GenerationRecipe.ALGORITHM, recipe.getAlgorithm());
        assertEquals(GeneratorConfig.deriveSeed(seedText), recipe.getSeed());
        assertEquals(seedText, recipe.getSeedText());
        assertEquals(Locale.GERMANY, recipe.getLocale());
        assertEquals(CLOCK_INSTANT, recipe.getClockInstant());
        assertEquals(CLOCK_ZONE, recipe.getClockZone());
        assertEquals("strict", recipe.getProfile());
        assertEquals("test-safe", recipe.getSafetyPolicy());
        assertEquals(ObjectConstructionPolicy.UNSAFE_CONSTRUCTOR_BYPASS, recipe.getConstructionPolicy());
        assertEquals("provider-v2", recipe.getProviderDatasetVersion());
        assertEquals(Map.of("object.date-min", "2020-01-01"), recipe.getSettings());
        assertEquals(recipe, GenerationRecipe.parse(recipe.serialize()));
        assertFalse(recipe.serializeForDiagnostics().contains(seedText));
        assertFalse(recipe.serializeForDiagnostics().contains("seed-text="));
    }

    @Test
    @DisplayName("replays the configured payment card safety policy")
    void replaysPaymentCardSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.CHECKSUM_VALID)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("CHECKSUM_VALID", recipe.getSettings().get("payment.card-safety-policy"));
        assertEquals(PaymentCardSafetyPolicy.CHECKSUM_VALID,
                     recipe.toGeneratorConfig().getPaymentCardSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured Stripe sandbox payment card policy")
    void replaysStripeSandboxPaymentCardSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.STRIPE_SANDBOX)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("STRIPE_SANDBOX", recipe.getSettings().get("payment.card-safety-policy"));
        assertEquals(PaymentCardSafetyPolicy.STRIPE_SANDBOX,
                     recipe.toGeneratorConfig().getPaymentCardSafetyPolicy());
    }

    static Stream<String> requiredPolicySettings() {
        return REQUIRED_POLICY_SETTINGS.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("requiredPolicySettings")
    @DisplayName("replay requires every safety and email policy setting")
    void replayRequiresPolicySetting(String setting) {
        String complete = GeneratorConfig.builder().seed(42L).build().getGenerationRecipe().orElseThrow().serialize();
        String missing = complete.lines()
                                 .filter(line -> !line.startsWith("setting." + setting + "="))
                                 .collect(Collectors.joining("\n", "", "\n"));
        GenerationRecipe recipe = GenerationRecipe.parse(missing);

        assertNotEquals(complete, missing, "the serializer must write " + setting);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, recipe::toGeneratorConfig);
        assertEquals("Recipe requires setting: " + setting, error.getMessage());
    }

    @Test
    @DisplayName("a hand-built recipe with only the required policy settings replays configuration defaults")
    void policyCompleteRecipeReplaysDefaults() {
        GeneratorConfig defaults = GeneratorConfig.builder().seed(7L).build();
        GeneratorConfig replay = policyComplete(7L).build().toGeneratorConfig();

        assertEquals(defaults.getObjectSemanticMode(), replay.getObjectSemanticMode());
        assertEquals(defaults.getObjectNullProbability(), replay.getObjectNullProbability());
        assertEquals(defaults.getObjectOptionalEmptyProbability(), replay.getObjectOptionalEmptyProbability());
        assertEquals(defaults.getObjectUniqueFieldNames(), replay.getObjectUniqueFieldNames());
        assertEquals(defaults.getObjectDateMin(), replay.getObjectDateMin());
        assertEquals(defaults.getPaymentCardSafetyPolicy(), replay.getPaymentCardSafetyPolicy());
        assertEquals(defaults.getEmailDomainPolicy(), replay.getEmailDomainPolicy());
    }

    @Test
    @DisplayName("replays the configured phone number safety policy")
    void replaysPhoneNumberSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .phoneNumberSafetyPolicy(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED", recipe.getSettings().get("phone-number.safety-policy"));
        assertEquals(PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getPhoneNumberSafetyPolicy());
    }

    @Test
    @DisplayName("the default recipe serializes the unique fields in declaration order on every JVM")
    void defaultRecipeSerializesUniqueFieldsInDeclarationOrder() {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals(java.util.List.of("email", "emailaddress", "username", "userhandle", "uuid", "guid", "id"),
                     java.util.List.copyOf(config.getObjectUniqueFieldNames()));
        assertEquals("email,emailaddress,username,userhandle,uuid,guid,id",
                     recipe.getSettings().get("object.unique-fields"));
        assertTrue(recipe.serialize().contains(
            "setting.object.unique-fields=email%2Cemailaddress%2Cusername%2Cuserhandle%2Cuuid%2Cguid%2Cid\n"));
    }

    @Test
    @DisplayName("records and replays the email domain policy")
    void replaysEmailDomainPolicy() {
        GenerationRecipe defaults = GeneratorConfig.builder().seed(42L).build().getGenerationRecipe().orElseThrow();
        GenerationRecipe realistic = GeneratorConfig.builder()
                                                    .seed(42L)
                                                    .emailDomainPolicy(EmailDomainPolicy.REALISTIC_UNCLASSIFIED)
                                                    .build()
                                                    .getGenerationRecipe()
                                                    .orElseThrow();

        assertEquals("TEST_SAFE_RESERVED_DOMAINS", defaults.getSettings().get("email.domain-policy"));
        assertEquals("REALISTIC_UNCLASSIFIED", realistic.getSettings().get("email.domain-policy"));
        assertEquals(EmailDomainPolicy.TEST_SAFE_RESERVED_DOMAINS,
                     GenerationRecipe.parse(defaults.serialize()).toGeneratorConfig().getEmailDomainPolicy());
        assertEquals(EmailDomainPolicy.REALISTIC_UNCLASSIFIED,
                     GenerationRecipe.parse(realistic.serialize()).toGeneratorConfig().getEmailDomainPolicy());
    }

    @Test
    @DisplayName("replays the configured national-ID safety policy")
    void replaysNationalIdSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .nationalIdSafetyPolicy(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED", recipe.getSettings().get("national-id.safety-policy"));
        assertEquals(NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getNationalIdSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured identity-document safety policy")
    void replaysIdentityDocumentSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .identityDocumentSafetyPolicy(
                                                    IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED", recipe.getSettings().get("identity-document.safety-policy"));
        assertEquals(IdentityDocumentSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getIdentityDocumentSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured banking safety policy")
    void replaysBankingSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED", recipe.getSettings().get("banking.safety-policy"));
        assertEquals(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getBankingSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured business tax-identifier safety policy")
    void replaysBusinessTaxIdentifierSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .businessTaxIdentifierSafetyPolicy(
                                                    BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED",
                     recipe.getSettings().get("business-tax-identifier.safety-policy"));
        assertEquals(BusinessTaxIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getBusinessTaxIdentifierSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured crypto-address safety policy")
    void replaysCryptoAddressSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .cryptoAddressSafetyPolicy(
                                                    CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED", recipe.getSettings().get("crypto-address.safety-policy"));
        assertEquals(CryptoAddressSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getCryptoAddressSafetyPolicy());
    }

    @Test
    @DisplayName("replays the configured securities-identifier safety policy")
    void replaysSecuritiesIdentifierSafetyPolicy() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .seed(42L)
                                                .securitiesIdentifierSafetyPolicy(
                                                    SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                .build();

        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals("REALISTIC_UNCLASSIFIED",
                     recipe.getSettings().get("securities-identifier.safety-policy"));
        assertEquals(SecuritiesIdentifierSafetyPolicy.REALISTIC_UNCLASSIFIED,
                     recipe.toGeneratorConfig().getSecuritiesIdentifierSafetyPolicy());
    }

    @Test
    @DisplayName("uses manifest metadata when available and a property fallback otherwise")
    void resolvesRuntimeLibraryVersion() {
        String original = System.getProperty("krandom.version");
        try {
            System.setProperty("krandom.version", "test-runtime");
            assertEquals("test-runtime", GenerationRecipe.currentLibraryVersion(null));
            assertEquals("2.0.0", GenerationRecipe.currentLibraryVersion("2.0.0"));
            assertFalse(GenerationRecipe.currentLibraryVersion().isBlank());
        } finally {
            if (original == null) {
                System.clearProperty("krandom.version");
            } else {
                System.setProperty("krandom.version", original);
            }
        }
    }

    @Test
    @DisplayName("rejects malformed, duplicate, incomplete, and unsupported recipes")
    void rejectsInvalidRecipes() {
        assertThrows(IllegalArgumentException.class, () -> GenerationRecipe.parse(""));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse("format=wrong\nrecipe-version=v1\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse("format=krandom-recipe\nrecipe-version=v1\nformat=krandom-recipe\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse("format=krandom-recipe\nrecipe-version=v99\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse("format=krandom-recipe\nrecipe-version=v1\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).profile(" ").build());
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).setting(" ", "value"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).setting("1invalid", "value"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).seedText("different").build());
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).providerDatasetVersion("line\nbreak"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).recipeVersion("v99").build());
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.builder().seed(1L).profile("value\r").build());
    }

    @Test
    @DisplayName("rejects invalid values for every parsed recipe boundary")
    void rejectsInvalidRecipeValues() {
        String serialized = baselineRecipe().serialize();

        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse("missing-separator"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("recipe-version=v1\n", "recipe-version=v1\n\n")));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("format=krandom-recipe", "1format=krandom-recipe")));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("seed=7", "seed=not-a-number")));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("algorithm=java.util.Random-v1", "algorithm=other")));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("construction-policy=safe-constructors",
                                                                       "construction-policy=other")));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized + "unknown=value\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized + "setting.=value\n"));
        assertThrows(IllegalArgumentException.class,
                     () -> GenerationRecipe.parse(serialized.replace("library-version=2.0.0", "library-version=")));
    }

    @Test
    @DisplayName("named child streams are stable and independent")
    void namedChildStreamsAreStableAndIndependent() {
        GenerationRecipe recipe = GenerationRecipe.builder().seed(99L).build();

        assertEquals(recipe.childRandom("object:example.User.email").nextLong(),
                     recipe.childRandom("object:example.User.email").nextLong());
        assertNotEquals(recipe.childRandom("object:example.User.email").nextLong(),
                        recipe.childRandom("object:example.User.name").nextLong());
        assertThrows(IllegalArgumentException.class, () -> recipe.childRandom(" "));
    }

    @Test
    @DisplayName("a seeded portable config replays every represented setting with a fixed clock")
    void replaysPortableConfig() {
        GeneratorConfig original = GeneratorConfig.builder()
                                                   .seed("recipe fixture")
                                                   .locale(Locale.JAPAN)
                                                   .clock(Clock.fixed(CLOCK_INSTANT, CLOCK_ZONE))
                                                   .generationProfile("realistic")
                                                   .safetyPolicy("legacy-unclassified")
                                                   .providerDatasetVersion("builtin-v1")
                                                   .stringLength(4, 16)
                                                   .collectionSize(2, 6)
                                                   .objectMaxDepth(3)
                                                   .objectPoolSize(2)
                                                   .objectOverrideDefaultInitialization(true)
                                                   .objectConstructionPolicy(ObjectConstructionPolicy.SAFE_CONSTRUCTORS)
                                                   .objectIgnoreErrors(true)
                                                   .objectNullProbability(0.25)
                                                   .objectOptionalEmptyProbability(0.5)
                                                   .objectUniqueFields("email", "accountId")
                                                   .objectUniquenessMaxAttempts(7)
                                                   .build();

        GenerationRecipe recipe = original.getGenerationRecipe().orElseThrow();
        GeneratorConfig replay = recipe.toGeneratorConfig();

        assertEquals(original.getSeed(), replay.getSeed());
        assertEquals(original.getStringSeed(), replay.getStringSeed());
        assertEquals(original.getLocale(), replay.getLocale());
        assertEquals(CLOCK_INSTANT, replay.getClock().instant());
        assertEquals(CLOCK_ZONE, replay.getClock().getZone());
        assertEquals(original.getGenerationProfile(), replay.getGenerationProfile());
        assertEquals(original.getSafetyPolicy(), replay.getSafetyPolicy());
        assertEquals(original.getProviderDatasetVersion(), replay.getProviderDatasetVersion());
        assertEquals(original.getMinStringLength(), replay.getMinStringLength());
        assertEquals(original.getMaxStringLength(), replay.getMaxStringLength());
        assertEquals(original.getMinCollectionSize(), replay.getMinCollectionSize());
        assertEquals(original.getMaxCollectionSize(), replay.getMaxCollectionSize());
        assertEquals(original.getObjectMaxDepth(), replay.getObjectMaxDepth());
        assertEquals(original.getObjectPoolSize(), replay.getObjectPoolSize());
        assertEquals(original.isObjectOverrideDefaultInitialization(), replay.isObjectOverrideDefaultInitialization());
        assertEquals(original.getObjectConstructionPolicy(), replay.getObjectConstructionPolicy());
        assertEquals(original.isObjectIgnoreErrors(), replay.isObjectIgnoreErrors());
        assertEquals(original.getObjectNullProbability(), replay.getObjectNullProbability());
        assertEquals(original.getObjectOptionalEmptyProbability(), replay.getObjectOptionalEmptyProbability());
        assertEquals(original.getObjectUniqueFieldNames(), replay.getObjectUniqueFieldNames());
        assertEquals(original.getObjectUniquenessMaxAttempts(), replay.getObjectUniquenessMaxAttempts());
    }

    @Test
    @DisplayName("a recipe replays a representative scalar generator sequence")
    void recipeReplaysScalarGenerator() {
        GeneratorConfig config = GeneratorConfig.builder().seed(101L).build();
        GenerationRecipe recipe = config.getGenerationRecipe().orElseThrow();

        assertEquals(new DigitGenerator(config).generateList(12),
                     new DigitGenerator(recipe.toGeneratorConfig()).generateList(12));
    }

    @Test
    @DisplayName("locale clock provider data and safety policy change portable recipes")
    void environmentMetadataChangesPortableRecipe() {
        GeneratorConfig.Builder base = GeneratorConfig.builder()
                                                       .seed(101L)
                                                       .locale(Locale.US)
                                                       .clock(Clock.fixed(CLOCK_INSTANT, CLOCK_ZONE))
                                                       .safetyPolicy("test-safe")
                                                       .providerDatasetVersion("builtin-v1");
        GenerationRecipe recipe = base.build().getGenerationRecipe().orElseThrow();

        assertNotEquals(recipe, base.locale(Locale.JAPAN).build().getGenerationRecipe().orElseThrow());
        assertNotEquals(recipe,
                        base.locale(Locale.US)
                            .clock(Clock.fixed(CLOCK_INSTANT.plusSeconds(1), CLOCK_ZONE))
                            .build()
                            .getGenerationRecipe()
                            .orElseThrow());
        assertNotEquals(recipe,
                        base.clock(Clock.fixed(CLOCK_INSTANT, CLOCK_ZONE))
                            .providerDatasetVersion("builtin-v2")
                            .build()
                            .getGenerationRecipe()
                            .orElseThrow());
        assertNotEquals(recipe,
                        base.providerDatasetVersion("builtin-v1")
                            .safetyPolicy("checksum-valid")
                            .build()
                            .getGenerationRecipe()
                            .orElseThrow());
    }

    @Test
    @DisplayName("replays a date range and rejects incomplete or unsupported settings")
    void handlesRecipeSettingsBoundaries() {
        GenerationRecipe dated = GeneratorConfig.builder()
                                                 .seed(7L)
                                                 .objectDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31))
                                                 .build()
                                                 .getGenerationRecipe()
                                                 .orElseThrow();

        assertEquals(LocalDate.of(2020, 1, 1), dated.toGeneratorConfig().getObjectDateMin());
        assertEquals(LocalDate.of(2020, 12, 31), dated.toGeneratorConfig().getObjectDateMax());
        assertRejected("Unsupported recipe setting: unknown", policyComplete(7L).setting("unknown", "value"));
        assertRejected("Recipe requires setting: string.max", policyComplete(7L).setting("string.min", "4"));
        assertRejected("Recipe requires setting: string.min", policyComplete(7L).setting("string.max", "4"));
        assertRejected("Recipe requires setting: collection.min", policyComplete(7L).setting("collection.max", "4"));
        assertRejected("Recipe requires setting: object.date-max",
                       policyComplete(7L).setting("object.date-min", "2020-01-01"));
        assertRejected("Recipe requires setting: object.date-min",
                       policyComplete(7L).setting("object.date-max", "2020-12-31"));
        assertRejected("Recipe setting 'object.override-default-initialization' must be true or false",
                       policyComplete(7L).setting("object.override-default-initialization", "yes"));
        assertThrows(IllegalArgumentException.class,
                     () -> policyComplete(7L).setting("object.semantic-mode", "UNKNOWN").build().toGeneratorConfig());
    }

    private static void assertRejected(String message, GenerationRecipe.Builder recipe) {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                                                      () -> recipe.build().toGeneratorConfig());
        assertEquals(message, error.getMessage());
    }

    /** A hand-built recipe carrying only the settings that replay requires, at their default values. */
    private static GenerationRecipe.Builder policyComplete(long seed) {
        Map<String, String> defaults =
            GeneratorConfig.builder().seed(seed).build().getGenerationRecipe().orElseThrow().getSettings();
        GenerationRecipe.Builder builder = GenerationRecipe.builder().seed(seed);
        for (String setting : REQUIRED_POLICY_SETTINGS) {
            builder.setting(setting, defaults.get(setting));
        }
        return builder;
    }

    @Test
    @DisplayName("a config with an unseeded or caller-owned source has no portable recipe")
    void nonPortableRandomSourcesHaveNoRecipe() {
        assertTrue(GeneratorConfig.defaults().getGenerationRecipe().isEmpty());
        assertTrue(GeneratorConfig.builder().random(new Random(1L)).build().getGenerationRecipe().isEmpty());
        assertTrue(GeneratorConfig.builder().randomFactory(Random::new).build().getGenerationRecipe().isEmpty());
        assertTrue(GeneratorConfig.builder().secureRandom().build().getGenerationRecipe().isEmpty());
        assertFalse(GeneratorConfig.builder().seed(1L).build().getGenerationRecipe().isEmpty());
        assertFalse(GeneratorConfig.builder().random(new SecureRandom()).build().getGenerationRecipe().isPresent());
    }

    @Test
    @DisplayName("does not claim replay support for custom object or registry state")
    void customGenerationStateHasNoRecipe() {
        assertEquals(Optional.empty(), GeneratorConfig.builder().seed(1L)
                                                       .registryContext(DataRegistryContext.builder().isolated().build())
                                                       .build()
                                                       .getGenerationRecipe());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectSemanticRegistry(
                                      io.github.frikit.krandom.generator.object.SemanticFieldRegistry.defaults()
                                                                                   .toBuilder()
                                                                                   .alias("email", "alternate")
                                                                                   .build())
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(String.class, () -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectExcludeField("password")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectSubtype(Runnable.class, RecipeRunnable.class)
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(RecipeFixture.class, "name", () -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(String.class, context -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(RecipeFixture.class, "name", context -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(field -> true, () -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectOverride(field -> true, context -> "value")
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertTrue(GeneratorConfig.builder().seed(1L)
                                  .objectExcludeType(type -> true)
                                  .build()
                                  .getGenerationRecipe()
                                  .isEmpty());
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().generationProfile("line\nbreak"));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().safetyPolicy("line\rbreak"));
        assertThrows(IllegalArgumentException.class,
                     () -> GeneratorConfig.builder().providerDatasetVersion(" "));
    }

    private static GenerationRecipe baselineRecipe() {
        return GenerationRecipe.builder().libraryVersion("2.0.0").seed(7L).build();
    }

    static final class RecipeRunnable implements Runnable {

        @Override
        public void run() {
            // Fixture type only.
        }
    }

    static final class RecipeFixture {

        String name;
    }
}
