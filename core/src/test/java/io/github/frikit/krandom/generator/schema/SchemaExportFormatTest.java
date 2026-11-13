/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Exported documents must stay valid for their format's parsers, whatever text the fields hold.
 */
class SchemaExportFormatTest {

    /** U+1F600, a supplementary character, which UTF-16 writes as a surrogate pair. */
    private static final String PAIR = "\uD83D\uDE00";

    /** A table name whose parts end in a lone high and a lone low surrogate. */
    private static final String TABLE = "audit\uD800.events\uDC00";

    private static Schema schemaOf(String fieldName, Object value) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put(fieldName, ctx -> value);
        return new Schema(fields);
    }

    @Test
    @DisplayName("XML stays well-formed for characters XML 1.0 cannot represent")
    void xmlStaysWellFormed() throws Exception {
        String value = "a\u0001b\u0000c￾￿d\uD800e\uDC00f😀g\r\n\t\"'&<>h\uD800";
        String name = "bad name \"q\" & 's'\n\t\r";
        String xml = schemaOf(name, value).toXml(1);

        Document document =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        Element field = (Element) document.getElementsByTagName("field").item(0);
        assertEquals(name, field.getAttribute("name"));
        assertEquals("a�b�c��d�e�f😀g\r\n\t\"'&<>h�", field.getTextContent());
    }

    /**
     * Text with each character that YAML cannot hold raw in a double-quoted scalar, beside its neighbours that
     * it can: DEL and the C1 controls, among them U+0085, which YAML 1.1 reads as a line break; U+2028 and
     * U+2029, the line and paragraph separators of YAML 1.1; and the noncharacters U+FFFE and U+FFFF.
     */
    private static final String YAML_UNPRINTABLE =
            "~\u007f\u0080\u0085\u009f\u00a0\u2027\u2028\u2029\u202a\ufffd\ufffe\uffff";

    /** What YAML writes for that text: an escape for each character it cannot hold raw. */
    private static final String YAML_UNPRINTABLE_ESCAPED =
            "~\\u007f\\u0080\\u0085\\u009f\u00a0\u2027\\u2028\\u2029\u202a\ufffd\\ufffe\\uffff";

    @Test
    @DisplayName("YAML escapes DEL, C1 controls, U+2028, U+2029, U+FFFE and U+FFFF, in names and values")
    void yamlEscapesCharactersItCannotHoldRaw() throws Exception {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("k" + YAML_UNPRINTABLE, YAML_UNPRINTABLE);
        record.put("char", '\u0085');
        record.put("nested", Map.of("n" + YAML_UNPRINTABLE, List.of(YAML_UNPRINTABLE)));
        record.put("other", new StringBuilder("\u2028"));
        record.put("number", 1);
        record.put("none", null);
        record.put("object", new Object() {
            @Override
            public String toString() {
                return "\u0085";
            }
        });
        String expectedYaml = "- \"k" + YAML_UNPRINTABLE_ESCAPED + "\": \"" + YAML_UNPRINTABLE_ESCAPED + "\"\n"
                + "  char: \"\\u0085\"\n"
                + "  nested:\n"
                + "    \"n" + YAML_UNPRINTABLE_ESCAPED + "\":\n"
                + "      - \"" + YAML_UNPRINTABLE_ESCAPED + "\"\n"
                + "  other: \"\\u2028\"\n"
                + "  number: 1\n"
                + "  none: null\n"
                + "  object: \"\\u0085\"\n";
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        record.forEach((name, value) -> fields.put(name, ctx -> value));
        Map<String, Function<Integer, ?>> projected = new LinkedHashMap<>();
        record.forEach((name, value) -> projected.put(name, ignored -> value));

        assertEquals(expectedYaml, new Schema(fields).toYaml(1));
        assertEquals(expectedYaml, SchemaProjection.of(projected).toYaml(List.of(1)));
        // JSON and TOML hold these characters raw, and keep writing them so.
        assertEquals(
                "{\"k\":\"" + YAML_UNPRINTABLE + "\"}\n",
                schemaOf("k", YAML_UNPRINTABLE).toJsonLines(1));
        assertEquals(
                "[[records]]\nk = \"~\\u007f" + YAML_UNPRINTABLE.substring(2) + "\"\n",
                schemaOf("k", YAML_UNPRINTABLE).toToml(1));
    }

    @Test
    @DisplayName("SQL quotes a column name with dots as one identifier and keeps qualified table names")
    void sqlQuotesColumnNamesAsSingleIdentifiers() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("customer.name", ctx -> "Ada");
        fields.put("say \"hi\"", ctx -> 1);
        String sql = new Schema(fields).toSqlInserts("crm.customers", 1);
        assertTrue(sql.startsWith("INSERT INTO \"crm\".\"customers\" (\"customer.name\", \"say \"\"hi\"\"\")"), sql);
    }

    @Test
    @DisplayName("TOML quotes keys outside the ASCII bare-key alphabet")
    void tomlQuotesNonAsciiKeys() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("prénom", ctx -> "Zoë");
        fields.put("plain_key-1", ctx -> 1);
        String toml = new Schema(fields).toToml(1);
        assertTrue(toml.contains("\"prénom\" = "), toml);
        assertTrue(toml.contains("\nplain_key-1 = "), toml);
    }

    /**
     * A record whose names and values hold a lone high surrogate, at the end of a name and inside a value, a
     * lone low one, a pair in reverse, a pair, and a nested key, string and character with one.
     */
    private static Map<String, Object> surrogateRecord() {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("high\uD800", "a\uD800b");
        record.put("low\uDC00", "a\uDC00b");
        record.put("reversed\uDC00\uD800", "a\uDC00\uD800b");
        record.put("pair" + PAIR, "a" + PAIR + "b");
        record.put("end", "z\uD800");
        record.put("nested", Map.of("k\uDBFF", List.of("\uDC00", '\uD800')));
        return record;
    }

    /** What each format writes for that record: U+FFFD for each unpaired surrogate, and the pair as it is. */
    private static String expectedSurrogateOutput(OutputFormat format) {
        String object = "{\"high\uFFFD\":\"a\uFFFDb\",\"low\uFFFD\":\"a\uFFFDb\","
                + "\"reversed\uFFFD\uFFFD\":\"a\uFFFD\uFFFDb\",\"pair" + PAIR + "\":\"a" + PAIR + "b\","
                + "\"end\":\"z\uFFFD\",\"nested\":{\"k\uFFFD\":[\"\uFFFD\",\"\uFFFD\"]}}";
        return switch (format) {
            case JSONL -> object + "\n";
            case JSON -> "[" + object + "]\n";
            case CSV ->
                "high\uFFFD,low\uFFFD,reversed\uFFFD\uFFFD,pair" + PAIR + ",end,nested\n"
                        + "a\uFFFDb,a\uFFFDb,a\uFFFD\uFFFDb,a" + PAIR + "b,z\uFFFD,"
                        + "\"{\"\"k\uFFFD\"\":[\"\"\uFFFD\"\",\"\"\uFFFD\"\"]}\"\n";
            case XML ->
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                        + "<records>\n"
                        + "  <record>\n"
                        + "    <field name=\"high\uFFFD\">a\uFFFDb</field>\n"
                        + "    <field name=\"low\uFFFD\">a\uFFFDb</field>\n"
                        + "    <field name=\"reversed\uFFFD\uFFFD\">a\uFFFD\uFFFDb</field>\n"
                        + "    <field name=\"pair" + PAIR + "\">a" + PAIR + "b</field>\n"
                        + "    <end>z\uFFFD</end>\n"
                        + "    <nested>{\"k\uFFFD\":[\"\uFFFD\",\"\uFFFD\"]}</nested>\n"
                        + "  </record>\n"
                        + "</records>\n";
            case SQL ->
                "INSERT INTO \"audit\uFFFD\".\"events\uFFFD\" (\"high\uFFFD\", \"low\uFFFD\", "
                        + "\"reversed\uFFFD\uFFFD\", \"pair" + PAIR + "\", \"end\", \"nested\") VALUES ("
                        + "'a\uFFFDb', 'a\uFFFDb', 'a\uFFFD\uFFFDb', 'a" + PAIR + "b', 'z\uFFFD', "
                        + "'{\"k\uFFFD\":[\"\uFFFD\",\"\uFFFD\"]}');\n";
            case YAML ->
                "- \"high\uFFFD\": \"a\uFFFDb\"\n"
                        + "  \"low\uFFFD\": \"a\uFFFDb\"\n"
                        + "  \"reversed\uFFFD\uFFFD\": \"a\uFFFD\uFFFDb\"\n"
                        + "  \"pair" + PAIR + "\": \"a" + PAIR + "b\"\n"
                        + "  end: \"z\uFFFD\"\n"
                        + "  nested:\n"
                        + "    \"k\uFFFD\":\n"
                        + "      - \"\uFFFD\"\n"
                        + "      - \"\uFFFD\"\n";
            case TOML ->
                "[[records]]\n"
                        + "\"high\uFFFD\" = \"a\uFFFDb\"\n"
                        + "\"low\uFFFD\" = \"a\uFFFDb\"\n"
                        + "\"reversed\uFFFD\uFFFD\" = \"a\uFFFD\uFFFDb\"\n"
                        + "\"pair" + PAIR + "\" = \"a" + PAIR + "b\"\n"
                        + "end = \"z\uFFFD\"\n"
                        + "nested = { \"k\uFFFD\" = [\"\uFFFD\", \"\uFFFD\"] }\n";
        };
    }

    private static Schema surrogateSchema() {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        surrogateRecord().forEach((name, value) -> fields.put(name, ctx -> value));
        return new Schema(fields);
    }

    private static SchemaProjection<Integer> surrogateProjection() {
        Map<String, Function<Integer, ?>> fields = new LinkedHashMap<>();
        surrogateRecord().forEach((name, value) -> fields.put(name, ignored -> value));
        return SchemaProjection.of(fields);
    }

    /** A schema's text in a format, from the method that returns it as a string. */
    private static String schemaText(OutputFormat format) {
        Schema schema = surrogateSchema();
        return switch (format) {
            case JSONL -> schema.toJsonLines(1);
            case JSON -> schema.toJson(1);
            case CSV -> schema.toCsv(1);
            case XML -> schema.toXml(1);
            case SQL -> schema.toSqlInserts(TABLE, 1);
            case YAML -> schema.toYaml(1);
            case TOML -> schema.toToml(1);
        };
    }

    /** A projection's text in a format, from the method that returns it as a string. */
    private static String projectionText(OutputFormat format) {
        SchemaProjection<Integer> projection = surrogateProjection();
        List<Integer> one = List.of(1);
        return switch (format) {
            case JSONL -> projection.toJsonLines(one);
            case JSON -> projection.toJson(one);
            case CSV -> projection.toCsv(one);
            case XML -> projection.toXml(one);
            case SQL -> projection.toSqlInserts(TABLE, one);
            case YAML -> projection.toYaml(one);
            case TOML -> projection.toToml(one);
        };
    }

    @Test
    @DisplayName("every format writes an unpaired surrogate as U+FFFD and a pair as it is, in names and values")
    void everyFormatWritesAnUnpairedSurrogateAsTheReplacementCharacter() throws Exception {
        for (OutputFormat format : OutputFormat.values()) {
            String expected = expectedSurrogateOutput(format);

            assertEquals(expected, encodedStrictly(schemaText(format)), format.name());
            assertEquals(expected, encodedStrictly(projectionText(format)), format.name());

            ByteArrayOutputStream schemaBytes = new ByteArrayOutputStream();
            surrogateSchema().writeTo(schemaBytes, format, 1, TABLE);
            assertEquals(expected, decodedStrictly(schemaBytes.toByteArray()), format.name());

            ByteArrayOutputStream projectionBytes = new ByteArrayOutputStream();
            surrogateProjection().writeTo(projectionBytes, format, List.of(1), TABLE);
            assertEquals(expected, decodedStrictly(projectionBytes.toByteArray()), format.name());
        }
    }

    @Test
    @DisplayName("XML written to a stream reads back with U+FFFD for each unpaired surrogate")
    void xmlWithSurrogatesReadsBack() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        surrogateSchema().writeTo(bytes, OutputFormat.XML, 1, TABLE);

        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new ByteArrayInputStream(bytes.toByteArray()));
        NodeList fields = document.getElementsByTagName("field");
        Map<String, String> read = new LinkedHashMap<>();
        for (int i = 0; i < fields.getLength(); i++) {
            Element field = (Element) fields.item(i);
            read.put(field.getAttribute("name"), field.getTextContent());
        }
        read.put("end", document.getElementsByTagName("end").item(0).getTextContent());

        assertEquals(
                Map.of(
                        "high\uFFFD",
                        "a\uFFFDb",
                        "low\uFFFD",
                        "a\uFFFDb",
                        "reversed\uFFFD\uFFFD",
                        "a\uFFFD\uFFFDb",
                        "pair" + PAIR,
                        "a" + PAIR + "b",
                        "end",
                        "z\uFFFD"),
                read);
    }

    /** The text, after checking that a strict UTF-8 encoder, which rejects an unpaired surrogate, takes it. */
    private static String encodedStrictly(String text) throws CharacterCodingException {
        StandardCharsets.UTF_8
                .newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .encode(CharBuffer.wrap(text));
        return text;
    }

    /** UTF-8 bytes read by a strict decoder, so that bytes UTF-8 does not allow fail rather than read as U+FFFD. */
    private static String decodedStrictly(byte[] bytes) throws CharacterCodingException {
        return StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }
}
