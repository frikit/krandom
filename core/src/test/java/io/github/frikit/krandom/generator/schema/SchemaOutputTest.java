/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.failure.GenerationFailureCategory;
import io.github.frikit.krandom.generator.failure.GenerationOperation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        fields.put("plain", ctx -> new PlainValue("json"));

        Schema schema = new Schema(fields);

        assertEquals(
            "{\"id\":0,\"text\":\"a,\\\"b\\\"\\n\\t\",\"control\":\"\\u0001\",\"letter\":\"A\","
            + "\"flag\":true,\"tags\":[\"x\",null],\"meta\":{\"count\":2,\"ok\":false},"
            + "\"numbers\":[1,2],\"custom\":{\"value\":\"json\"},\"plain\":\"PlainValue[value=json]\"}\n",
            schema.toJsonLines(1));
    }

    @Test
    @DisplayName("record accessor failures expose structured component context")
    void recordAccessorFailuresExposeStructuredContext() {
        Schema schema = new Schema(Map.of("record", ctx -> new ThrowingRecord("payload")));

        SchemaGenerationException ex = assertThrows(SchemaGenerationException.class, () -> schema.toJsonLines(1));
        var context = ex.getContext().orElseThrow();
        assertEquals(GenerationFailureCategory.REFLECTION, context.category());
        assertEquals(GenerationOperation.READ, context.operation());
        assertEquals("value", context.path());
        assertEquals(ThrowingRecord.class, context.ownerType());
        assertEquals(String.class.getTypeName(), context.declaredType());
        assertEquals(-1, context.recordIndex());
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertFalse(ex.getMessage().contains("accessor failure"));
    }

    @Test
    @DisplayName("toJsonLines escapes backslash, backspace, formfeed, and carriage return")
    void toJsonLinesEscapesAdditionalControlCharacters() {
        Schema schema = new Schema(Map.of("value", ctx -> "\\\b\f\r"));

        String jsonl = schema.toJsonLines(1);

        assertEquals("{\"value\":\"" + "\\\\" + "\\b" + "\\f" + "\\r" + "\"}\n", jsonl);
    }

    @Test
    @DisplayName("toJsonLines renders finite numbers and rejects non-finite numbers")
    void toJsonLinesRejectsNonFiniteNumbers() {
        Schema finite = new Schema(Map.of(
            "doubleValue", ctx -> 1.5d,
            "floatValue", ctx -> 2.5f,
            "intValue", ctx -> 7
        ));

        String jsonl = finite.toJsonLines(1);

        assertTrue(jsonl.contains("\"doubleValue\":1.5"));
        assertTrue(jsonl.contains("\"floatValue\":2.5"));
        assertTrue(jsonl.contains("\"intValue\":7"));
        assertThrows(IllegalArgumentException.class,
                     () -> new Schema(Map.of("value", ctx -> Double.NaN)).toJsonLines(1));
        assertThrows(IllegalArgumentException.class,
                     () -> new Schema(Map.of("value", ctx -> Float.POSITIVE_INFINITY)).toJsonLines(1));
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
        fields.put("plain", ctx -> new PlainValue("csv"));
        fields.put("blank", ctx -> null);

        Schema schema = new Schema(fields);

        assertEquals(
            "name,padded,meta,tags,custom,plain,blank\n"
            + "\"Ada \"\"Ace\"\"\",\" padded \",\"{\"\"id\"\":1,\"\"active\"\":true}\","
            + "\"[\"\"x\"\",\"\"y\"\"]\",\"{\"\"value\"\":\"\"csv\"\"}\",PlainValue[value=csv],\n",
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
    @DisplayName("toXml renders escaped values, complex payloads, and invalid field-name fallback")
    void toXmlRendersEscapedValuesAndFallbackFieldNames() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", ctx -> "Ada & <Ace>");
        fields.put("meta", ctx -> {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("id", 1);
            meta.put("active", true);
            return meta;
        });
        fields.put("tags", ctx -> List.of("x", "y"));
        fields.put("bad name", ctx -> "quoted \"value\" and 'apostrophe'");
        fields.put("missing", ctx -> null);

        Schema schema = new Schema(fields);

        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<records>\n"
            + "  <record>\n"
            + "    <name>Ada &amp; &lt;Ace&gt;</name>\n"
            + "    <meta>{\"id\":1,\"active\":true}</meta>\n"
            + "    <tags>[\"x\",\"y\"]</tags>\n"
            + "    <field name=\"bad name\">quoted \"value\" and 'apostrophe'</field>\n"
            + "    <missing/>\n"
            + "  </record>\n"
            + "</records>\n",
            schema.toXml(1));
    }

    @Test
    @DisplayName("toXml zero count returns an empty root document")
    void toXmlZeroCountReturnsEmptyRootDocument() {
        Schema schema = new Schema(Map.of("value", ctx -> 1));

        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<records/>\n",
            schema.toXml(0));
    }

    @Test
    @DisplayName("toXml covers scalar variants and escapes invalid field-name attributes")
    void toXmlCoversScalarVariantsAndAttributeEscaping() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("count", ctx -> 5);
        fields.put("enabled", ctx -> false);
        fields.put("letter", ctx -> 'Q');
        fields.put("numbers", ctx -> new int[]{ 1, 2 });
        fields.put("1bad&<>'\"", ctx -> new NamedValue("xml"));
        fields.put("plain", ctx -> new PlainValue("xml"));

        String xml = new Schema(fields).toXml(1);

        assertTrue(xml.contains("<count>5</count>"));
        assertTrue(xml.contains("<enabled>false</enabled>"));
        assertTrue(xml.contains("<letter>Q</letter>"));
        assertTrue(xml.contains("<numbers>[1,2]</numbers>"));
        assertTrue(xml.contains("<field name=\"1bad&amp;&lt;&gt;&apos;&quot;\">{\"value\":\"xml\"}</field>"));
        assertTrue(xml.contains("<plain>PlainValue[value=xml]</plain>"));
    }

    @Test
    @DisplayName("toSqlInserts quotes identifiers and escapes values")
    void toSqlInsertsQuotesIdentifiersAndEscapesValues() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("id", ctx -> 7);
        fields.put("full name", ctx -> "Ada's");
        fields.put("active", ctx -> true);
        fields.put("meta", ctx -> {
            Map<String, Object> meta = new LinkedHashMap<>();
            meta.put("id", 1);
            meta.put("tags", List.of("x", "y"));
            return meta;
        });
        fields.put("notes", ctx -> null);
        fields.put("custom", ctx -> new NamedValue("sql"));
        fields.put("plain", ctx -> new PlainValue("sql"));

        Schema schema = new Schema(fields);

        assertEquals(
            "INSERT INTO \"public\".\"orders\" (\"id\", \"full name\", \"active\", \"meta\", \"notes\", \"custom\", \"plain\") VALUES "
            + "(7, 'Ada''s', TRUE, '{\"id\":1,\"tags\":[\"x\",\"y\"]}', NULL, '{\"value\":\"sql\"}', "
            + "'PlainValue[value=sql]');\n",
            schema.toSqlInserts("public.orders", 1));
    }

    @Test
    @DisplayName("toSqlInserts covers false booleans, iterable and array values, and quoted identifiers")
    void toSqlInsertsCoversAdditionalSqlBranches() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("quoted\"name", ctx -> "x");
        fields.put("enabled", ctx -> false);
        fields.put("tags", ctx -> List.of("a", "b"));
        fields.put("numbers", ctx -> new int[]{ 1, 2 });

        String sql = new Schema(fields).toSqlInserts("audit.event\"logs", 1);

        assertEquals(
            "INSERT INTO \"audit\".\"event\"\"logs\" (\"quoted\"\"name\", \"enabled\", \"tags\", \"numbers\") VALUES "
            + "('x', FALSE, '[\"a\",\"b\"]', '[1,2]');\n",
            sql);
    }

    @Test
    @DisplayName("toSqlInserts rejects non-finite numbers")
    void toSqlInsertsRejectsNonFiniteNumbers() {
        Map<String, SchemaValueProvider> finiteFields = new LinkedHashMap<>();
        finiteFields.put("doubleValue", ctx -> 1.5d);
        finiteFields.put("floatValue", ctx -> 2.5f);
        finiteFields.put("intValue", ctx -> 7);
        Schema finite = new Schema(finiteFields);

        String sql = finite.toSqlInserts("metrics", 1);

        assertTrue(sql.contains("(1.5, 2.5, 7)"));
        assertThrows(IllegalArgumentException.class,
                     () -> new Schema(Map.of("value", ctx -> Double.NaN)).toSqlInserts("metrics", 1));
        assertThrows(IllegalArgumentException.class,
                     () -> new Schema(Map.of("value", ctx -> Float.NEGATIVE_INFINITY)).toSqlInserts("metrics", 1));
    }

    @Test
    @DisplayName("writeXml and writeSqlInserts stream records and validation is enforced")
    void writeXmlAndWriteSqlInsertsStreamRecordsAndValidate() throws IOException {
        Schema schema = new Schema(Map.of("index", ctx -> ctx.recordIndex()));

        StringBuilder xml = new StringBuilder();
        schema.writeXml(xml, 1);
        assertEquals(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<records>\n"
            + "  <record>\n"
            + "    <index>0</index>\n"
            + "  </record>\n"
            + "</records>\n",
            xml.toString());

        StringBuilder sql = new StringBuilder();
        schema.writeSqlInserts(sql, "events", 2);
        assertEquals(
            "INSERT INTO \"events\" (\"index\") VALUES (1);\n"
            + "INSERT INTO \"events\" (\"index\") VALUES (2);\n",
            sql.toString());

        assertThrows(NullPointerException.class, () -> schema.writeXml(null, 1));
        assertThrows(NullPointerException.class, () -> schema.writeSqlInserts(null, "events", 1));
        assertThrows(NullPointerException.class, () -> schema.toSqlInserts(null, 1));
        assertThrows(IllegalArgumentException.class, () -> schema.writeXml(new StringBuilder(), -1));
        assertThrows(IllegalArgumentException.class, () -> schema.writeSqlInserts(new StringBuilder(), " ", 1));
        assertThrows(IllegalArgumentException.class, () -> schema.writeSqlInserts(new StringBuilder(), "public..events", 1));
    }

    @Test
    @DisplayName("writeXml and writeSqlInserts propagate appendable failures")
    void writeXmlAndWriteSqlInsertsPropagateAppendableFailures() {
        Schema schema = new Schema(Map.of("value", ctx -> "x"));

        assertThrows(IOException.class, () -> schema.writeXml(new FailingAppendable(), 1));
        assertThrows(IOException.class, () -> schema.writeSqlInserts(new FailingAppendable(), "events", 1));
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
        assertEquals("", schema.toSqlInserts("events", 0));

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
    @DisplayName("internal StringBuilder wrapper converts impossible IOExceptions into IllegalStateException")
    void stringBuilderWrapperConvertsImpossibleIoExceptions() throws Exception {
        Class<?> writerType = Arrays.stream(Schema.class.getDeclaredClasses())
                                    .filter(candidate -> candidate.getSimpleName().equals("StringBuilderWriter"))
                                    .findFirst()
                                    .orElseThrow();

        Method method = Schema.class.getDeclaredMethod("buildString", writerType);
        method.setAccessible(true);

        Object writer = Proxy.newProxyInstance(
            writerType.getClassLoader(),
            new Class<?>[]{ writerType },
            (proxy, invoked, args) -> {
                throw new IOException("boom");
            });

        InvocationTargetException thrown = assertThrows(InvocationTargetException.class, () -> method.invoke(null, writer));
        IllegalStateException cause = (IllegalStateException) thrown.getCause();
        assertEquals("StringBuilder should not throw IOException", cause.getMessage());
        assertTrue(cause.getCause() instanceof IOException);
        assertEquals("boom", cause.getCause().getMessage());
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

    @Test
    @DisplayName("xml element-name validation covers blank, underscore, hyphen, and dot branches")
    void xmlElementNameValidationCoversHelperBranches() {
        assertFalse(invokeXmlElementNameValidation(null));
        assertFalse(invokeXmlElementNameValidation(""));
        assertTrue(invokeXmlElementNameValidation("_root"));
        assertTrue(invokeXmlElementNameValidation("a_b-c.d1"));
    }

    private static boolean invokeXmlElementNameValidation(String value) {
        try {
            Method method = Schema.class.getDeclaredMethod("isValidXmlElementName", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(null, value);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new AssertionError("Failed to access Schema XML name validation helper", e);
        } catch (InvocationTargetException e) {
            throw new AssertionError("Schema XML name validation helper threw unexpectedly", e.getCause());
        }
    }

    record NamedValue(String value) {
    }

    record ThrowingRecord(String value) {
        @Override
        public String value() {
            throw new IllegalStateException("accessor failure");
        }
    }

    static final class PlainValue {

        private final String value;

        private PlainValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "PlainValue[value=" + value + "]";
        }
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
