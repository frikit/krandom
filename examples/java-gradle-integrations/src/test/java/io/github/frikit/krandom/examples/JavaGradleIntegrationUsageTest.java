package io.github.frikit.krandom.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.provider.ProviderHub;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import io.github.frikit.krandom.jackson.KrandomJackson;
import io.github.frikit.krandom.spring.KrandomAutoConfiguration;
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JavaGradleIntegrationUsageTest {

    @Test
    void jacksonModuleSerializesSchemaAsJsonSchema() {
        GeneratorConfig config = GeneratorConfig.builder()
            .locale(Locale.US)
            .seed(42L)
            .build();

        Field field = Generators.ofField(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("order", field.bind("commerce.order_info"));
        fields.put("payment", field.bind("finance.payment_info"));

        Schema schema = Generators.ofSchema(config, fields);
        ObjectMapper mapper = KrandomJackson.newObjectMapper();

        JsonNode jsonSchema = mapper.valueToTree(schema);

        assertEquals("object", jsonSchema.path("type").asText());
        assertEquals("object", jsonSchema.path("properties").path("order").path("type").asText());
        assertEquals("object", jsonSchema.path("properties").path("payment").path("type").asText());
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
            assertFalse(hub.get("person.email", Generator.class).generate().toString().isBlank());
            assertNotNull(factory.generator(SpringExampleUser.class).generate());
        });
    }

    public static final class SpringExampleUser {
        public String name;
        public String email;
    }
}
