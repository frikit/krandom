/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.schema.OutputFormat;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Reads a schema's YAML back with SnakeYAML, which the Spring Boot test stack brings, so that this module is
 * where YAML output meets a YAML parser.
 */
class SchemaYamlOutputTest {

    @Test
    @DisplayName("schema YAML with unpaired surrogates encodes as strict UTF-8 and reads back with U+FFFD")
    void schemaYamlWithUnpairedSurrogatesReadsBack() throws Exception {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("high\uD800", ctx -> "a\uD800b");
        fields.put("low\uDC00", ctx -> "a\uDC00b");
        fields.put("reversed", ctx -> "\uDC00\uD800");
        fields.put("pair\uD83D\uDE00", ctx -> "a\uD83D\uDE00b");
        fields.put("nested", ctx -> Map.of("k\uDBFF", List.of("\uDC00", '\uD800')));
        Schema schema = new Schema(fields);
        ByteBuffer encoded = StandardCharsets.UTF_8
                .newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(schema.toYaml(1)));
        byte[] text = new byte[encoded.remaining()];
        encoded.get(text);
        ByteArrayOutputStream streamed = new ByteArrayOutputStream();
        schema.writeTo(streamed, OutputFormat.YAML, 1);

        for (byte[] document : List.of(text, streamed.toByteArray())) {
            List<?> records = new Yaml().load(new ByteArrayInputStream(document));
            Map<?, ?> row = (Map<?, ?>) records.get(0);

            assertEquals("a\uFFFDb", row.get("high\uFFFD"));
            assertEquals("a\uFFFDb", row.get("low\uFFFD"));
            assertEquals("\uFFFD\uFFFD", row.get("reversed"));
            assertEquals("a\uD83D\uDE00b", row.get("pair\uD83D\uDE00"));
            assertEquals(Map.of("k\uFFFD", List.of("\uFFFD", "\uFFFD")), row.get("nested"));
        }
    }

    @Test
    @DisplayName("schema YAML with DEL, C1 controls, U+2028, U+2029, U+FFFE and U+FFFF reads back as written")
    void schemaYamlWithUnprintableCharactersReadsBack() throws Exception {
        StringBuilder text = new StringBuilder("a ");
        for (char ch = 0x7F; ch <= 0xA0; ch++) {
            text.append(ch);
        }
        text.append(" \u2028 \u2029 \ufeff \ufffd \ufffe \uffff z");
        String value = text.toString();
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("key " + value, ctx -> value);
        fields.put("nested", ctx -> Map.of("n" + value, List.of(value, value.charAt(8))));
        Schema schema = new Schema(fields);

        ByteArrayOutputStream streamed = new ByteArrayOutputStream();
        schema.writeTo(streamed, OutputFormat.YAML, 1);
        for (byte[] document : List.of(schema.toYaml(1).getBytes(StandardCharsets.UTF_8), streamed.toByteArray())) {
            List<?> records = new Yaml().load(new ByteArrayInputStream(document));
            Map<?, ?> row = (Map<?, ?>) records.get(0);

            assertEquals(value, row.get("key " + value));
            assertEquals(Map.of("n" + value, List.of(value, String.valueOf(value.charAt(8)))), row.get("nested"));
        }
    }
}
