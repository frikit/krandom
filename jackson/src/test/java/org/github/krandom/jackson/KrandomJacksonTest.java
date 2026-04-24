/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
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
        fields.put("name", ctx -> "alice");
        fields.put("age", ctx -> 42);
        fields.put("active", ctx -> true);

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
}
