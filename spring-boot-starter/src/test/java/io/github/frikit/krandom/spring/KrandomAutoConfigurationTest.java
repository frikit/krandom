/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KrandomAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class));

    @Test
    @DisplayName("default auto-configuration registers all three beans")
    void defaultBeans() {
        runner.run(context -> {
            assertNotNull(context.getBean(GeneratorConfig.class));
            assertNotNull(context.getBean(ProviderHub.class));
            assertNotNull(context.getBean(KrandomObjectFakerFactory.class));
        });
    }

    @Test
    @DisplayName("seed property produces deterministic config")
    void seedProperty() {
        runner.withPropertyValues("krandom.seed=42")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  // Two randoms from the same seed should produce the same first value
                  long first = config.createRandom().nextLong();
                  GeneratorConfig config2 = GeneratorConfig.builder().seed(42L).build();
                  long second = config2.createRandom().nextLong();
                  assertEquals(first, second);
              });
    }

    @Test
    @DisplayName("locale property is propagated to GeneratorConfig")
    void localeProperty() {
        runner.withPropertyValues("krandom.locale=de_DE")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals("de", config.getLocale().getLanguage());
                  assertEquals("DE", config.getLocale().getCountry());
              });
    }

    @Test
    @DisplayName("BCP 47 hyphenated locale tag is propagated to GeneratorConfig")
    void hyphenatedLocaleProperty() {
        runner.withPropertyValues("krandom.locale=en-US")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(Locale.US, config.getLocale());
              });
    }

    @Test
    @DisplayName("object-max-depth property is propagated")
    void objectMaxDepthProperty() {
        runner.withPropertyValues("krandom.object-max-depth=3")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(3, config.getObjectMaxDepth());
              });
    }

    @Test
    @DisplayName("string length bounds can be configured independently")
    void stringLengthBoundsCanBeConfiguredIndependently() {
        GeneratorConfig defaults = GeneratorConfig.defaults();

        runner.withPropertyValues("krandom.max-string-length=17")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(defaults.getMinStringLength(), config.getMinStringLength());
                  assertEquals(17, config.getMaxStringLength());
              });

        runner.withPropertyValues("krandom.min-string-length=7")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(7, config.getMinStringLength());
                  assertEquals(defaults.getMaxStringLength(), config.getMaxStringLength());
              });
    }

    @Test
    @DisplayName("collection size bounds can be configured independently")
    void collectionSizeBoundsCanBeConfiguredIndependently() {
        GeneratorConfig defaults = GeneratorConfig.defaults();

        runner.withPropertyValues("krandom.max-collection-size=4")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(defaults.getMinCollectionSize(), config.getMinCollectionSize());
                  assertEquals(4, config.getMaxCollectionSize());
              });

        runner.withPropertyValues("krandom.min-collection-size=3")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(3, config.getMinCollectionSize());
                  assertEquals(defaults.getMaxCollectionSize(), config.getMaxCollectionSize());
              });
    }

    @Test
    @DisplayName("string and collection bounds are propagated when both sides are configured")
    void completeBoundsArePropagated() {
        runner.withPropertyValues("krandom.min-string-length=2",
                                  "krandom.max-string-length=9",
                                  "krandom.min-collection-size=0",
                                  "krandom.max-collection-size=3")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(2, config.getMinStringLength());
                  assertEquals(9, config.getMaxStringLength());
                  assertEquals(0, config.getMinCollectionSize());
                  assertEquals(3, config.getMaxCollectionSize());
              });
    }

    @Test
    @DisplayName("factory produces independent ObjectFaker and ObjectGenerator instances")
    void factoryProducesInstances() {
        runner.run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
            var faker1 = factory.faker(SampleUser.class);
            var faker2 = factory.faker(SampleUser.class);
            assertNotSame(faker1, faker2);

            var gen1 = factory.generator(SampleUser.class);
            var gen2 = factory.generator(SampleUser.class);
            assertNotSame(gen1, gen2);

            // Verify generation works
            SampleUser user = gen1.generate();
            assertNotNull(user);
        });
    }

    @Test
    @DisplayName("user-provided GeneratorConfig bean overrides auto-configured one")
    void userOverridesConfig() {
        runner.withUserConfiguration(CustomConfigConfiguration.class)
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  // Verify the custom bean is the one injected (same instance)
                  assertSame(config, context.getBean("customConfig"));
                  // Only one GeneratorConfig bean should exist
                  assertEquals(1, context.getBeansOfType(GeneratorConfig.class).size());
              });
    }

    @Test
    @DisplayName("language-only locale tag is supported")
    void languageOnlyLocale() {
        runner.withPropertyValues("krandom.locale=de")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals("de", config.getLocale().getLanguage());
              });
    }

    @Test
    @DisplayName("blank locale property keeps the default locale")
    void blankLocalePropertyKeepsDefault() {
        runner.withPropertyValues("krandom.locale=")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(Locale.US, config.getLocale());
              });
    }

    @Test
    @DisplayName("invalid locale property fails fast")
    void invalidLocalePropertyFailsFast() {
        runner.withPropertyValues("krandom.locale=en--US")
              .run(context -> {
                  Throwable failure = context.getStartupFailure();
                  assertNotNull(failure);
                  assertTrue(hasMessage(failure, "Invalid krandom.locale"));
              });
    }

    @Test
    @DisplayName("generated configuration metadata covers all bindable properties and core defaults")
    void generatedMetadataMatchesCoreDefaults() throws IOException {
        Map<String, JsonNode> metadata = generatedMetadata();
        GeneratorConfig defaults = GeneratorConfig.defaults();

        assertEquals(Set.of(
                         "krandom.seed",
                         "krandom.locale",
                         "krandom.object-max-depth",
                         "krandom.object-null-probability",
                         "krandom.min-string-length",
                         "krandom.max-string-length",
                         "krandom.min-collection-size",
                         "krandom.max-collection-size",
                         "krandom.recipe",
                         "krandom.clock",
                         "krandom.clock-zone",
                         "krandom.payment-card-safety-policy",
                         "krandom.phone-number-safety-policy",
                         "krandom.national-id-safety-policy",
                         "krandom.banking-safety-policy",
                         "krandom.securities-identifier-safety-policy",
                         "krandom.crypto-address-safety-policy",
                         "krandom.business-tax-identifier-safety-policy",
                         "krandom.identity-document-safety-policy",
                         "krandom.object-construction-policy"),
                     metadata.keySet());
        assertTrue(metadata.values().stream()
                           .allMatch(property -> !property.path("description").asText().isBlank()));

        assertEquals(defaults.getLocale().toLanguageTag(), metadata.get("krandom.locale").path("defaultValue").asText());
        assertEquals(defaults.getObjectMaxDepth(), metadata.get("krandom.object-max-depth").path("defaultValue").asInt());
        assertEquals(defaults.getObjectNullProbability(),
                     metadata.get("krandom.object-null-probability").path("defaultValue").asDouble());
        assertEquals(defaults.getMinStringLength(), metadata.get("krandom.min-string-length").path("defaultValue").asInt());
        assertEquals(defaults.getMaxStringLength(), metadata.get("krandom.max-string-length").path("defaultValue").asInt());
        assertEquals(defaults.getMinCollectionSize(), metadata.get("krandom.min-collection-size").path("defaultValue").asInt());
        assertEquals(defaults.getMaxCollectionSize(), metadata.get("krandom.max-collection-size").path("defaultValue").asInt());
        assertEquals(defaults.getPaymentCardSafetyPolicy().name(),
                     metadata.get("krandom.payment-card-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getPhoneNumberSafetyPolicy().name(),
                     metadata.get("krandom.phone-number-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getNationalIdSafetyPolicy().name(),
                     metadata.get("krandom.national-id-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getBankingSafetyPolicy().name(),
                     metadata.get("krandom.banking-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getSecuritiesIdentifierSafetyPolicy().name(),
                     metadata.get("krandom.securities-identifier-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getCryptoAddressSafetyPolicy().name(),
                     metadata.get("krandom.crypto-address-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getBusinessTaxIdentifierSafetyPolicy().name(),
                     metadata.get("krandom.business-tax-identifier-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getIdentityDocumentSafetyPolicy().name(),
                     metadata.get("krandom.identity-document-safety-policy").path("defaultValue").asText());
        assertEquals(defaults.getObjectConstructionPolicy().name(),
                     metadata.get("krandom.object-construction-policy").path("defaultValue").asText());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    public static final class SampleUser {
        public String firstName;
        public String lastName;
        public int age;
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomConfigConfiguration {

        @Bean
        GeneratorConfig customConfig() {
            return GeneratorConfig.builder().seed(99L).build();
        }
    }

    private static boolean hasMessage(Throwable failure, String expected) {
        Throwable current = failure;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(expected)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static Map<String, JsonNode> generatedMetadata() throws IOException {
        Enumeration<URL> resources = KrandomAutoConfigurationTest.class.getClassLoader()
            .getResources("META-INF/spring-configuration-metadata.json");
        Map<String, JsonNode> metadata = new HashMap<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try (InputStream input = resource.openStream()) {
                mergeKrandomProperties(input, metadata);
            }
        }
        try (InputStream input = KrandomAutoConfigurationTest.class.getResourceAsStream(
            "/META-INF/additional-spring-configuration-metadata.json")) {
            assertNotNull(input, "Additional Spring configuration metadata must be packaged with the starter");
            mergeKrandomProperties(input, metadata);
        }
        assertFalse(metadata.isEmpty(), "Spring configuration metadata must be packaged with the starter");
        return metadata;
    }

    private static void mergeKrandomProperties(InputStream input, Map<String, JsonNode> metadata) throws IOException {
        JsonNode properties = new ObjectMapper().readTree(input).path("properties");
        for (JsonNode property : properties) {
            String name = property.path("name").asText();
            if (name.startsWith("krandom.")) {
                metadata.merge(name, property, KrandomAutoConfigurationTest::mergeMetadata);
            }
        }
    }

    private static JsonNode mergeMetadata(JsonNode generated, JsonNode additional) {
        com.fasterxml.jackson.databind.node.ObjectNode merged = generated.deepCopy();
        merged.setAll((com.fasterxml.jackson.databind.node.ObjectNode) additional);
        return merged;
    }

    @Test
    @DisplayName("recipe property replays seed, locale, and profile")
    void recipePropertyReplaysConfiguration() {
        io.github.frikit.krandom.generator.GenerationRecipe recipe =
            io.github.frikit.krandom.generator.GenerationRecipe.builder()
                .seed(24680L)
                .locale(Locale.CANADA_FRENCH)
                .profile("spring-replay")
                .build();
        String encoded = "base64:" + java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(recipe.serialize().getBytes(java.nio.charset.StandardCharsets.UTF_8));

        runner.withPropertyValues("krandom.recipe=" + encoded)
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(24680L, config.getSeed().getAsLong());
                  assertEquals(Locale.CANADA_FRENCH, config.getLocale());
                  assertEquals("spring-replay", config.getGenerationProfile());
              });
    }

    @Test
    @DisplayName("recipe conflicts with seed and locale properties")
    void recipeConflictsWithSeedProperty() {
        runner.withPropertyValues("krandom.recipe=base64:xxx", "krandom.seed=1")
              .run(context -> {
                  assertNotNull(context.getStartupFailure());
                  assertTrue(rootMessage(context.getStartupFailure())
                      .contains("krandom.recipe or the individual krandom.seed/krandom.locale"));
              });
    }

    @Test
    @DisplayName("malformed recipe fails with an actionable message")
    void malformedRecipeFails() {
        runner.withPropertyValues("krandom.recipe=base64:not-a-recipe")
              .run(context -> {
                  assertNotNull(context.getStartupFailure());
                  assertTrue(rootMessage(context.getStartupFailure()).contains("Invalid krandom.recipe"));
              });
    }

    @Test
    @DisplayName("clock and clock-zone bind to a fixed clock")
    void clockPropertyBindsFixedClock() {
        runner.withPropertyValues("krandom.clock=2026-01-01T00:00:00Z",
                                  "krandom.clock-zone=Europe/Berlin")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(java.time.Instant.parse("2026-01-01T00:00:00Z"),
                               config.getClock().instant());
                  assertEquals(java.time.ZoneId.of("Europe/Berlin"), config.getClock().getZone());
              });
    }

    @Test
    @DisplayName("clock-zone without clock fails fast")
    void clockZoneWithoutClockFails() {
        runner.withPropertyValues("krandom.clock-zone=Europe/Berlin")
              .run(context -> {
                  assertNotNull(context.getStartupFailure());
                  assertTrue(rootMessage(context.getStartupFailure())
                      .contains("krandom.clock-zone requires krandom.clock"));
              });
    }

    @Test
    @DisplayName("invalid clock instant fails with an actionable message")
    void invalidClockFails() {
        runner.withPropertyValues("krandom.clock=not-a-time")
              .run(context -> {
                  assertNotNull(context.getStartupFailure());
                  assertTrue(rootMessage(context.getStartupFailure()).contains("Invalid krandom.clock"));
              });
    }

    @Test
    @DisplayName("safety and construction policies bind by relaxed enum name")
    void safetyAndConstructionPoliciesBind() {
        runner.withPropertyValues(
                  "krandom.banking-safety-policy=realistic-unclassified",
                  "krandom.national-id-safety-policy=realistic-unclassified",
                  "krandom.object-construction-policy=" +
                      io.github.frikit.krandom.generator.object.ObjectConstructionPolicy.values()[0].name())
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(io.github.frikit.krandom.generator.finance.BankingSafetyPolicy.REALISTIC_UNCLASSIFIED,
                               config.getBankingSafetyPolicy());
                  assertEquals(io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy.REALISTIC_UNCLASSIFIED,
                               config.getNationalIdSafetyPolicy());
                  assertEquals(io.github.frikit.krandom.generator.object.ObjectConstructionPolicy.values()[0],
                               config.getObjectConstructionPolicy());
              });
    }

    private static String rootMessage(Throwable failure) {
        Throwable current = failure;
        StringBuilder all = new StringBuilder();
        while (current != null) {
            all.append(current.getMessage()).append(' ');
            current = current.getCause();
        }
        return all.toString();
    }
}
