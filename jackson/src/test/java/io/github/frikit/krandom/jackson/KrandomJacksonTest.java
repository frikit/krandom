/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("krandom jackson integration")
class KrandomJacksonTest {

    @Test
    @DisplayName("configure registers module on provided mapper")
    void configure() {
        ObjectMapper mapper = new ObjectMapper();
        assertSame(mapper, KrandomJackson.configure(mapper));
    }

    @Test
    @DisplayName("configure rejects null mapper")
    void configureNull() {
        assertThrows(NullPointerException.class, () -> KrandomJackson.configure(null));
    }

    @Test
    @DisplayName("schema serialization uses Schema.toJsonSchema shape")
    void schemaSerialization() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", SchemaValueProvider.withSample(ctx -> "alice", "alice"));
        fields.put("age", SchemaValueProvider.withSample(ctx -> 42, 42));
        fields.put("active", SchemaValueProvider.withSample(ctx -> true, true));

        Schema schema = new Schema(fields);
        ObjectMapper mapper = KrandomJackson.newObjectMapper();
        JsonNode node = mapper.valueToTree(schema);

        assertEquals("https://json-schema.org/draft/2020-12/schema", node.get("$schema").asText());
        assertEquals("object", node.get("type").asText());
        assertEquals("string", node.path("properties").path("name").path("type").asText());
        assertEquals("integer", node.path("properties").path("age").path("type").asText());
        assertEquals("boolean", node.path("properties").path("active").path("type").asText());
    }

    @Test
    @DisplayName("schemas and generated rows serialize composite records")
    void schemaAndRowsSerializeCompositeRecords() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("company", field.bind("company.info"));
        fields.put("card", field.bind("finance.credit_card_info"));

        Schema schema = new Schema(config, fields);
        ObjectMapper mapper = KrandomJackson.newObjectMapper();

        JsonNode schemaNode = mapper.valueToTree(schema);
        assertEquals("object", schemaNode.path("properties").path("company").path("type").asText());
        assertEquals("object", schemaNode.path("properties").path("company")
                                         .path("properties").path("address").path("type").asText());
        assertEquals("object", schemaNode.path("properties").path("card").path("type").asText());

        JsonNode rowNode = mapper.valueToTree(schema.generate());
        assertTrue(rowNode.path("company").path("name").isTextual());
        assertTrue(rowNode.path("company").path("address").path("city").isTextual());
        assertTrue(rowNode.path("card").path("number").isTextual());
    }

    @Test
    @DisplayName("schema serialization does not advance generated row sequence")
    void schemaSerializationDoesNotAdvanceGeneratedRows() {
        GeneratorConfig config = GeneratorConfig.builder().locale(Locale.US).seed(123L).build();
        Field fieldA = new Field(config);
        Field fieldB = new Field(config);
        Map<String, SchemaValueProvider> fieldsA = new LinkedHashMap<>();
        fieldsA.put("order", fieldA.bind("commerce.order_info"));
        fieldsA.put("payment", fieldA.bind("finance.payment_info"));
        Map<String, SchemaValueProvider> fieldsB = new LinkedHashMap<>();
        fieldsB.put("order", fieldB.bind("commerce.order_info"));
        fieldsB.put("payment", fieldB.bind("finance.payment_info"));

        Schema exported = new Schema(config, fieldsA);
        Schema untouched = new Schema(config, fieldsB);

        KrandomJackson.newObjectMapper().valueToTree(exported);

        assertEquals(untouched.generate(), exported.generate());
    }
}
