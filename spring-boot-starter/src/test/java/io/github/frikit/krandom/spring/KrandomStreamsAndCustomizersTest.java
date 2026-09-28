/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KrandomStreamsAndCustomizersTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class));

    @Test
    @DisplayName("seeded generator(type) restarts the same sequence on every call")
    void seededGeneratorRestartsPerCall() {
        runner.withPropertyValues("krandom.seed=42").run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
            assertEquals(fields(factory.generator(Account.class).generate()),
                         fields(factory.generator(Account.class).generate()));
        });
    }

    @Test
    @DisplayName("named streams derive independent, reproducible child seeds")
    void namedStreamsDeriveChildSeeds() {
        runner.withPropertyValues("krandom.seed=42").run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);

            List<Object> first = fields(factory.generator(Account.class, "order-1").generate());
            List<Object> again = fields(factory.generator(Account.class, "order-1").generate());
            List<Object> second = fields(factory.generator(Account.class, "order-2").generate());

            assertEquals(first, again, "the same stream name must replay the same sequence");
            assertNotEquals(first, second, "different stream names must not restart the same sequence");
            GeneratorConfig child = GeneratorConfig.builder()
                                                   .seed(GenerationRecipe.deriveChildSeed(42L, "order-1"))
                                                   .build();
            assertEquals(fields(new ObjectGenerator<>(Account.class, child).generate()), first,
                         "the child seed is GenerationRecipe.deriveChildSeed(seed, streamName)");
        });
    }

    @Test
    @DisplayName("named faker streams use the same child seed as named generator streams")
    void namedFakerStreams() {
        runner.withPropertyValues("krandom.seed=42").run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
            assertEquals(fields(factory.generator(Account.class, "order-1").generate()),
                         fields(factory.faker(Account.class, "order-1").generate()));
            assertNotEquals(fields(factory.faker(Account.class, "order-1").generate()),
                            fields(factory.faker(Account.class, "order-2").generate()));
        });
    }

    @Test
    @DisplayName("named streams keep the rest of the configuration")
    void namedStreamsKeepConfiguration() {
        runner.withPropertyValues("krandom.seed=42", "krandom.min-string-length=12", "krandom.max-string-length=13")
              .run(context -> {
                  KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
                  GeneratorConfig config = context.getBean(GeneratorConfig.class);
                  Account account = factory.generator(Account.class, "lengths").generate();

                  GeneratorConfig child = config.toBuilder()
                                                .seed(GenerationRecipe.deriveChildSeed(42L, "lengths"))
                                                .build();
                  assertEquals(fields(new ObjectGenerator<>(Account.class, child).generate()), fields(account));
                  assertEquals(true, account.code.length() >= 12 && account.code.length() <= 13, account.code);
              });
    }

    @Test
    @DisplayName("named streams of an unseeded configuration stay unseeded")
    void namedStreamsWithoutSeed() {
        runner.run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
            assertNotNull(factory.generator(Account.class, "any").generate());
        });
    }

    @Test
    @DisplayName("a blank stream name is rejected")
    void blankStreamNameIsRejected() {
        runner.withPropertyValues("krandom.seed=42").run(context -> {
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);
            assertThrows(IllegalArgumentException.class, () -> factory.generator(Account.class, " "));
        });
    }

    @Test
    @DisplayName("provider customizers run once, in order, when the hub is created")
    void providerCustomizersApplyInOrder() {
        runner.withUserConfiguration(CustomizerConfiguration.class).run(context -> {
            ProviderHub hub = context.getBean(ProviderHub.class);
            assertEquals("fixed", hub.get("test.constant", Generator.class).generate());
            assertEquals("fixed", hub.get("test.alias", Generator.class).generate());
            assertSame(hub, context.getBean(ProviderHub.class));
        });
    }

    @Test
    @DisplayName("provider customizers do not touch an application-defined hub")
    void customizersSkipUserHub() {
        runner.withUserConfiguration(CustomizerConfiguration.class, UserHubConfiguration.class).run(context -> {
            ProviderHub hub = context.getBean(ProviderHub.class);
            assertEquals(false, hub.has("test.constant"));
        });
    }

    private static List<Object> fields(Account account) {
        return List.of(account.code, account.balance);
    }

    public static final class Account {
        public String code;
        public long balance;
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomizerConfiguration {

        @Bean
        @Order(2)
        KrandomProviderCustomizer aliasCustomizer() {
            return hub -> hub.registerAlias("test.alias", "test.constant");
        }

        @Bean
        @Order(1)
        KrandomProviderCustomizer constantCustomizer() {
            return hub -> hub.register("test.constant", config -> (Generator<String>) () -> "fixed");
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class UserHubConfiguration {

        @Bean
        ProviderHub userHub() {
            return new ProviderHub();
        }
    }
}
