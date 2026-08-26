/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.base.RegexGenerator;
import io.github.frikit.krandom.generator.base.StringGenerator;
import io.github.frikit.krandom.generator.datetime.DateGenerator;
import io.github.frikit.krandom.generator.datetime.LocalDateTimeGenerator;
import io.github.frikit.krandom.generator.datetime.TimeGenerator;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;
import io.github.frikit.krandom.generator.network.HostnameGenerator;
import io.github.frikit.krandom.generator.network.IPv4Generator;
import io.github.frikit.krandom.generator.network.IPv6Generator;
import io.github.frikit.krandom.generator.network.URLGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parses JSON Schema (Draft 2020-12) or OpenAPI 3.x schema objects into krandom
 * {@link Schema} instances with field-appropriate generators.
 *
 * <p>Supports the following JSON Schema keywords:
 * <ul>
 *   <li>{@code type} — string, integer, number, boolean, array, object, null</li>
 *   <li>{@code format} — date, date-time, time, email, uri, uuid, ipv4, ipv6, hostname</li>
 *   <li>{@code enum} — random selection from enumerated values</li>
 *   <li>{@code const} — constant value</li>
 *   <li>{@code minimum}/{@code maximum} — bounded numeric generation</li>
 *   <li>{@code minLength}/{@code maxLength} — bounded string length</li>
 *   <li>{@code pattern} — regex-based string generation</li>
 *   <li>{@code items} — array element schema</li>
 *   <li>{@code properties} — nested object fields</li>
 *   <li>{@code nullable} (OpenAPI 3.0) — nullable field support</li>
 * </ul>
 *
 * <p>A type list may contain one concrete supported type plus {@code null}. Empty, malformed, and
 * unsupported type declarations, non-string formats, and empty or malformed enums are rejected
 * with their schema path. The parser accepts the control keywords emitted by
 * {@link Schema#toJsonSchema()}, including {@code $schema}, {@code required}, and
 * {@code additionalProperties}. References and schema composition ({@code $ref}, {@code allOf},
 * {@code anyOf}, {@code oneOf}, and related conditional keywords) are rejected with a schema path
 * because this parser has no resolver or composition semantics.
 *
 * <p>Field names are matched against krandom's semantic resolver for realistic data.
 * For example, a property named "email" with type "string" will produce email addresses.
 *
 * <pre>{@code
 *   Map<String, Object> jsonSchema = Map.of(
 *       "type", "object",
 *       "properties", Map.of(
 *           "name",  Map.of("type", "string"),
 *           "email", Map.of("type", "string", "format", "email"),
 *           "age",   Map.of("type", "integer", "minimum", 18, "maximum", 65)
 *       )
 *   );
 *
 *   Schema schema = SchemaParser.fromJsonSchema(jsonSchema);
 *   Map<String, Object> record = schema.generate();
 * }</pre>
 */
public final class SchemaParser {

    private static final Set<String> UNSUPPORTED_KEYWORDS = Set.of(
        "$dynamicRef", "$recursiveRef", "$ref",
        "allOf", "anyOf", "oneOf", "not",
        "if", "then", "else",
        "contains", "prefixItems", "unevaluatedItems",
        "dependentRequired", "dependentSchemas",
        "patternProperties", "propertyNames", "unevaluatedProperties");

    private static final Set<String> SUPPORTED_TYPES = Set.of(
        "string", "integer", "number", "boolean", "array", "object", "null");

    private SchemaParser() {
    }

    /**
     * Parses a JSON Schema object document into a {@link Schema} with default configuration.
     *
     * @param jsonSchema JSON Schema as a nested map (must have {@code "type": "object"})
     * @return schema generator backed by type-appropriate generators
     * @throws IllegalArgumentException if the schema is not an object type or is invalid
     */
    public static Schema fromJsonSchema(Map<String, Object> jsonSchema) {
        return fromJsonSchema(jsonSchema, GeneratorConfig.defaults());
    }

    /**
     * Parses a JSON Schema object document into a {@link Schema} with custom configuration.
     *
     * @param jsonSchema JSON Schema as a nested map (must have {@code "type": "object"})
     * @param config     generator configuration (seed, locale, etc.)
     * @return schema generator backed by type-appropriate generators
     * @throws IllegalArgumentException if the schema is not an object type or is invalid
     */
    public static Schema fromJsonSchema(Map<String, Object> jsonSchema, GeneratorConfig config) {
        Objects.requireNonNull(jsonSchema, "jsonSchema must not be null");
        Objects.requireNonNull(config, "config must not be null");

        Object type = jsonSchema.get("type");
        if (!"object".equals(type)) {
            throw new IllegalArgumentException(
                "Root JSON Schema must have \"type\": \"object\", got: " + type);
        }

        Object propertiesRaw = jsonSchema.get("properties");
        if (!(propertiesRaw instanceof Map<?, ?> propertiesMap)) {
            throw new IllegalArgumentException(
                "JSON Schema object must have a \"properties\" map");
        }

        validateSupportedKeywords(jsonSchema, "$",
                                  Collections.newSetFromMap(new IdentityHashMap<>()));

        Field field = new Field(config);
        Map<String, SchemaValueProvider> providers = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
            if (!(entry.getKey() instanceof String fieldName)) {
                throw new IllegalArgumentException("Property keys must be strings");
            }
            if (!(entry.getValue() instanceof Map<?, ?> fieldSchemaRaw)) {
                throw new IllegalArgumentException(
                    "Property '" + fieldName + "' must have a schema object");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fieldSchema = (Map<String, Object>) fieldSchemaRaw;
            boolean nullable = Boolean.TRUE.equals(fieldSchema.get("nullable"));

            SchemaValueProvider provider = resolveProvider(fieldName, fieldSchema, field, config);
            if (nullable) {
                SchemaValueProvider inner = provider;
                provider = ctx -> ctx.random().nextBoolean() ? null : inner.generate(ctx);
            }
            providers.put(fieldName, provider);
        }

        if (providers.isEmpty()) {
            throw new IllegalArgumentException("JSON Schema must have at least one property");
        }

        return new Schema(config, providers);
    }

    private static void validateSupportedKeywords(Map<?, ?> schema,
                                                  String path,
                                                  Set<Map<?, ?>> visiting) {
        if (!visiting.add(schema)) {
            throw new IllegalArgumentException("Recursive JSON Schema map is unsupported at " + path);
        }
        try {
            for (String keyword : UNSUPPORTED_KEYWORDS) {
                if (schema.containsKey(keyword)) {
                    throw new IllegalArgumentException(
                        "Unsupported JSON Schema keyword '" + keyword + "' at " + path);
                }
            }
            validateSupportedShape(schema, path);
            Object properties = schema.get("properties");
            if (properties instanceof Map<?, ?> nestedProperties) {
                for (Map.Entry<?, ?> entry : nestedProperties.entrySet()) {
                    if (entry.getKey() instanceof String name && entry.getValue() instanceof Map<?, ?> nestedSchema) {
                        validateSupportedKeywords(nestedSchema, path + ".properties." + name, visiting);
                    }
                }
            }
            Object items = schema.get("items");
            if (items instanceof Map<?, ?> itemSchema) {
                validateSupportedKeywords(itemSchema, path + ".items", visiting);
            }
        } finally {
            visiting.remove(schema);
        }
    }

    private static void validateSupportedShape(Map<?, ?> schema, String path) {
        if (schema.containsKey("enum")) {
            Object enumValues = schema.get("enum");
            if (!(enumValues instanceof List<?> values) || values.isEmpty()) {
                throw new IllegalArgumentException(
                    "JSON Schema enum must be a non-empty list at " + path);
            }
        }

        if (schema.containsKey("format") && !(schema.get("format") instanceof String)) {
            throw new IllegalArgumentException("JSON Schema format must be a string at " + path);
        }

        Object type = schema.get("type");
        if (type instanceof String namedType) {
            validateSupportedType(namedType, path);
        } else if (type instanceof List<?> types) {
            if (types.isEmpty()) {
                throw new IllegalArgumentException("JSON Schema type list must not be empty at " + path);
            }
            long concreteTypes = 0;
            for (Object item : types) {
                if (!(item instanceof String namedType)) {
                    throw new IllegalArgumentException("JSON Schema type entries must be strings at " + path);
                }
                validateSupportedType(namedType, path);
                if (!"null".equals(namedType)) {
                    concreteTypes++;
                }
            }
            if (concreteTypes > 1) {
                throw new IllegalArgumentException(
                    "JSON Schema type lists may contain one concrete type plus null at " + path);
            }
        } else if (type != null) {
            throw new IllegalArgumentException("JSON Schema type must be a string or list at " + path);
        }
    }

    private static void validateSupportedType(String type, String path) {
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new IllegalArgumentException("Unsupported JSON Schema type '" + type + "' at " + path);
        }
    }

    /**
     * Parses an OpenAPI 3.x schema component into a {@link Schema}.
     *
     * <p>This is an alias for {@link #fromJsonSchema(Map)} since OpenAPI schema objects
     * are a superset of JSON Schema.
     *
     * @param openApiSchema OpenAPI schema component
     * @return schema generator
     */
    public static Schema fromOpenApi(Map<String, Object> openApiSchema) {
        return fromJsonSchema(openApiSchema);
    }

    /**
     * Parses an OpenAPI 3.x schema component into a {@link Schema} with custom configuration.
     *
     * @param openApiSchema OpenAPI schema component
     * @param config        generator configuration
     * @return schema generator
     */
    public static Schema fromOpenApi(Map<String, Object> openApiSchema, GeneratorConfig config) {
        return fromJsonSchema(openApiSchema, config);
    }

    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveProvider(String fieldName,
                                                       Map<String, Object> fieldSchema,
                                                       Field field,
                                                       GeneratorConfig config) {
        // const takes absolute priority
        if (fieldSchema.containsKey("const")) {
            Object constValue = fieldSchema.get("const");
            return ctx -> constValue;
        }

        // enum: random selection
        if (fieldSchema.containsKey("enum")) {
            List<?> enumValues = (List<?>) fieldSchema.get("enum");
            List<Object> values = Collections.unmodifiableList(new ArrayList<>(enumValues));
            return ctx -> values.get(ctx.random().nextInt(values.size()));
        }

        String schemaType = typeOf(fieldSchema);
        String format = stringOrNull(fieldSchema.get("format"));

        // Try semantic resolution by field name first for string types
        if ("string".equals(schemaType) && format == null) {
            SchemaValueProvider semantic = trySemanticResolve(fieldName, field);
            if (semantic != null) {
                return semantic;
            }
        }

        return switch (schemaType) {
            case "string" -> resolveString(fieldName, fieldSchema, format, field, config);
            case "integer" -> resolveInteger(fieldSchema, config);
            case "number" -> resolveNumber(fieldSchema, config);
            case "boolean" -> resolveBooleanProvider(config);
            case "array" -> resolveArray(fieldName, fieldSchema, field, config);
            case "object" -> resolveNestedObject(fieldSchema, field, config);
            case "null" -> ctx -> null;
            default -> resolveUntyped(fieldName, field, config);
        };
    }

    private static SchemaValueProvider resolveString(String fieldName,
                                                      Map<String, Object> schema,
                                                      String format,
                                                      Field field,
                                                      GeneratorConfig config) {
        // pattern-based generation
        String pattern = stringOrNull(schema.get("pattern"));
        if (pattern != null) {
            var gen = new RegexGenerator(pattern);
            return ctx -> gen.generate();
        }

        // format-based generation
        if (format != null) {
            SchemaValueProvider formatProvider = resolveFormat(format, config);
            if (formatProvider != null) {
                return formatProvider;
            }
        }

        // Try semantic field name resolution
        SchemaValueProvider semantic = trySemanticResolve(fieldName, field);
        if (semantic != null) {
            return semantic;
        }

        // Bounded string length
        int minLength = intOrDefault(schema.get("minLength"), config.getMinStringLength());
        int maxLength = intOrDefault(schema.get("maxLength"), config.getMaxStringLength());
        StringGenerator.Builder builder = StringGenerator.builder()
                                                       .minLength(minLength)
                                                       .maxLength(maxLength);
        config.getSeed().ifPresent(builder::seed);
        var gen = builder.build();
        return ctx -> gen.generate();
    }

    private static SchemaValueProvider resolveFormat(String format, GeneratorConfig config) {
        return switch (format) {
            case "email" -> {
                var gen = Generators.ofEmail(config);
                yield ctx -> gen.generate();
            }
            case "uri", "url" -> {
                var gen = new URLGenerator(config);
                yield ctx -> gen.generate();
            }
            case "uuid" -> {
                var gen = new UUIDGenerator(config);
                yield ctx -> gen.generate().toString();
            }
            case "date" -> {
                var gen = new DateGenerator(config);
                yield ctx -> gen.generate().toString();
            }
            case "date-time" -> {
                var gen = new LocalDateTimeGenerator(config);
                yield ctx -> gen.generate().toString();
            }
            case "time" -> {
                var gen = new TimeGenerator(config);
                yield ctx -> gen.generate().toString();
            }
            case "ipv4" -> {
                var gen = new IPv4Generator(config);
                yield ctx -> gen.generate();
            }
            case "ipv6" -> {
                var gen = new IPv6Generator(config);
                yield ctx -> gen.generate();
            }
            case "hostname" -> {
                var gen = new HostnameGenerator(config);
                yield ctx -> gen.generate();
            }
            default -> null;
        };
    }

    private static SchemaValueProvider resolveInteger(Map<String, Object> schema,
                                                       GeneratorConfig config) {
        long min = longOrDefault(schema.get("minimum"), Integer.MIN_VALUE);
        long max = longOrDefault(schema.get("maximum"), Integer.MAX_VALUE);

        if (schema.containsKey("exclusiveMinimum")) {
            min = longOrDefault(schema.get("exclusiveMinimum"), min) + 1;
        }
        if (schema.containsKey("exclusiveMaximum")) {
            max = longOrDefault(schema.get("exclusiveMaximum"), max) - 1;
        }

        // Use int range if values fit; guard against overflow on max + 1
        if (min >= Integer.MIN_VALUE && max < Integer.MAX_VALUE) {
            var gen = Generators.ofInt((int) min, (int) max + 1);
            return ctx -> gen.generate();
        }

        // For long range, guard against overflow on max + 1
        if (max < Long.MAX_VALUE) {
            var gen = Generators.ofLong(min, max + 1);
            return ctx -> gen.generate();
        }

        var gen = Generators.ofLong(min, max);
        return ctx -> gen.generate();
    }

    private static SchemaValueProvider resolveNumber(Map<String, Object> schema,
                                                      GeneratorConfig config) {
        double min = doubleOrDefault(schema.get("minimum"), -1_000_000.0);
        double max = doubleOrDefault(schema.get("maximum"), 1_000_000.0);

        if (schema.containsKey("exclusiveMinimum")) {
            min = doubleOrDefault(schema.get("exclusiveMinimum"), min)
                  + Double.MIN_VALUE;
        }
        if (schema.containsKey("exclusiveMaximum")) {
            max = doubleOrDefault(schema.get("exclusiveMaximum"), max)
                  - Double.MIN_VALUE;
        }

        var gen = Generators.ofDouble(min, max);
        return ctx -> gen.generate();
    }

    private static SchemaValueProvider resolveBooleanProvider(GeneratorConfig config) {
        var gen = Generators.ofBoolean();
        return ctx -> gen.generate();
    }

    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveArray(String fieldName,
                                                     Map<String, Object> schema,
                                                     Field field,
                                                     GeneratorConfig config) {
        int minItems = intOrDefault(schema.get("minItems"), config.getMinCollectionSize());
        int maxItems = intOrDefault(schema.get("maxItems"), config.getMaxCollectionSize());

        SchemaValueProvider itemProvider;
        Object itemsRaw = schema.get("items");
        if (itemsRaw instanceof Map<?, ?> itemSchema) {
            itemProvider = resolveProvider(
                fieldName + "[]", (Map<String, Object>) itemSchema, field, config);
        } else {
            var gen = Generators.ofString();
            itemProvider = ctx -> gen.generate();
        }

        SchemaValueProvider items = itemProvider;
        return ctx -> {
            int size = minItems == maxItems
                       ? minItems
                       : minItems + ctx.random().nextInt(maxItems - minItems + 1);
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(items.generate(ctx));
            }
            return list;
        };
    }

    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveNestedObject(Map<String, Object> schema,
                                                            Field field,
                                                            GeneratorConfig config) {
        Object propertiesRaw = schema.get("properties");
        if (!(propertiesRaw instanceof Map<?, ?> propertiesMap) || propertiesMap.isEmpty()) {
            return ctx -> Collections.emptyMap();
        }

        Map<String, SchemaValueProvider> nestedProviders = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
            if (entry.getKey() instanceof String name && entry.getValue() instanceof Map<?, ?> propSchema) {
                nestedProviders.put(name,
                    resolveProvider(name, (Map<String, Object>) propSchema, field, config));
            }
        }

        return ctx -> {
            Map<String, Object> nested = new LinkedHashMap<>(nestedProviders.size());
            for (Map.Entry<String, SchemaValueProvider> entry : nestedProviders.entrySet()) {
                nested.put(entry.getKey(), entry.getValue().generate(ctx));
            }
            return nested;
        };
    }

    private static SchemaValueProvider resolveUntyped(String fieldName,
                                                       Field field,
                                                       GeneratorConfig config) {
        SchemaValueProvider semantic = trySemanticResolve(fieldName, field);
        if (semantic != null) {
            return semantic;
        }
        var gen = Generators.ofString();
        return ctx -> gen.generate();
    }

    private static SchemaValueProvider trySemanticResolve(String fieldName, Field field) {
        // Normalize field name to potential schema reference
        String normalized = normalizeFieldName(fieldName);

        // Try direct reference resolution
        if (field.hasReference(normalized)) {
            return field.bind(normalized);
        }

        // Try common semantic mappings
        String semanticRef = semanticReference(normalized);
        if (semanticRef != null && field.hasReference(semanticRef)) {
            return field.bind(semanticRef);
        }

        return null;
    }

    private static String normalizeFieldName(String fieldName) {
        // Convert camelCase and snake_case to dot-notation for Field.bind()
        return fieldName
            .replaceAll("([a-z])([A-Z])", "$1_$2")
            .toLowerCase()
            .replace('_', '.');
    }

    private static String semanticReference(String normalized) {
        // Only mappings whose normalized forms are NOT already registered as
        // FieldLookup aliases belong here. Aliases like "city", "state",
        // "country", "domain", "username", "email", "name", "url", "uuid"
        // are resolved earlier by field.bind() and never reach this method.
        return switch (normalized) {
            case "first.name" -> "person.first_name";
            case "last.name" -> "person.last_name";
            case "full.name" -> "person.full_name";
            case "email.address" -> "person.email";
            case "phone", "phone.number" -> "person.telephone";
            case "age" -> "person.age";
            case "zip", "zip.code", "postal.code" -> "address.postal_code";
            case "street", "street.address" -> "address.street_address";
            case "latitude", "lat" -> "address.latitude";
            case "longitude", "lng", "lon" -> "address.longitude";
            case "company", "company.name" -> "finance.company_name";
            case "website" -> "internet.url";
            case "ip", "ip.address" -> "internet.ip_v4";
            case "id" -> "cryptographic.uuid";
            default -> null;
        };
    }

    private static String typeOf(Map<String, Object> schema) {
        Object type = schema.get("type");
        if (type instanceof String s) {
            return s;
        }
        if (type instanceof List<?> types) {
            // Pick first non-null type
            for (Object t : types) {
                String namedType = (String) t;
                if (!"null".equals(namedType)) {
                    return namedType;
                }
            }
            return "null";
        }
        return "";
    }

    private static String stringOrNull(Object value) {
        return value instanceof String s ? s : null;
    }

    private static int intOrDefault(Object value, int defaultValue) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return defaultValue;
    }

    private static long longOrDefault(Object value, long defaultValue) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        return defaultValue;
    }

    private static double doubleOrDefault(Object value, double defaultValue) {
        if (value instanceof Number n) {
            return n.doubleValue();
        }
        return defaultValue;
    }
}
