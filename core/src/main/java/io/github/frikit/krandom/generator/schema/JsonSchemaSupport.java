/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Internal helpers for building JSON Schema fragments without generating data.
 */
final class JsonSchemaSupport {

    private JsonSchemaSupport() {
    }

    static Map<String, Object> any() {
        return Map.of();
    }

    static Map<String, Object> string() {
        return Map.of("type", "string");
    }

    static Map<String, Object> stringFormat(String format) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "string");
        schema.put("format", format);
        return Collections.unmodifiableMap(schema);
    }

    static Map<String, Object> integer() {
        return Map.of("type", "integer");
    }

    static Map<String, Object> number() {
        return Map.of("type", "number");
    }

    static Map<String, Object> bool() {
        return Map.of("type", "boolean");
    }

    static Map<String, Object> nullType() {
        return Map.of("type", "null");
    }

    static Map<String, Object> array(Map<String, ?> itemSchema) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "array");
        schema.put("items", copyJsonSchema(itemSchema));
        return Collections.unmodifiableMap(schema);
    }

    static Map<String, Object> object(Map<String, ? extends Map<String, ?>> properties) {
        Map<String, Object> copiedProperties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>(properties.size());
        for (Map.Entry<String, ? extends Map<String, ?>> entry : properties.entrySet()) {
            required.add(entry.getKey());
            copiedProperties.put(entry.getKey(), copyJsonSchema(entry.getValue()));
        }
        return objectWithRequired(copiedProperties, required);
    }

    static Map<String, Object> record(Class<?> recordType) {
        return record(recordType, Set.of());
    }

    static Map<String, Object> record(Class<?> recordType, Set<String> nullableComponents) {
        Set<String> nullable = Set.copyOf(Objects.requireNonNull(nullableComponents, "nullableComponents must not be null"));
        if (!recordType.isRecord()) {
            throw new IllegalArgumentException("recordType must be a record class: " + recordType.getName());
        }
        Map<String, Object> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (RecordComponent component : recordType.getRecordComponents()) {
            String name = component.getName();
            Map<String, Object> componentSchema = fromType(component.getType());
            required.add(name);
            properties.put(name, nullable.contains(name) ? nullable(componentSchema) : componentSchema);
        }
        return objectWithRequired(properties, required);
    }

    static Map<String, Object> nullable(Map<String, ?> jsonSchema) {
        Map<String, Object> copy = copyJsonSchema(jsonSchema);
        if (copy.isEmpty()) {
            return any();
        }

        Object type = copy.get("type");
        Map<String, Object> nullable = new LinkedHashMap<>(copy);
        if (type instanceof String singleType) {
            nullable.put("type", List.of(singleType, "null"));
            return Collections.unmodifiableMap(nullable);
        }
        if (type instanceof Iterable<?> types) {
            Set<Object> combinedTypes = new LinkedHashSet<>();
            for (Object item : types) {
                combinedTypes.add(item);
            }
            combinedTypes.add("null");
            nullable.put("type", Collections.unmodifiableList(new ArrayList<>(combinedTypes)));
            return Collections.unmodifiableMap(nullable);
        }

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("oneOf", List.of(copy, nullType()));
        return Collections.unmodifiableMap(schema);
    }

    static Map<String, Object> infer(Object value) {
        value = normalizeStructuredValue(value);
        if (value == null) {
            return nullType();
        }
        if (value instanceof String || value instanceof Character) {
            return string();
        }
        if (value instanceof Boolean) {
            return bool();
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long || value instanceof BigInteger) {
            return integer();
        }
        if (value instanceof Number) {
            return number();
        }
        if (value instanceof List<?> list) {
            Object firstNonNull = null;
            for (Object item : list) {
                if (item != null) {
                    firstNonNull = item;
                    break;
                }
            }
            return array(firstNonNull == null ? nullType() : infer(firstNonNull));
        }
        if (value instanceof Map<?, ?> nestedMap) {
            Map<String, Map<String, Object>> nestedProperties = new LinkedHashMap<>();
            for (Map.Entry<?, ?> nested : nestedMap.entrySet()) {
                if (nested.getKey() instanceof String key) {
                    nestedProperties.put(key, infer(nested.getValue()));
                }
            }
            return object(nestedProperties);
        }
        return string();
    }

    static Map<String, Object> copyJsonSchema(Map<String, ?> jsonSchema) {
        if (jsonSchema.isEmpty()) {
            return any();
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : jsonSchema.entrySet()) {
            copy.put(entry.getKey(), copyJsonSchemaValue(entry.getValue()));
        }
        return Collections.unmodifiableMap(copy);
    }

    private static Object copyJsonSchemaValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!(entry.getKey() instanceof String key)) {
                    throw new IllegalArgumentException("jsonSchema map keys must be strings");
                }
                nested.put(key, copyJsonSchemaValue(entry.getValue()));
            }
            return Collections.unmodifiableMap(nested);
        }
        if (value instanceof Iterable<?> values) {
            List<Object> copied = new ArrayList<>();
            for (Object item : values) {
                copied.add(copyJsonSchemaValue(item));
            }
            return Collections.unmodifiableList(copied);
        }
        return value;
    }

    private static Map<String, Object> objectWithRequired(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", Collections.unmodifiableMap(new LinkedHashMap<>(properties)));
        schema.put("required", Collections.unmodifiableList(new ArrayList<>(required)));
        return Collections.unmodifiableMap(schema);
    }

    private static Map<String, Object> fromType(Class<?> type) {
        if (type == String.class || type == Character.class || type == char.class) {
            return string();
        }
        if (type == boolean.class || type == Boolean.class) {
            return bool();
        }
        if (type == byte.class || type == short.class || type == int.class || type == long.class
            || type == Byte.class || type == Short.class || type == Integer.class || type == Long.class
            || type == BigInteger.class) {
            return integer();
        }
        if (type == float.class || type == double.class || Number.class.isAssignableFrom(type)) {
            return number();
        }
        if (type == LocalDate.class) {
            return stringFormat("date");
        }
        if (type == LocalTime.class) {
            return stringFormat("time");
        }
        if (type == LocalDateTime.class || type == OffsetDateTime.class || type == ZonedDateTime.class) {
            return stringFormat("date-time");
        }
        if (type.isArray()) {
            return array(fromType(type.getComponentType()));
        }
        if (type.isRecord()) {
            return record(type);
        }
        return any();
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
