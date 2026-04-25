/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.spring;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
