/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Declarative schema-based record generator.
 */
public final class Schema implements Generator<Map<String, Object>> {

    private static final String JSON_SCHEMA_DRAFT_2020_12 = "https://json-schema.org/draft/2020-12/schema";
    private static final String XML_DECLARATION           = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String DEFAULT_XML_ROOT_ELEMENT  = "records";
    private static final String DEFAULT_XML_RECORD_ELEMENT = "record";
    private static final char   NEWLINE                   = '\n';

    private final GeneratorConfig                  config;
    private final Map<String, SchemaValueProvider> fields;
    private final Random                           random;
    private       int                              nextRecordIndex;

    @FunctionalInterface
    interface StringBuilderWriter {

        void write(StringBuilder builder) throws IOException;
    }

    /**
     * Creates schema generator with default configuration.
     *
     * @param fields schema field mapping
     */
    public Schema(Map<String, SchemaValueProvider> fields) {
        this(GeneratorConfig.defaults(), fields);
    }

    /**
     * Creates schema generator for a specific locale.
     *
     * @param locale locale for generators
     * @param fields schema field mapping
     */
    public Schema(Locale locale, Map<String, SchemaValueProvider> fields) {
        this(GeneratorConfig.builder().locale(locale).build(), fields);
    }

    /**
     * Creates schema generator with explicit configuration.
     *
     * @param config generator config
     * @param fields schema field mapping
     */
    public Schema(GeneratorConfig config, Map<String, SchemaValueProvider> fields) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(fields, "fields must not be null");
        if (fields.isEmpty()) {
            throw new IllegalArgumentException("fields must not be empty");
        }
        this.fields = validateAndCopy(fields);
        this.random = config.createRandom();
        this.nextRecordIndex = 0;
    }

    private static Map<String, SchemaValueProvider> validateAndCopy(Map<String, SchemaValueProvider> fields) {
        Map<String, SchemaValueProvider> copy = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String name = entry.getKey();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("field names must be non-blank");
            }
            SchemaValueProvider provider = entry.getValue();
            if (provider == null) {
                throw new IllegalArgumentException("provider for field '" + name + "' must not be null");
            }
            copy.put(name, provider);
        }
        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<String, Object> generate() {
        return generateAtIndex(nextRecordIndex++);
    }

    /**
     * Generates a batch of schema records.
     *
     * @param count record count
     * @return generated record list
     */
    public List<Map<String, Object>> generateBatch(int count) {
        validateCount(count);
        List<Map<String, Object>> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            records.add(generate());
        }
        return records;
    }

    /**
     * Alias for {@link #generateBatch(int)}.
     *
     * @param iterations number of records
     * @return generated record list
     */
    public List<Map<String, Object>> loop(int iterations) {
        return generateBatch(iterations);
    }

    /**
     * Renders generated records as newline-delimited JSON.
     *
     * @param count record count
     * @return JSONL payload
     */
    public String toJsonLines(int count) {
        return buildString(builder -> writeJsonLines(builder, count));
    }

    /**
     * Writes generated records as newline-delimited JSON without materializing a batch list.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeJsonLines(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);
        for (int i = 0; i < count; i++) {
            appendJsonObject(out, generate());
            out.append(NEWLINE);
        }
    }

    /**
     * Renders generated records as CSV.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param count record count
     * @return CSV payload with a header row
     */
    public String toCsv(int count) {
        return buildString(builder -> writeCsv(builder, count));
    }

    /**
     * Writes generated records as CSV without materializing a batch list.
     *
     * <p>Nested objects, arrays, and lists are serialized into JSON cell values.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeCsv(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);

        List<String> columns = new ArrayList<>(fields.keySet());
        appendCsvRow(out, columns);
        out.append(NEWLINE);

        for (int i = 0; i < count; i++) {
            Map<String, Object> record = generate();
            List<String> row = new ArrayList<>(columns.size());
            for (String column : columns) {
                row.add(toCsvCell(record.get(column)));
            }
            appendCsvRow(out, row);
            out.append(NEWLINE);
        }
    }

    /**
     * Renders generated records as XML.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON text nodes.
     *
     * @param count record count
     * @return XML payload
     */
    public String toXml(int count) {
        return buildString(builder -> writeXml(builder, count));
    }

    /**
     * Writes generated records as XML without materializing a batch list.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON text nodes.
     *
     * @param out   appendable destination
     * @param count record count
     * @throws IOException if the appendable fails
     */
    public void writeXml(Appendable out, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);

        out.append(XML_DECLARATION).append(NEWLINE);
        if (count == 0) {
            out.append('<').append(DEFAULT_XML_ROOT_ELEMENT).append("/>").append(NEWLINE);
            return;
        }

        out.append('<').append(DEFAULT_XML_ROOT_ELEMENT).append('>').append(NEWLINE);
        for (int i = 0; i < count; i++) {
            Map<String, Object> record = generate();
            appendIndent(out, 2);
            out.append('<').append(DEFAULT_XML_RECORD_ELEMENT).append('>').append(NEWLINE);
            for (Map.Entry<String, Object> field : record.entrySet()) {
                appendXmlField(out, field.getKey(), field.getValue(), 4);
            }
            appendIndent(out, 2);
            out.append("</").append(DEFAULT_XML_RECORD_ELEMENT).append('>').append(NEWLINE);
        }
        out.append("</").append(DEFAULT_XML_ROOT_ELEMENT).append('>').append(NEWLINE);
    }

    /**
     * Renders generated records as SQL {@code INSERT} statements.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON string literals.
     *
     * @param tableName target table name
     * @param count     record count
     * @return SQL payload
     */
    public String toSqlInserts(String tableName, int count) {
        return buildString(builder -> writeSqlInserts(builder, tableName, count));
    }

    /**
     * Writes generated records as SQL {@code INSERT} statements without materializing a batch list.
     *
     * <p>Complex values such as maps, lists, and arrays are serialized into JSON string literals.
     *
     * @param out       appendable destination
     * @param tableName target table name
     * @param count     record count
     * @throws IOException if the appendable fails
     */
    public void writeSqlInserts(Appendable out, String tableName, int count) throws IOException {
        Objects.requireNonNull(out, "out must not be null");
        validateCount(count);

        List<String> columns = new ArrayList<>(fields.keySet());
        String normalizedTableName = requireSqlIdentifier(tableName, "tableName");
        for (int i = 0; i < count; i++) {
            Map<String, Object> record = generate();
            out.append("INSERT INTO ");
            appendSqlIdentifier(out, normalizedTableName);
            out.append(" (");
            for (int col = 0; col < columns.size(); col++) {
                if (col > 0) {
                    out.append(", ");
                }
                appendSqlIdentifier(out, columns.get(col));
            }
            out.append(") VALUES (");
            for (int col = 0; col < columns.size(); col++) {
                if (col > 0) {
                    out.append(", ");
                }
                appendSqlValue(out, record.get(columns.get(col)));
            }
            out.append(");").append(NEWLINE);
        }
    }

    /**
     * Returns configured locale.
     *
     * @return locale
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    /**
     * Returns immutable schema field mapping.
     *
     * @return schema fields
     */
    public Map<String, SchemaValueProvider> getFields() {
        return fields;
    }

    /**
     * Exports this schema definition to a JSON Schema-like map.
     *
     * <p>Types are read from provider metadata so exporting the schema does not consume
     * generator state. Providers without metadata export as unconstrained JSON Schema fragments.
     *
     * @return JSON Schema document as a nested map
     */
    public Map<String, Object> toJsonSchema() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("$schema", JSON_SCHEMA_DRAFT_2020_12);
        root.put("type", "object");
        root.put("additionalProperties", false);

        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>(fields.size());

        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String field = entry.getKey();
            required.add(field);
            try {
                properties.put(field, JsonSchemaSupport.copyJsonSchema(entry.getValue().jsonSchema()));
            } catch (RuntimeException ex) {
                throw new SchemaGenerationException(field, 0, ex);
            }
        }

        root.put("properties", properties);
        root.put("required", Collections.unmodifiableList(required));
        return Collections.unmodifiableMap(root);
    }

    private Map<String, Object> generateAtIndex(int recordIndex) {
        SchemaContext context = new SchemaContext(config.getLocale(), random, recordIndex);
        Map<String, Object> record = new LinkedHashMap<>(fields.size());
        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String name = entry.getKey();
            SchemaValueProvider provider = entry.getValue();
            try {
                record.put(name, provider.generate(context));
            } catch (RuntimeException ex) {
                throw new SchemaGenerationException(name, recordIndex, ex);
            }
        }
        return record;
    }

    private static void validateCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
    }

    private static void appendIndent(Appendable out, int spaces) throws IOException {
        for (int i = 0; i < spaces; i++) {
            out.append(' ');
        }
    }

    private static void appendXmlField(Appendable out, String fieldName, Object value, int indent) throws IOException {
        appendIndent(out, indent);
        String elementName = isValidXmlElementName(fieldName) ? fieldName : "field";
        boolean useNameAttribute = !elementName.equals(fieldName);

        out.append('<').append(elementName);
        if (useNameAttribute) {
            out.append(" name=\"");
            appendXmlAttribute(out, fieldName);
            out.append('"');
        }
        if (value == null) {
            out.append("/>").append(NEWLINE);
            return;
        }
        out.append('>');
        appendXmlValue(out, value);
        out.append("</").append(elementName).append('>').append(NEWLINE);
    }

    private static void appendXmlValue(Appendable out, Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value instanceof CharSequence
            || value instanceof Character
            || value instanceof Number
            || value instanceof Boolean) {
            appendXmlText(out, String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?>) {
            StringBuilder builder = new StringBuilder();
            appendJsonValue(builder, value);
            appendXmlText(out, builder.toString());
            return;
        }
        appendXmlText(out, String.valueOf(value));
    }

    private static void appendXmlText(Appendable out, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                default -> out.append(ch);
            }
        }
    }

    private static void appendXmlAttribute(Appendable out, String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> out.append(ch);
            }
        }
    }

    private static boolean isValidXmlElementName(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        char first = value.charAt(0);
        if (!(Character.isLetter(first) || first == '_')) {
            return false;
        }
        for (int i = 1; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '-' || ch == '.')) {
                return false;
            }
        }
        return true;
    }

    private static String requireSqlIdentifier(String value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        String[] parts = value.split("\\.", -1);
        for (String part : parts) {
            if (part.isBlank()) {
                throw new IllegalArgumentException(label + " must not contain blank identifier segments");
            }
        }
        return value;
    }

    private static void appendSqlIdentifier(Appendable out, String identifier) throws IOException {
        String[] parts = identifier.split("\\.", -1);
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                out.append('.');
            }
            out.append('"');
            for (int j = 0; j < parts[i].length(); j++) {
                char ch = parts[i].charAt(j);
                if (ch == '"') {
                    out.append("\"\"");
                } else {
                    out.append(ch);
                }
            }
            out.append('"');
        }
    }

    private static void appendSqlValue(Appendable out, Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value == null) {
            out.append("NULL");
            return;
        }
        if (value instanceof Number) {
            out.append(String.valueOf(value));
            return;
        }
        if (value instanceof Boolean bool) {
            out.append(bool ? "TRUE" : "FALSE");
            return;
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?>) {
            StringBuilder builder = new StringBuilder();
            appendJsonValue(builder, value);
            appendSqlStringLiteral(out, builder.toString());
            return;
        }
        appendSqlStringLiteral(out, String.valueOf(value));
    }

    private static void appendSqlStringLiteral(Appendable out, String value) throws IOException {
        out.append('\'');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\'') {
                out.append("''");
            } else {
                out.append(ch);
            }
        }
        out.append('\'');
    }

    private static void appendJsonValue(Appendable out, Object value) throws IOException {
        value = normalizeStructuredValue(value);
        if (value == null) {
            out.append("null");
            return;
        }
        if (value instanceof CharSequence text) {
            appendJsonString(out, text.toString());
            return;
        }
        if (value instanceof Character ch) {
            appendJsonString(out, String.valueOf(ch));
            return;
        }
        if (value instanceof Number || value instanceof Boolean) {
            out.append(String.valueOf(value));
            return;
        }
        if (value instanceof Map<?, ?> map) {
            appendJsonObject(out, map);
            return;
        }
        if (value instanceof Iterable<?> items) {
            appendJsonArray(out, items);
            return;
        }
        appendJsonString(out, String.valueOf(value));
    }

    private static void appendJsonObject(Appendable out, Map<?, ?> value) throws IOException {
        out.append('{');
        boolean first = true;
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            if (!first) {
                out.append(',');
            }
            appendJsonString(out, String.valueOf(entry.getKey()));
            out.append(':');
            appendJsonValue(out, entry.getValue());
            first = false;
        }
        out.append('}');
    }

    private static void appendJsonArray(Appendable out, Iterable<?> values) throws IOException {
        out.append('[');
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                out.append(',');
            }
            appendJsonValue(out, value);
            first = false;
        }
        out.append(']');
    }

    private static void appendJsonString(Appendable out, String value) throws IOException {
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        appendJsonUnicodeEscape(out, ch);
                    } else {
                        out.append(ch);
                    }
                }
            }
        }
        out.append('"');
    }

    private static void appendJsonUnicodeEscape(Appendable out, char ch) throws IOException {
        out.append("\\u");
        out.append(Character.forDigit((ch >> 12) & 0xF, 16));
        out.append(Character.forDigit((ch >> 8) & 0xF, 16));
        out.append(Character.forDigit((ch >> 4) & 0xF, 16));
        out.append(Character.forDigit(ch & 0xF, 16));
    }

    private static void appendCsvRow(Appendable out, List<String> values) throws IOException {
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                out.append(',');
            }
            appendCsvCell(out, values.get(i));
        }
    }

    private static void appendCsvCell(Appendable out, String value) throws IOException {
        if (!needsCsvQuoting(value)) {
            out.append(value);
            return;
        }
        out.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '"') {
                out.append("\"\"");
            } else {
                out.append(ch);
            }
        }
        out.append('"');
    }

    private static boolean needsCsvQuoting(String value) {
        return value.indexOf(',') >= 0
               || value.indexOf('"') >= 0
               || value.indexOf('\n') >= 0
               || value.indexOf('\r') >= 0
               || value.startsWith(" ")
               || value.endsWith(" ");
    }

    private static String toCsvCell(Object value) {
        Object normalized = normalizeStructuredValue(value);
        if (normalized == null) {
            return "";
        }
        if (normalized instanceof CharSequence
            || normalized instanceof Character
            || normalized instanceof Number
            || normalized instanceof Boolean) {
            return String.valueOf(normalized);
        }
        if (normalized instanceof Map<?, ?> || normalized instanceof Iterable<?>) {
            return buildString(builder -> appendJsonValue(builder, normalized));
        }
        return String.valueOf(normalized);
    }

    private static String buildString(StringBuilderWriter writer) {
        StringBuilder builder = new StringBuilder();
        try {
            writer.write(builder);
        } catch (IOException e) {
            throw new IllegalStateException("StringBuilder should not throw IOException", e);
        }
        return builder.toString();
    }

    private static Object normalizeStructuredValue(Object value) {
        if (value == null
            || value instanceof CharSequence
            || value instanceof Character
            || value instanceof Number
            || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> map) {
            Map<Object, Object> normalized = new LinkedHashMap<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                normalized.put(entry.getKey(), normalizeStructuredValue(entry.getValue()));
            }
            return Collections.unmodifiableMap(normalized);
        }
        if (value instanceof Iterable<?> items) {
            List<Object> normalized = new ArrayList<>();
            for (Object item : items) {
                normalized.add(normalizeStructuredValue(item));
            }
            return Collections.unmodifiableList(normalized);
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> normalized = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                normalized.add(normalizeStructuredValue(Array.get(value, i)));
            }
            return Collections.unmodifiableList(normalized);
        }
        if (value.getClass().isRecord()) {
            return recordToMap(value);
        }
        return value;
    }

    private static Map<String, Object> recordToMap(Object record) {
        RecordComponent[] components = record.getClass().getRecordComponents();
        Map<String, Object> values = new LinkedHashMap<>(components.length);
        for (RecordComponent component : components) {
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            try {
                values.put(component.getName(), normalizeStructuredValue(accessor.invoke(record)));
            } catch (ReflectiveOperationException ex) {
                throw new IllegalArgumentException(
                    "Failed to read record component '" + component.getName() + "' from "
                    + record.getClass().getName(), ex);
            }
        }
        return Collections.unmodifiableMap(values);
    }

}
