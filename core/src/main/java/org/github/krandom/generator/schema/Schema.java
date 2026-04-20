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
import java.math.BigInteger;
import java.security.SecureRandom;
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
    private static final char   NEWLINE                   = '\n';

    private final GeneratorConfig                  config;
    private final Map<String, SchemaValueProvider> fields;
    private final Random                           random;
    private       int                              nextRecordIndex;

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
        StringBuilder builder = new StringBuilder();
        try {
            writeJsonLines(builder, count);
        } catch (IOException e) {
            throw new IllegalStateException("StringBuilder should not throw IOException", e);
        }
        return builder.toString();
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
        StringBuilder builder = new StringBuilder();
        try {
            writeCsv(builder, count);
        } catch (IOException e) {
            throw new IllegalStateException("StringBuilder should not throw IOException", e);
        }
        return builder.toString();
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
     * <p>Types are inferred from a representative provider sample for each field.
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
        SchemaContext sampleContext = new SchemaContext(config.getLocale(), new Random(0L), 0);

        for (Map.Entry<String, SchemaValueProvider> entry : fields.entrySet()) {
            String field = entry.getKey();
            required.add(field);
            try {
                Object sample = entry.getValue().generate(sampleContext);
                properties.put(field, inferJsonSchema(sample));
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

    private static void appendJsonValue(Appendable out, Object value) throws IOException {
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
        if (value.getClass().isArray()) {
            appendJsonArray(out, value);
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

    private static void appendJsonArray(Appendable out, Object array) throws IOException {
        out.append('[');
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                out.append(',');
            }
            appendJsonValue(out, Array.get(array, i));
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
        if (value == null) {
            return "";
        }
        if (value instanceof CharSequence
            || value instanceof Character
            || value instanceof Number
            || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray()) {
            StringBuilder builder = new StringBuilder();
            try {
                appendJsonValue(builder, value);
            } catch (IOException e) {
                throw new IllegalStateException("StringBuilder should not throw IOException", e);
            }
            return builder.toString();
        }
        return String.valueOf(value);
    }

    private static Map<String, Object> inferJsonSchema(Object value) {
        if (value == null) {
            return Map.of("type", "null");
        }
        if (value instanceof String) {
            return Map.of("type", "string");
        }
        if (value instanceof Boolean) {
            return Map.of("type", "boolean");
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
            return Map.of("type", "integer");
        }
        if (value instanceof Number) {
            return Map.of("type", "number");
        }
        if (value instanceof List<?> list) {
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "array");
            Object firstNonNull = null;
            for (Object item : list) {
                if (item != null) {
                    firstNonNull = item;
                    break;
                }
            }
            schema.put("items", firstNonNull == null ? Map.of("type", "null") : inferJsonSchema(firstNonNull));
            return Collections.unmodifiableMap(schema);
        }
        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Object> nestedProperties = new LinkedHashMap<>();
            List<String> required = new ArrayList<>();
            for (Map.Entry<?, ?> nested : nestedMap.entrySet()) {
                if (nested.getKey() instanceof String key) {
                    required.add(key);
                    nestedProperties.put(key, inferJsonSchema(nested.getValue()));
                }
            }
            Map<String, Object> schema = new LinkedHashMap<>();
            schema.put("type", "object");
            schema.put("additionalProperties", false);
            schema.put("properties", Collections.unmodifiableMap(nestedProperties));
            schema.put("required", Collections.unmodifiableList(required));
            return Collections.unmodifiableMap(schema);
        }
        return Map.of("type", "string");
    }
}
