/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.BusinessTaxIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.finance.BankingSafetyPolicy;
import io.github.frikit.krandom.generator.finance.CryptoAddressSafetyPolicy;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.finance.SecuritiesIdentifierSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.object.ObjectConstructionPolicy;
import io.github.frikit.krandom.generator.user.IdentityDocumentSafetyPolicy;
import io.github.frikit.krandom.generator.user.nationalid.NationalIdSafetyPolicy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class KrandomRecipeExclusivityTest {

    private static final String RECIPE = encode(GeneratorConfig.builder()
                                                               .seed(24680L)
                                                               .locale(Locale.CANADA_FRENCH)
                                                               .build()
                                                               .getGenerationRecipe()
                                                               .orElseThrow());

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class));

    static Stream<String> propertiesTheRecipeDefines() {
        return Stream.of(
            "krandom.seed=1",
            "krandom.locale=en-US",
            "krandom.clock=2026-01-01T00:00:00Z",
            "krandom.clock-zone=Europe/Berlin",
            "krandom.object-max-depth=3",
            "krandom.object-null-probability=0.1",
            "krandom.min-string-length=2",
            "krandom.max-string-length=9",
            "krandom.min-collection-size=1",
            "krandom.max-collection-size=3",
            "krandom.payment-card-safety-policy=" + PaymentCardSafetyPolicy.values()[0].name(),
            "krandom.phone-number-safety-policy=" + PhoneNumberSafetyPolicy.values()[0].name(),
            "krandom.national-id-safety-policy=" + NationalIdSafetyPolicy.values()[0].name(),
            "krandom.banking-safety-policy=" + BankingSafetyPolicy.values()[0].name(),
            "krandom.securities-identifier-safety-policy=" + SecuritiesIdentifierSafetyPolicy.values()[0].name(),
            "krandom.crypto-address-safety-policy=" + CryptoAddressSafetyPolicy.values()[0].name(),
            "krandom.business-tax-identifier-safety-policy="
                + BusinessTaxIdentifierSafetyPolicy.values()[0].name(),
            "krandom.identity-document-safety-policy=" + IdentityDocumentSafetyPolicy.values()[0].name(),
            "krandom.object-construction-policy=" + ObjectConstructionPolicy.values()[0].name());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("propertiesTheRecipeDefines")
    @DisplayName("krandom.recipe rejects an explicitly set property it already defines")
    void recipeRejectsConflictingProperty(String property) {
        String name = property.substring(0, property.indexOf('='));
        runner.withPropertyValues("krandom.recipe=" + RECIPE, property).run(context -> {
            Throwable failure = context.getStartupFailure();
            assertNotNull(failure, name + " must not silently override the recipe");
            String messages = messages(failure);
            assertTrue(messages.contains("krandom.recipe") && messages.contains(name), messages);
        });
    }

    @Test
    @DisplayName("krandom.recipe names every conflicting property at once")
    void recipeNamesAllConflicts() {
        runner.withPropertyValues("krandom.recipe=" + RECIPE, "krandom.seed=1", "krandom.clock=2026-01-01T00:00:00Z")
              .run(context -> {
                  String messages = messages(context.getStartupFailure());
                  assertTrue(messages.contains("krandom.seed") && messages.contains("krandom.clock"), messages);
              });
    }

    @Test
    @DisplayName("blank companion properties do not count as set")
    void blankPropertiesAreIgnored() {
        runner.withPropertyValues("krandom.recipe=" + RECIPE, "krandom.locale=", "krandom.clock=")
              .run(context -> {
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  assertEquals(24680L, config.getSeed().getAsLong());
                  assertEquals(Locale.CANADA_FRENCH, config.getLocale());
              });
    }

    @Test
    @DisplayName("the effective recipe is logged at startup")
    void effectiveRecipeIsLogged(CapturedOutput output) {
        runner.withPropertyValues("krandom.seed=42", "krandom.clock=2026-01-01T00:00:00Z").run(context -> {
            assertNotNull(context.getBean(GeneratorConfig.class));
            assertTrue(output.getAll().contains("krandom.recipe=base64:"), output.getAll());
            assertTrue(output.getAll().contains("seed=42"), output.getAll());
            assertTrue(output.getAll().contains("format=krandom-recipe"), output.getAll());
        });
    }

    @Test
    @DisplayName("an unseeded configuration logs no recipe")
    void unseededConfigurationLogsNoRecipe(CapturedOutput output) {
        runner.run(context -> {
            assertNotNull(context.getBean(GeneratorConfig.class));
            assertFalse(output.getAll().contains("format=krandom-recipe"), output.getAll());
        });
    }

    private static String encode(GenerationRecipe recipe) {
        return "base64:" + Base64.getUrlEncoder().withoutPadding()
                                 .encodeToString(recipe.serialize().getBytes(StandardCharsets.UTF_8));
    }

    private static String messages(Throwable failure) {
        StringBuilder all = new StringBuilder();
        for (Throwable current = failure; current != null; current = current.getCause()) {
            all.append(current.getMessage()).append(' ');
        }
        return all.toString();
    }
}
