/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.jackson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.commerce.OrderInfo;
import io.github.frikit.krandom.generator.datetime.ZoneOffsetGenerator;
import io.github.frikit.krandom.generator.schema.Field;
import io.github.frikit.krandom.generator.schema.OutputFormat;
import io.github.frikit.krandom.generator.schema.Schema;
import io.github.frikit.krandom.generator.schema.SchemaValueProvider;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.json.JsonMapper;

@DisplayName("krandom jackson integration")
class KrandomJacksonTest {

    @Test
    @DisplayName("configure adds the module to the provided builder and returns that builder")
    void configure() {
        JsonMapper.Builder builder = JsonMapper.builder();

        assertSame(builder, KrandomJackson.configure(builder));
        assertTrue(builder.build().registeredModules().stream().anyMatch(KrandomJacksonModule.class::isInstance));
    }

    @Test
    @DisplayName("configure rejects null builder")
    void configureNull() {
        assertThrows(NullPointerException.class, () -> KrandomJackson.configure((JsonMapper.Builder) null));
    }

    @Test
    @DisplayName("schema serialization uses Schema.toJsonSchema shape")
    void schemaSerialization() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", SchemaValueProvider.withSample(ctx -> "alice", "alice"));
        fields.put("age", SchemaValueProvider.withSample(ctx -> 42, 42));
        fields.put("active", SchemaValueProvider.withSample(ctx -> true, true));

        Schema schema = new Schema(fields);
        JsonMapper mapper = KrandomJackson.newJsonMapper();
        JsonNode node = mapper.valueToTree(schema);

        assertEquals(
                "https://json-schema.org/draft/2020-12/schema",
                node.get("$schema").asString());
        assertEquals("object", node.get("type").asString());
        assertEquals("string", node.path("properties").path("name").path("type").asString());
        assertEquals("integer", node.path("properties").path("age").path("type").asString());
        assertEquals(
                "boolean", node.path("properties").path("active").path("type").asString());
    }

    @Test
    @DisplayName("schema written as JSON text is the same document as its tree")
    void schemaSerializationAsText() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", SchemaValueProvider.withSample(ctx -> "alice", "alice"));

        Schema schema = new Schema(fields);
        JsonMapper mapper = KrandomJackson.newJsonMapper();
        JsonNode parsed = mapper.readTree(mapper.writeValueAsString(schema));

        assertEquals(mapper.valueToTree(schema), parsed);
        assertEquals(
                "string", parsed.path("properties").path("name").path("type").asString());
    }

    @Test
    @DisplayName("schemas and generated rows serialize composite records")
    void schemaAndRowsSerializeCompositeRecords() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("company", field.bind("company.info"));
        fields.put("card", field.bind("finance.credit_card_info"));

        Schema schema = new Schema(config, fields);
        JsonMapper mapper = KrandomJackson.newJsonMapper();

        JsonNode schemaNode = mapper.valueToTree(schema);
        assertEquals(
                "object",
                schemaNode.path("properties").path("company").path("type").asString());
        assertEquals(
                "object",
                schemaNode
                        .path("properties")
                        .path("company")
                        .path("properties")
                        .path("address")
                        .path("type")
                        .asString());
        assertEquals(
                "object",
                schemaNode.path("properties").path("card").path("type").asString());

        JsonNode rowNode = mapper.valueToTree(schema.generate());
        assertTrue(rowNode.path("company").path("name").isString());
        assertTrue(rowNode.path("company").path("address").path("city").isString());
        assertTrue(rowNode.path("card").path("number").isString());
    }

    @Test
    @DisplayName("generated rows with java.time values serialize them as ISO-8601 text")
    void rowsWithJavaTimeValuesSerialize() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(42L).build();
        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("order", field.bind("commerce.order_info"));
        Map<String, Object> row = new Schema(config, fields).generate();
        LocalDate orderedOn = ((OrderInfo) row.get("order")).orderedOn();

        JsonNode rowNode = KrandomJackson.newJsonMapper().valueToTree(row);

        assertEquals(
                orderedOn.toString(), rowNode.path("order").path("orderedOn").asString());
    }

    @Test
    @DisplayName("a generated offset's ISO text has hh:mm, which Jackson's Date parser needs")
    void generatedOffsetsParseAsJacksonDates() {
        JsonMapper mapper = KrandomJackson.newJsonMapper();
        ZoneOffsetGenerator offsets =
                new ZoneOffsetGenerator(GeneratorConfig.builder().seed(42L).build());
        LocalDateTime local = LocalDateTime.of(2020, 1, 1, 10, 15, 30);

        for (int i = 0; i < 500; i++) {
            OffsetDateTime value = local.atOffset(offsets.generate());
            String text = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
            assertTrue(text.matches("2020-01-01T10:15:30(Z|[+-]\\d{2}:\\d{2})"), text);
            assertEquals(
                    value.toInstant(),
                    mapper.readValue(mapper.writeValueAsString(text), Date.class)
                            .toInstant());
        }
        assertThrows(
                InvalidFormatException.class,
                () -> mapper.readValue("\"2020-01-01T10:15:30+07:17:55\"", Date.class),
                "the parser refuses seconds in an offset, so the loop above is a real check");
    }

    @Test
    @DisplayName("schema JSON with unpaired surrogates encodes as strict UTF-8 and reads back with U+FFFD")
    void schemaJsonWithUnpairedSurrogatesReadsBack() throws Exception {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("high\uD800", ctx -> "a\uD800b");
        fields.put("low\uDC00", ctx -> "a\uDC00b");
        fields.put("reversed", ctx -> "\uDC00\uD800");
        fields.put("pair\uD83D\uDE00", ctx -> "a\uD83D\uDE00b");
        fields.put("nested", ctx -> Map.of("k\uDBFF", List.of("\uDC00", '\uD800')));
        Schema schema = new Schema(fields);
        JsonMapper mapper = KrandomJackson.newJsonMapper();
        CharsetEncoder strict = StandardCharsets.UTF_8
                .newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        ByteArrayOutputStream streamed = new ByteArrayOutputStream();
        schema.writeTo(streamed, OutputFormat.JSON, 1);

        List<JsonNode> rows = List.of(
                mapper.readTree(bytes(strict.encode(CharBuffer.wrap(schema.toJson(1)))))
                        .get(0),
                mapper.readTree(bytes(strict.encode(CharBuffer.wrap(schema.toJsonLines(1))))),
                mapper.readTree(streamed.toByteArray()).get(0));

        for (JsonNode row : rows) {
            assertEquals("a\uFFFDb", row.path("high\uFFFD").asString());
            assertEquals("a\uFFFDb", row.path("low\uFFFD").asString());
            assertEquals("\uFFFD\uFFFD", row.path("reversed").asString());
            assertEquals("a\uD83D\uDE00b", row.path("pair\uD83D\uDE00").asString());
            assertEquals("\uFFFD", row.path("nested").path("k\uFFFD").path(0).asString());
            assertEquals("\uFFFD", row.path("nested").path("k\uFFFD").path(1).asString());
        }
    }

    private static byte[] bytes(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    @Test
    @DisplayName("schema serialization does not advance generated row sequence")
    void schemaSerializationDoesNotAdvanceGeneratedRows() {
        GeneratorConfig config =
                GeneratorConfig.builder().locale(Locale.US).seed(123L).build();
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

        KrandomJackson.newJsonMapper().valueToTree(exported);

        assertEquals(untouched.generate(), exported.generate());
    }
}
