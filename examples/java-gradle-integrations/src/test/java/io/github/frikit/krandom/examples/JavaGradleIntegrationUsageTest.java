/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.commerce.OrderInfo;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import io.github.frikit.krandom.jackson.KrandomJackson;
import io.github.frikit.krandom.spring.KrandomAutoConfiguration;
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class JavaGradleIntegrationUsageTest {

    @Test
    void jacksonModuleSerializesSchemaAsJsonSchema() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();

        Field field = Generators.ofField(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("order", field.bind("commerce.order_info"));
        fields.put("payment", field.bind("finance.payment_info"));

        Schema schema = Generators.ofSchema(config, fields);
        JsonMapper mapper = KrandomJackson.newJsonMapper();

        JsonNode jsonSchema = mapper.valueToTree(schema);

        assertEquals("object", jsonSchema.path("type").asString());
        assertEquals(
                "object",
                jsonSchema.path("properties").path("order").path("type").asString());
        assertEquals(
                "object",
                jsonSchema.path("properties").path("payment").path("type").asString());
    }

    @Test
    void jackson3WritesGeneratedJavaTimeValuesAsIsoText() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        Schema schema = Generators.ofSchema(
                config, Map.of("order", Generators.ofField(config).bind("commerce.order_info")));
        // Jackson 3 mappers are immutable, so the module is added to the builder.
        JsonMapper mapper = KrandomJackson.configure(JsonMapper.builder()).build();

        Map<String, Object> row = schema.generate();
        JsonNode json = mapper.valueToTree(row);

        // Jackson 3 writes java.time values as ISO-8601 text without an extra module.
        OrderInfo order = (OrderInfo) row.get("order");
        assertEquals(
                order.orderedOn().toString(),
                json.path("order").path("orderedOn").asString());
    }

    @Test
    void springStarterAutoConfiguresKrandomBeans() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class))
                .withPropertyValues("krandom.locale=en-US", "krandom.seed=42");

        runner.run(context -> {
            GeneratorConfig config = context.getBean(GeneratorConfig.class);
            ProviderHub hub = context.getBean(ProviderHub.class);
            KrandomObjectFakerFactory factory = context.getBean(KrandomObjectFakerFactory.class);

            assertEquals(Locale.US, config.getLocale());
            assertFalse(hub.get("person.email", Generator.class)
                    .generate()
                    .toString()
                    .isBlank());
            assertNotNull(factory.generator(SpringExampleUser.class).generate());
        });
    }

    @Test
    void springStarterBindsKrandomProperties() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration.class))
                .withPropertyValues(
                        "krandom.seed=42",
                        "krandom.locale=de-DE",
                        "krandom.clock=2026-01-01T00:00:00Z",
                        "krandom.clock-zone=Europe/Berlin",
                        "krandom.object-max-depth=3",
                        "krandom.min-collection-size=2",
                        "krandom.max-collection-size=4",
                        "krandom.payment-card-safety-policy=stripe-sandbox");

        runner.run(context -> {
            GeneratorConfig config = context.getBean(GeneratorConfig.class);

            assertEquals(OptionalLong.of(42L), config.getSeed());
            assertEquals(Locale.GERMANY, config.getLocale());
            assertEquals(
                    Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Europe/Berlin")), config.getClock());
            assertEquals(3, config.getObjectMaxDepth());
            assertEquals(2, config.getMinCollectionSize());
            assertEquals(4, config.getMaxCollectionSize());
            assertEquals(PaymentCardSafetyPolicy.STRIPE_SANDBOX, config.getPaymentCardSafetyPolicy());
        });
    }

    public static final class SpringExampleUser {
        public String name;
        public String email;
    }
}
