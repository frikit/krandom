/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Schema - outputs")
class SchemaOutputTest {

    @Test
    @DisplayName("toJsonLines renders nested values, arrays, chars, custom objects, and escapes")
    void toJsonLinesRendersStructuredValuesAndEscapes() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("id", ctx -> ctx.recordIndex());
        fields.put("text", ctx -> "a,\"b\"\n\t");
        fields.put("control", ctx -> "\u0001");
        fields.put("letter", ctx -> 'A');
        fields.put("flag", ctx -> true);
        fields.put("tags", ctx -> Arrays.asList("x", null));
        fields.put("meta", ctx -> {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("count", 2);
            meta.put("ok", false);
            return meta;
        });
        fields.put("numbers", ctx -> new int[]{ 1, 2 });
        fields.put("custom", ctx -> new NamedValue("json"));

        Schema schema = new Schema(fields);

        assertEquals(
            "{\"id\":0,\"text\":\"a,\\\"b\\\"\\n\\t\",\"control\":\"\\u0001\",\"letter\":\"A\","
            + "\"flag\":true,\"tags\":[\"x\",null],\"meta\":{\"count\":2,\"ok\":false},"
            + "\"numbers\":[1,2],\"custom\":\"NamedValue[value=json]\"}\n",
            schema.toJsonLines(1));
    }

    @Test
    @DisplayName("toJsonLines escapes backslash, backspace, formfeed, and carriage return")
    void toJsonLinesEscapesAdditionalControlCharacters() {
        Schema schema = new Schema(Map.of("value", ctx -> "\\\b\f\r"));

        String jsonl = schema.toJsonLines(1);

        assertEquals("{\"value\":\"" + "\\\\" + "\\b" + "\\f" + "\\r" + "\"}\n", jsonl);
    }

    @Test
    @DisplayName("toCsv renders header rows and serializes structured cells as JSON")
    void toCsvRendersHeaderRowsAndStructuredCells() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", ctx -> "Ada \"Ace\"");
        fields.put("padded", ctx -> " padded ");
        fields.put("meta", ctx -> {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("id", 1);
            meta.put("active", true);
            return meta;
        });
        fields.put("tags", ctx -> List.of("x", "y"));
        fields.put("custom", ctx -> new NamedValue("csv"));
        fields.put("blank", ctx -> null);

        Schema schema = new Schema(fields);

        assertEquals(
            "name,padded,meta,tags,custom,blank\n"
            + "\"Ada \"\"Ace\"\"\",\" padded \",\"{\"\"id\"\":1,\"\"active\"\":true}\","
            + "\"[\"\"x\"\",\"\"y\"\"]\",NamedValue[value=csv],\n",
            schema.toCsv(1));
    }

    @Test
    @DisplayName("toCsv quotes newline, carriage-return, and trailing-space cells")
    void toCsvQuotesNewlineCarriageReturnAndTrailingSpaceCells() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("newline", ctx -> "a\nb");
        fields.put("carriage", ctx -> "a\rb");
        fields.put("tail", ctx -> "tail ");
        fields.put("numbers", ctx -> new int[]{ 1, 2 });
        fields.put("letter", ctx -> 'Q');
        fields.put("active", ctx -> false);

        Schema schema = new Schema(fields);

        assertEquals(
            "newline,carriage,tail,numbers,letter,active\n"
            + "\"a\nb\",\"a\rb\",\"tail \",\"[1,2]\",Q,false\n",
            schema.toCsv(1));
    }

    @Test
    @DisplayName("write methods stream records and advance record index")
    void writeMethodsStreamRecordsAndAdvanceRecordIndex() throws IOException {
        Schema schema = new Schema(Map.of("index", ctx -> ctx.recordIndex()));

        StringBuilder jsonl = new StringBuilder("prefix:");
        schema.writeJsonLines(jsonl, 2);
        assertEquals("prefix:{\"index\":0}\n{\"index\":1}\n", jsonl.toString());

        StringBuilder csv = new StringBuilder();
        schema.writeCsv(csv, 1);
        assertEquals("index\n2\n", csv.toString());
    }

    @Test
    @DisplayName("zero-count outputs are valid and validation is enforced")
    void zeroCountOutputsAndValidation() {
        Schema schema = new Schema(Map.of("value", ctx -> 1));

        assertEquals("", schema.toJsonLines(0));
        assertEquals("value\n", schema.toCsv(0));

        assertThrows(NullPointerException.class, () -> schema.writeJsonLines(null, 1));
        assertThrows(NullPointerException.class, () -> schema.writeCsv(null, 1));
        assertThrows(IllegalArgumentException.class, () -> schema.writeJsonLines(new StringBuilder(), -1));
        assertThrows(IllegalArgumentException.class, () -> schema.writeCsv(new StringBuilder(), -1));
    }

    @Test
    @DisplayName("write methods propagate appendable failures")
    void writeMethodsPropagateAppendableFailures() {
        Schema schema = new Schema(Map.of("value", ctx -> "x"));

        assertThrows(IOException.class, () -> schema.writeJsonLines(new FailingAppendable(), 1));
        assertThrows(IOException.class, () -> schema.writeCsv(new FailingAppendable(), 1));
    }

    @Test
    @DisplayName("schema preserves caller field order for output headers")
    void schemaPreservesCallerFieldOrderForOutputHeaders() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("b", ctx -> 2);
        fields.put("a", ctx -> 1);

        Schema schema = new Schema(fields);

        assertEquals(List.of("b", "a"), List.copyOf(schema.getFields().keySet()));
        assertTrue(schema.toCsv(0).startsWith("b,a\n"));
    }

    record NamedValue(String value) {
    }

    static final class FailingAppendable implements Appendable {

        @Override
        public Appendable append(CharSequence csq) throws IOException {
            throw new IOException("boom");
        }

        @Override
        public Appendable append(CharSequence csq, int start, int end) throws IOException {
            throw new IOException("boom");
        }

        @Override
        public Appendable append(char c) throws IOException {
            throw new IOException("boom");
        }
    }
}
