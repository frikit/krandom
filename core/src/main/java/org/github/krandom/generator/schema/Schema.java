/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

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
        return Map.copyOf(copy);
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
        if (count < 0) {
            throw new IllegalArgumentException("count must be >= 0, got: " + count);
        }
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
