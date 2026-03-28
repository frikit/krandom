/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
