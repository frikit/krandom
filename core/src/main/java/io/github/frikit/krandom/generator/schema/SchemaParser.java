/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.Generator;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * Parses JSON Schema (Draft 2020-12) or OpenAPI 3.x schema objects into krandom
 * {@link Schema} instances with field-appropriate generators.
 *
 * <p>Supports the following JSON Schema keywords:
 * <ul>
 *   <li>{@code type} — string, integer, number, boolean, array, object, null</li>
 *   <li>{@code format} — date, date-time, time, email, idn-email, uri, uuid, ipv4, ipv6, hostname</li>
 *   <li>{@code enum} — random selection from enumerated values</li>
 *   <li>{@code const} — constant value</li>
 *   <li>{@code minimum}/{@code maximum}/{@code exclusiveMinimum}/{@code exclusiveMaximum} — bounded
 *       numeric generation, including OpenAPI 3.0's boolean exclusive flags</li>
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
 * <p>Constraints apply together, and a keyword constrains only values of its own JSON type. A
 * {@code const} or {@code enum} value must meet the other constraints, and an {@code enum} keeps only
 * the values that do; {@code format} is not one of them, because it is an annotation in Draft 2020-12.
 * Overlapping numeric bounds intersect. A {@code pattern} or {@code format} value is drawn again until
 * it fits {@code minLength} and {@code maxLength}, up to a fixed number of attempts. A combination that
 * leaves no value is rejected with its schema path, while parsing where that can be told. A combination
 * of length bounds with a {@code pattern} or {@code format} is judged by sampling rather than analysis,
 * so one that rarely fits can also be rejected, and can fail on a later record. A {@code pattern} that
 * {@link RegexGenerator} cannot generate from is rejected with its schema path, unless a {@code const} or
 * {@code enum} beside it names the values, as those are only checked against it.
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
            "$dynamicRef",
            "$recursiveRef",
            "$ref",
            "allOf",
            "anyOf",
            "oneOf",
            "not",
            "if",
            "then",
            "else",
            "contains",
            "prefixItems",
            "unevaluatedItems",
            "dependentRequired",
            "dependentSchemas",
            "patternProperties",
            "propertyNames",
            "unevaluatedProperties");

    private static final Set<String> SUPPORTED_TYPES =
            Set.of("string", "integer", "number", "boolean", "array", "object", "null");

    /** How many generated values may miss the length bounds before the record is given up on. */
    private static final int LENGTH_ATTEMPTS = 10_000;

    /** The same limit while parsing. It is lower, so a combination that passes rarely runs out later. */
    private static final int PARSING_LENGTH_ATTEMPTS = 1_000;

    private static final BigDecimal LONG_MIN = BigDecimal.valueOf(Long.MIN_VALUE);
    private static final BigDecimal LONG_MAX = BigDecimal.valueOf(Long.MAX_VALUE);

    private static final double NUMBER_DEFAULT_LIMIT = 1_000_000.0;

    private SchemaParser() {}

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
            throw new IllegalArgumentException("Root JSON Schema must have \"type\": \"object\", got: " + type);
        }

        Object propertiesRaw = jsonSchema.get("properties");
        if (!(propertiesRaw instanceof Map<?, ?> propertiesMap)) {
            throw new IllegalArgumentException("JSON Schema object must have a \"properties\" map");
        }

        validateSupportedKeywords(jsonSchema, "$", Collections.newSetFromMap(new IdentityHashMap<>()));

        Field field = new Field(config);
        Map<String, SchemaValueProvider> providers = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
            if (!(entry.getKey() instanceof String fieldName)) {
                throw new IllegalArgumentException("Property keys must be strings");
            }
            if (!(entry.getValue() instanceof Map<?, ?> fieldSchemaRaw)) {
                throw new IllegalArgumentException("Property '" + fieldName + "' must have a schema object");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> fieldSchema = (Map<String, Object>) fieldSchemaRaw;
            // nullable widens the type, but a const or enum can still leave null out.
            boolean nullable = Boolean.TRUE.equals(fieldSchema.get("nullable")) && satisfies(null, fieldSchema);

            SchemaValueProvider provider =
                    resolveProvider(fieldName, "$.properties." + fieldName, fieldSchema, field, config);
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

    private static void validateSupportedKeywords(Map<?, ?> schema, String path, Set<Map<?, ?>> visiting) {
        if (!visiting.add(schema)) {
            throw new IllegalArgumentException("Recursive JSON Schema map is unsupported at " + path);
        }
        try {
            for (String keyword : UNSUPPORTED_KEYWORDS) {
                if (schema.containsKey(keyword)) {
                    throw new IllegalArgumentException("Unsupported JSON Schema keyword '" + keyword + "' at " + path);
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
                throw new IllegalArgumentException("JSON Schema enum must be a non-empty list at " + path);
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
    private static SchemaValueProvider resolveProvider(
            String fieldName, String path, Map<String, Object> fieldSchema, Field field, GeneratorConfig config) {
        // const and enum name their values outright, but the other constraints still apply to them.
        if (fieldSchema.containsKey("const")) {
            Object constValue = fieldSchema.get("const");
            allowedValues(Collections.singletonList(constValue), fieldSchema, path);
            return ctx -> constValue;
        }

        // enum: random selection
        if (fieldSchema.containsKey("enum")) {
            List<?> values = allowedValues((List<?>) fieldSchema.get("enum"), fieldSchema, path);
            return ctx -> values.get(ctx.random().nextInt(values.size()));
        }

        String schemaType = typeOf(fieldSchema);
        String format = stringOrNull(fieldSchema.get("format"));

        return switch (schemaType) {
            case "integer" -> resolveInteger(fieldSchema, path);
            case "number" -> resolveNumber(fieldSchema, path);
            case "boolean" -> ctx -> ctx.random().nextBoolean();
            case "array" -> resolveArray(fieldName, path, fieldSchema, field, config);
            case "object" -> resolveNestedObject(path, fieldSchema, field, config);
            case "null" -> ctx -> null;
            // A schema without a type also generates a string, so string constraints apply to it too.
            default -> resolveString(fieldName, path, fieldSchema, format, field, config);
        };
    }

    private static List<?> allowedValues(List<?> values, Map<?, ?> schema, String path) {
        List<?> allowed;
        try {
            allowed = values.stream().filter(value -> satisfies(value, schema)).toList();
        } catch (IllegalArgumentException exception) {
            // A pattern that is not a regular expression, or a number that JSON cannot write, such as NaN.
            throw new IllegalArgumentException(
                    "JSON Schema const or enum cannot be checked at " + path + ": " + exception.getMessage(),
                    exception);
        }
        if (allowed.isEmpty()) {
            throw new IllegalArgumentException(
                    "JSON Schema const or enum has no value that meets the other constraints at " + path + ": "
                            + schema);
        }
        return allowed;
    }

    /**
     * Whether an explicit value meets a schema. A value is judged as it is written, so an Optional counts
     * as its content and a set or array as a list. A keyword constrains only values of its own JSON type.
     */
    private static boolean satisfies(@Nullable Object explicit, Map<?, ?> schema) {
        Object value = JsonSchemaSupport.normalizeStructuredValue(explicit);
        if (!hasDeclaredType(value, schema)
                || (schema.containsKey("const") && !sameValue(value, schema.get("const")))
                || (schema.get("enum") instanceof List<?> values
                        && values.stream().noneMatch(other -> sameValue(value, other)))) {
            return false;
        }
        if (value instanceof Number number) {
            return withinNumericBounds(number, schema);
        }
        if (value instanceof List<?> list) {
            return list.size() >= intOrDefault(schema.get("minItems"), 0)
                    && list.size() <= intOrDefault(schema.get("maxItems"), Integer.MAX_VALUE)
                    && (!(schema.get("items") instanceof Map<?, ?> items)
                            || list.stream().allMatch(item -> satisfies(item, items)));
        }
        if (value instanceof Map<?, ?> object && schema.get("properties") instanceof Map<?, ?> properties) {
            return properties.entrySet().stream()
                    .allMatch(property -> !object.containsKey(property.getKey())
                            || !(property.getValue() instanceof Map<?, ?> propertySchema)
                            || satisfies(object.get(property.getKey()), propertySchema));
        }
        // A string, or anything else written as one, such as a date. A pattern searches that text; it
        // need not match all of it.
        return value == null
                || !"string".equals(JsonSchemaSupport.infer(value).get("type"))
                || (withinLengthBounds(value.toString(), schema)
                        && (!(schema.get("pattern") instanceof String pattern)
                                || Pattern.compile(pattern)
                                        .matcher(value.toString())
                                        .find()));
    }

    private static boolean hasDeclaredType(@Nullable Object value, Map<?, ?> schema) {
        Object declared = schema.get("type");
        if (declared == null) {
            return true;
        }
        List<?> types = declared instanceof List<?> list ? list : List.of(declared);
        if (value == null) {
            return types.contains("null") || Boolean.TRUE.equals(schema.get("nullable"));
        }
        if (value instanceof Number number) {
            // JSON has one number type: 5.0 is an integer.
            return types.contains("number")
                    || (types.contains("integer")
                            && decimal(number).stripTrailingZeros().scale() <= 0);
        }
        return types.contains(JsonSchemaSupport.infer(value).get("type"));
    }

    private static boolean sameValue(@Nullable Object value, @Nullable Object explicit) {
        Object other = JsonSchemaSupport.normalizeStructuredValue(explicit);
        return value instanceof Number number && other instanceof Number otherNumber
                ? decimal(number).compareTo(decimal(otherNumber)) == 0
                : Objects.equals(value, other);
    }

    private static boolean withinNumericBounds(Number number, Map<?, ?> schema) {
        BigDecimal value = decimal(number);
        return (!(schema.get("minimum") instanceof Number minimum) || value.compareTo(decimal(minimum)) >= 0)
                && (!(schema.get("maximum") instanceof Number maximum) || value.compareTo(decimal(maximum)) <= 0)
                && (!(exclusiveBound(schema, "exclusiveMinimum", "minimum") instanceof Number exclusiveMinimum)
                        || value.compareTo(decimal(exclusiveMinimum)) > 0)
                && (!(exclusiveBound(schema, "exclusiveMaximum", "maximum") instanceof Number exclusiveMaximum)
                        || value.compareTo(decimal(exclusiveMaximum)) < 0);
    }

    /** An exclusive bound, which OpenAPI 3.0 writes as a boolean flag on the inclusive keyword. */
    private static @Nullable Object exclusiveBound(Map<?, ?> schema, String exclusiveKeyword, String inclusiveKeyword) {
        Object bound = schema.get(exclusiveKeyword);
        return Boolean.TRUE.equals(bound) ? schema.get(inclusiveKeyword) : bound;
    }

    private static BigDecimal decimal(Number number) {
        return new BigDecimal(number.toString());
    }

    private static boolean withinLengthBounds(String text, Map<?, ?> schema) {
        // JSON Schema counts characters, not UTF-16 units.
        int length = text.codePointCount(0, text.length());
        return length >= intOrDefault(schema.get("minLength"), 0)
                && length <= intOrDefault(schema.get("maxLength"), Integer.MAX_VALUE);
    }

    private static boolean declaresLength(Map<?, ?> schema) {
        return schema.containsKey("minLength") || schema.containsKey("maxLength");
    }

    private static SchemaValueProvider resolveString(
            String fieldName,
            String path,
            Map<String, Object> schema,
            @Nullable String format,
            Field field,
            GeneratorConfig config) {
        if (intOrDefault(schema.get("minLength"), 0) > intOrDefault(schema.get("maxLength"), Integer.MAX_VALUE)) {
            throw emptyRange("string length", path, schema);
        }

        // Explicit constraints win over field-name semantics: pattern first, then format, and
        // semantic values only when no length bounds are declared.
        String pattern = stringOrNull(schema.get("pattern"));
        if (pattern != null) {
            validatePattern(pattern, path);
            // RegexGenerator's open-ended repetitions have to be able to reach the declared minimum.
            int minLength = Math.clamp(intOrDefault(schema.get("minLength"), 1), 1, Integer.MAX_VALUE - 1);
            return fittingLength(
                    ctx -> matching(pattern, minLength, ctx.random().nextLong()), schema, path, config);
        }

        if (format != null) {
            SchemaValueProvider formatProvider = resolveFormat(format, config);
            if (formatProvider != null) {
                return fittingLength(formatProvider, schema, path, config);
            }
        }

        if (!declaresLength(schema)) {
            SchemaValueProvider semantic = trySemanticResolve(fieldName, field);
            if (semantic != null) {
                // A semantic value can have its own type, such as a numeric timestamp; a declared type wins.
                return schema.containsKey("type")
                                && !"string".equals(semantic.jsonSchema().get("type"))
                        ? ctx -> String.valueOf(semantic.generate(ctx))
                        : semantic;
            }
        }

        // Bounded string length
        // A missing bound defaults within the declared one: maxLength 2 alone must not keep a larger
        // configured minimum length.
        int maxLength = intOrDefault(
                schema.get("maxLength"),
                Math.max(config.getMaxStringLength(), intOrDefault(schema.get("minLength"), 0)));
        int minLength = intOrDefault(schema.get("minLength"), Math.min(config.getMinStringLength(), maxLength));
        // Generated text has at least one character, so only maxLength 0 needs the empty string.
        return maxLength == 0 ? ctx -> "" : text(Math.max(minLength, 1), maxLength);
    }

    /**
     * Draws candidates until one fits the declared length bounds. Nothing analyses a pattern or format
     * for its possible lengths, so the first search runs here, to fail while parsing. Candidates that
     * draw from the context get a fixed seed, which gives them the same verdict on every run.
     */
    private static SchemaValueProvider fittingLength(
            SchemaValueProvider candidates, Map<String, Object> schema, String path, GeneratorConfig config) {
        if (!declaresLength(schema)) {
            return candidates;
        }
        // Values keep to the bounds as parsed, even if the caller's map changes later.
        Map<String, Object> parsed = new LinkedHashMap<>(schema);
        firstFitting(
                candidates,
                new SchemaContext(config.getLocale(), new Random(0L), 0),
                PARSING_LENGTH_ATTEMPTS,
                parsed,
                path);
        return ctx -> firstFitting(candidates, ctx, LENGTH_ATTEMPTS, parsed, path);
    }

    private static String firstFitting(
            SchemaValueProvider candidates, SchemaContext ctx, int attempts, Map<String, Object> schema, String path) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            String candidate = String.valueOf(candidates.generate(ctx));
            if (withinLengthBounds(candidate, schema)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("JSON Schema length bounds could not be met at " + path + ": none of "
                + attempts + " generated values fit " + schema
                + " (a pattern's *, + and {n,} repeat up to 10 times, or up to twice minLength, at most 10000 times)");
    }

    /** Parses the pattern while the schema is parsed, so that one it cannot generate from fails with its path. */
    private static void validatePattern(String pattern, String path) {
        try {
            new RegexGenerator(pattern);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "JSON Schema pattern cannot generate values at " + path + ": " + exception.getMessage(), exception);
        }
    }

    private static String matching(String pattern, int minLength, long seed) {
        // Each value draws its own seed from the field's stream, so a fresh generator per value keeps
        // the record replayable without sharing mutable state; the parsed pattern itself is cached.
        return new RegexGenerator(
                        pattern,
                        GeneratorConfig.builder()
                                .seed(seed)
                                .stringLength(minLength, minLength)
                                .build())
                .generate();
    }

    private static @Nullable SchemaValueProvider resolveFormat(String format, GeneratorConfig config) {
        Function<GeneratorConfig, Generator<?>> factory = switch (format) {
            // Every email is also a valid idn-email.
            case "email", "idn-email" -> Generators::ofEmail;
            case "uri", "url" -> URLGenerator::new;
            case "uuid" -> UUIDGenerator::new;
            case "date" -> DateGenerator::new;
            case "date-time" -> LocalDateTimeGenerator::new;
            case "time" -> TimeGenerator::new;
            case "ipv4" -> IPv4Generator::new;
            case "ipv6" -> IPv6Generator::new;
            case "hostname" -> HostnameGenerator::new;
            default -> null;
        };
        if (factory == null) {
            return null;
        }
        if (config.getSeed().isEmpty()) {
            // Unseeded, secure, and caller-owned sources keep one generator per field.
            Generator<?> generator = factory.apply(config);
            return ctx -> generator.generate().toString();
        }
        // A seeded value comes from the field's own stream, so equal formats in different columns
        // differ and each value replays with its record.
        return ctx -> factory.apply(
                        config.toBuilder().seed(ctx.random().nextLong()).build())
                .generate()
                .toString();
    }

    private static SchemaValueProvider resolveInteger(Map<String, Object> schema, String path) {
        // Every bound narrows the range, and a fractional one rounds inward: a minimum of 1.5 means 2.
        BigDecimal lowest = LONG_MIN;
        BigDecimal highest = LONG_MAX;
        if (schema.get("minimum") instanceof Number minimum) {
            lowest = lowest.max(decimal(minimum).setScale(0, RoundingMode.CEILING));
        }
        if (exclusiveBound(schema, "exclusiveMinimum", "minimum") instanceof Number exclusive) {
            lowest = lowest.max(
                    decimal(exclusive).setScale(0, RoundingMode.FLOOR).add(BigDecimal.ONE));
        }
        if (schema.get("maximum") instanceof Number maximum) {
            highest = highest.min(decimal(maximum).setScale(0, RoundingMode.FLOOR));
        }
        if (exclusiveBound(schema, "exclusiveMaximum", "maximum") instanceof Number exclusive) {
            highest = highest.min(
                    decimal(exclusive).setScale(0, RoundingMode.CEILING).subtract(BigDecimal.ONE));
        }
        if (lowest.compareTo(highest) > 0) {
            throw emptyRange("integer", path, schema);
        }

        long min = lowest.longValue();
        long max = highest.longValue();
        // A missing bound defaults within the declared one: a lone minimum above the int range must not
        // keep the int maximum.
        if (!schema.containsKey("maximum") && !schema.containsKey("exclusiveMaximum") && min <= Integer.MAX_VALUE) {
            max = Integer.MAX_VALUE;
        }
        if (!schema.containsKey("minimum") && !schema.containsKey("exclusiveMinimum") && max >= Integer.MIN_VALUE) {
            min = Integer.MIN_VALUE;
        }

        long lower = min;
        long upper = max;
        // Values that fit an int stay Integer; the + 1 below cannot overflow.
        if (lower >= Integer.MIN_VALUE && upper < Integer.MAX_VALUE) {
            return ctx -> ctx.random().nextInt((int) lower, (int) upper + 1);
        }
        if (upper < Long.MAX_VALUE) {
            return ctx -> ctx.random().nextLong(lower, upper + 1);
        }
        if (lower > Long.MIN_VALUE) {
            return ctx -> ctx.random().nextLong(lower - 1, upper) + 1;
        }
        return ctx -> ctx.random().nextLong();
    }

    private static SchemaValueProvider resolveNumber(Map<String, Object> schema, String path) {
        // Every bound narrows the range; a side with no bound stays infinite until its default below.
        double lowest = Double.NEGATIVE_INFINITY;
        if (schema.get("minimum") instanceof Number minimum) {
            lowest = doubleAtOrAbove(minimum);
        }
        if (exclusiveBound(schema, "exclusiveMinimum", "minimum") instanceof Number exclusive) {
            lowest = Math.max(lowest, Math.nextUp(doubleAtOrBelow(exclusive)));
        }
        double highest = Double.POSITIVE_INFINITY;
        if (schema.get("maximum") instanceof Number maximum) {
            highest = doubleAtOrBelow(maximum);
        }
        if (exclusiveBound(schema, "exclusiveMaximum", "maximum") instanceof Number exclusive) {
            highest = Math.min(highest, Math.nextDown(doubleAtOrAbove(exclusive)));
        }
        // A missing bound defaults within the declared one: a lone minimum above the default range must
        // not keep the default maximum.
        if (highest == Double.POSITIVE_INFINITY) {
            highest = lowest < NUMBER_DEFAULT_LIMIT ? NUMBER_DEFAULT_LIMIT : lowest + 2 * NUMBER_DEFAULT_LIMIT;
        }
        if (lowest == Double.NEGATIVE_INFINITY) {
            lowest = highest > -NUMBER_DEFAULT_LIMIT ? -NUMBER_DEFAULT_LIMIT : highest - 2 * NUMBER_DEFAULT_LIMIT;
        }
        // An infinity left here is a bound beyond every double, with no double on its allowed side.
        if (lowest > highest || lowest == Double.POSITIVE_INFINITY || highest == Double.NEGATIVE_INFINITY) {
            throw emptyRange("number", path, schema);
        }
        double origin = lowest;
        double limit = highest;
        // Draws are half-open, so an inclusive maximum becomes the next representable double.
        double bound = Math.nextUp(limit);
        if (Double.isInfinite(bound - origin)) {
            // nextDouble(origin, bound) cannot take a range wider than a double holds.
            return ctx -> {
                double share = ctx.random().nextDouble();
                return Math.clamp(origin * (1 - share) + limit * share, origin, limit);
            };
        }
        return ctx -> ctx.random().nextDouble(origin, bound);
    }

    /**
     * The lowest double that, as written, is not below a bound, since a double may not hold the bound
     * itself. Beyond the range of a double, this is the infinity on the bound's side.
     */
    private static double doubleAtOrAbove(Number bound) {
        BigDecimal exact = decimal(bound);
        double nearest = exact.doubleValue();
        return Double.isFinite(nearest) && decimal(nearest).compareTo(exact) < 0 ? Math.nextUp(nearest) : nearest;
    }

    /**
     * The highest double that, as written, is not above a bound, since a double may not hold the bound
     * itself. Beyond the range of a double, this is the infinity on the bound's side.
     */
    private static double doubleAtOrBelow(Number bound) {
        BigDecimal exact = decimal(bound);
        double nearest = exact.doubleValue();
        return Double.isFinite(nearest) && decimal(nearest).compareTo(exact) > 0 ? Math.nextDown(nearest) : nearest;
    }

    private static IllegalArgumentException emptyRange(String kind, String path, Map<String, Object> schema) {
        return new IllegalArgumentException("JSON Schema " + kind + " range has no values at " + path + ": " + schema);
    }

    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveArray(
            String fieldName, String path, Map<String, Object> schema, Field field, GeneratorConfig config) {
        // As with string lengths, a missing bound defaults within the declared one.
        int maxItems = intOrDefault(
                schema.get("maxItems"),
                Math.max(config.getMaxCollectionSize(), intOrDefault(schema.get("minItems"), 0)));
        int minItems = intOrDefault(schema.get("minItems"), Math.min(config.getMinCollectionSize(), maxItems));
        if (minItems > maxItems) {
            throw emptyRange("array size", path, schema);
        }

        SchemaValueProvider itemProvider;
        Object itemsRaw = schema.get("items");
        if (itemsRaw instanceof Map<?, ?> itemSchema) {
            itemProvider =
                    resolveProvider(fieldName + "[]", path + ".items", (Map<String, Object>) itemSchema, field, config);
        } else {
            itemProvider = randomText(config);
        }

        SchemaValueProvider items = itemProvider;
        return ctx -> {
            int size = minItems == maxItems ? minItems : minItems + ctx.random().nextInt(maxItems - minItems + 1);
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(items.generate(ctx));
            }
            return list;
        };
    }

    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveNestedObject(
            String path, Map<String, Object> schema, Field field, GeneratorConfig config) {
        Object propertiesRaw = schema.get("properties");
        if (!(propertiesRaw instanceof Map<?, ?> propertiesMap) || propertiesMap.isEmpty()) {
            return ctx -> Collections.emptyMap();
        }

        Map<String, SchemaValueProvider> nestedProviders = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : propertiesMap.entrySet()) {
            if (entry.getKey() instanceof String name && entry.getValue() instanceof Map<?, ?> propSchema) {
                nestedProviders.put(
                        name,
                        resolveProvider(
                                name, path + ".properties." + name, (Map<String, Object>) propSchema, field, config));
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

    private static SchemaValueProvider randomText(GeneratorConfig config) {
        return text(config.getMinStringLength(), config.getMaxStringLength());
    }

    /** Text drawn from the field's own stream, so seeded schemas replay and equal columns differ. */
    private static SchemaValueProvider text(int minLength, int maxLength) {
        return ctx -> StringGenerator.builder()
                .minLength(minLength)
                .maxLength(maxLength)
                .seed(ctx.random().nextLong())
                .build()
                .generate();
    }

    private static @Nullable SchemaValueProvider trySemanticResolve(String fieldName, Field field) {
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
                .toLowerCase(Locale.ROOT)
                .replace('_', '.');
    }

    private static @Nullable String semanticReference(String normalized) {
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
            case "id" -> "code.uuid";
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

    private static @Nullable String stringOrNull(@Nullable Object value) {
        return value instanceof String s ? s : null;
    }

    private static int intOrDefault(@Nullable Object value, int defaultValue) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        return defaultValue;
    }
}
