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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
 *   <li>{@code multipleOf} — exact multiples: an integer stays an {@code Integer} or {@code Long}, and a
 *       number is a {@link BigDecimal}</li>
 *   <li>{@code minLength}/{@code maxLength} — bounded string length</li>
 *   <li>{@code pattern} — regex-based string generation</li>
 *   <li>{@code items} — array element schema, or a list of schemas, one for each position, which is the
 *       tuple form of the drafts up to 2019-09; {@code true} allows any item and {@code false} none</li>
 *   <li>{@code additionalItems} — beside a list of items, the schema of the items after them, or
 *       {@code false} for none</li>
 *   <li>{@code minItems}/{@code maxItems} — bounded array size</li>
 *   <li>{@code uniqueItems} — array items that are distinct JSON values, drawn up to 100 times for each
 *       item</li>
 *   <li>{@code properties} — nested object fields, where {@code true} allows any value and {@code false}
 *       leaves the property out</li>
 *   <li>{@code required}, {@code additionalProperties}, {@code minProperties}/{@code maxProperties} — the
 *       members of a nested object, which holds every property it declares unless these ask otherwise;
 *       additional members are named {@code property1}, {@code property2}, and so on</li>
 *   <li>{@code dependencies} — property dependencies, the form of the drafts up to 7</li>
 *   <li>{@code nullable} (OpenAPI 3.0) — nullable field support</li>
 * </ul>
 *
 * <p>A type list may contain one concrete supported type plus {@code null}. Empty, malformed, and
 * unsupported type declarations, non-string formats, empty or malformed enums, and an {@code items} that
 * is no schema, boolean, or list of them are rejected with their schema path. A list of items holds the
 * whole tuple by default, and {@code additionalItems} is read only beside one, as in the drafts that
 * define it; {@code prefixItems}, its Draft 2020-12 replacement, is rejected. The root generates every
 * property as a column, so its {@code required} names, property dependencies, and member counts must allow
 * exactly those. References and schema composition ({@code $ref}, {@code allOf}, {@code anyOf},
 * {@code oneOf}, schema dependencies, and related conditional keywords) are rejected with a schema path
 * because this parser has no resolver or composition semantics.
 *
 * <p>Constraints apply together, and a keyword constrains only values of its own JSON type. A
 * {@code const} or {@code enum} value must meet the other constraints, and an {@code enum} keeps only
 * the values that do; {@code format} is not one of them, because it is an annotation in Draft 2020-12.
 * Values compare as JSON values: a number by its value at any depth, so {@code {"id": 1}} equals
 * {@code {"id": 1.0}}, an array item by item in order, an object member by member under the name it is
 * written with, and a value written as a string, such as a date, by its text. Overlapping numeric
 * bounds intersect. A {@code pattern} or {@code format} value is drawn again until
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

    /** How many items an array with uniqueItems may draw for each item it holds. */
    private static final int UNIQUE_ITEM_ATTEMPTS = 100;

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
            // Resolving first rejects a const or enum that is no JSON value with its path.
            SchemaValueProvider provider =
                    resolveProvider(fieldName, "$.properties." + fieldName, fieldSchema, field, config);
            // nullable widens the type, but a const or enum can still leave null out.
            boolean nullable = Boolean.TRUE.equals(fieldSchema.get("nullable")) && satisfies(null, fieldSchema);
            if (nullable) {
                SchemaValueProvider inner = provider;
                provider = ctx -> ctx.random().nextBoolean() ? null : inner.generate(ctx);
            }
            providers.put(fieldName, provider);
        }

        if (providers.isEmpty()) {
            throw new IllegalArgumentException("JSON Schema must have at least one property");
        }
        checkRootMembers(jsonSchema, providers.keySet());

        return new Schema(config, providers);
    }

    /**
     * The root generates every property it declares, as a column, and no other, so its required names,
     * property dependencies, and minProperties and maxProperties must allow exactly those.
     */
    private static void checkRootMembers(Map<String, Object> root, Set<String> columns) {
        String reason = "JSON Schema root object generates exactly its properties, as columns, so ";
        for (String name : names(root.get("required"))) {
            if (!columns.contains(name)) {
                throw new IllegalArgumentException(
                        reason + "required property '" + name + "' must be one of them at $");
            }
        }
        for (Map.Entry<String, List<String>> dependency :
                propertyDependencies(root).entrySet()) {
            for (String name : dependency.getValue()) {
                if (columns.contains(dependency.getKey()) && !columns.contains(name)) {
                    throw new IllegalArgumentException(reason + "property '" + name
                            + "', which dependencies require beside '" + dependency.getKey()
                            + "', must be one of them at $");
                }
            }
        }
        if (columns.size() < count(root, "minProperties", 0)
                || columns.size() > count(root, "maxProperties", Integer.MAX_VALUE)) {
            throw new IllegalArgumentException(
                    reason + "its " + columns.size() + " properties must meet minProperties and maxProperties at $");
        }
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
            } else if (items instanceof List<?> tuple) {
                for (int position = 0; position < tuple.size(); position++) {
                    if (tuple.get(position) instanceof Map<?, ?> itemSchema) {
                        validateSupportedKeywords(itemSchema, path + ".items[" + position + "]", visiting);
                    }
                }
                if (schema.get("additionalItems") instanceof Map<?, ?> additionalSchema) {
                    validateSupportedKeywords(additionalSchema, path + ".additionalItems", visiting);
                }
            }
            if (schema.get("additionalProperties") instanceof Map<?, ?> additionalSchema) {
                validateSupportedKeywords(additionalSchema, path + ".additionalProperties", visiting);
            }
        } finally {
            visiting.remove(schema);
        }
    }

    private static void validateSupportedShape(Map<?, ?> schema, String path) {
        if (schema.containsKey("items")) {
            Object items = schema.get("items");
            if (!(items instanceof List<?> tuple ? tuple.stream().allMatch(SchemaParser::isSchema) : isSchema(items))) {
                throw new IllegalArgumentException(
                        "JSON Schema items must be a schema, a boolean, or a list of them at " + path);
            }
            // additionalItems means something only beside a list of items, and is ignored elsewhere.
            if (items instanceof List<?>
                    && schema.containsKey("additionalItems")
                    && !isSchema(schema.get("additionalItems"))) {
                throw new IllegalArgumentException(
                        "JSON Schema additionalItems must be a schema or a boolean at " + path);
            }
        }

        for (String keyword : List.of("minProperties", "maxProperties")) {
            if (schema.containsKey(keyword) && !isCount(schema.get(keyword))) {
                throw new IllegalArgumentException(
                        "JSON Schema " + keyword + " must be a whole number of at least 0 at " + path);
            }
        }
        if (schema.containsKey("required") && !isNameList(schema.get("required"))) {
            throw new IllegalArgumentException("JSON Schema required must be a list of property names at " + path);
        }
        if (schema.containsKey("additionalProperties") && !isSchema(schema.get("additionalProperties"))) {
            throw new IllegalArgumentException(
                    "JSON Schema additionalProperties must be a schema or a boolean at " + path);
        }
        if (schema.containsKey("dependencies")) {
            validateDependencies(schema.get("dependencies"), path);
        }

        if (schema.containsKey("enum")) {
            Object enumValues = schema.get("enum");
            if (!(enumValues instanceof List<?> values) || values.isEmpty()) {
                throw new IllegalArgumentException("JSON Schema enum must be a non-empty list at " + path);
            }
        }

        if (schema.containsKey("format") && !(schema.get("format") instanceof String)) {
            throw new IllegalArgumentException("JSON Schema format must be a string at " + path);
        }

        if (schema.containsKey("multipleOf")
                && !(schema.get("multipleOf") instanceof Number step && isPositive(step))) {
            throw new IllegalArgumentException("JSON Schema multipleOf must be a number above 0 at " + path);
        }

        if (schema.containsKey("uniqueItems") && !(schema.get("uniqueItems") instanceof Boolean)) {
            throw new IllegalArgumentException("JSON Schema uniqueItems must be a boolean at " + path);
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

    /** Whether a value is a schema: an object, or true or false, which allow any value or none. */
    private static boolean isSchema(@Nullable Object value) {
        return value instanceof Map<?, ?> || value instanceof Boolean;
    }

    private static boolean isNameList(@Nullable Object value) {
        return value instanceof List<?> names && names.stream().allMatch(String.class::isInstance);
    }

    /** Whether a value is a whole number of at least 0, as a count of properties must be. */
    private static boolean isCount(@Nullable Object value) {
        try {
            return value instanceof Number number
                    && decimal(number).signum() >= 0
                    && decimal(number).stripTrailingZeros().scale() <= 0;
        } catch (NumberFormatException notFinite) {
            return false;
        }
    }

    /**
     * Accepts property dependencies, which name the properties an object must hold beside one it holds.
     * A schema dependency asks such an object to meet a second schema too, which needs the schema
     * composition this parser does not have, so it is rejected, as allOf is.
     */
    private static void validateDependencies(@Nullable Object dependencies, String path) {
        if (dependencies instanceof Map<?, ?> entries) {
            for (Map.Entry<?, ?> entry : entries.entrySet()) {
                if (entry.getKey() instanceof String name && isSchema(entry.getValue())) {
                    throw new IllegalArgumentException("Unsupported JSON Schema schema dependency at " + path
                            + ".dependencies." + name + ": an object that holds '" + name
                            + "' must also meet a second schema, which needs schema composition; only a list of"
                            + " property names is supported");
                }
                if (!(entry.getKey() instanceof String) || !isNameList(entry.getValue())) {
                    throw malformedDependencies(path);
                }
            }
            return;
        }
        throw malformedDependencies(path);
    }

    private static IllegalArgumentException malformedDependencies(String path) {
        return new IllegalArgumentException(
                "JSON Schema dependencies must map property names to lists of property names at " + path);
    }

    /** A count of properties, which the shape check has made a whole number of at least 0, up to an int. */
    private static int count(Map<?, ?> schema, String keyword, int defaultValue) {
        return schema.get(keyword) instanceof Number number
                ? decimal(number).min(BigDecimal.valueOf(Integer.MAX_VALUE)).intValue()
                : defaultValue;
    }

    /** A list of names, which the shape check has made sure of, or none. */
    @SuppressWarnings("unchecked")
    private static List<String> names(@Nullable Object value) {
        return value instanceof List<?> names ? (List<String>) names : List.of();
    }

    /** The property dependencies, the only ones the shape check lets through: names each name brings. */
    @SuppressWarnings("unchecked")
    private static Map<String, List<String>> propertyDependencies(Map<?, ?> schema) {
        return schema.get("dependencies") instanceof Map<?, ?> dependencies
                ? (Map<String, List<String>>) dependencies
                : Map.of();
    }

    /** A property and every property that dependencies require beside it, through every link, in order. */
    private static List<String> dependents(String name, Map<String, List<String>> dependencies) {
        List<String> closure = new ArrayList<>(List.of(name));
        for (int i = 0; i < closure.size(); i++) {
            for (String dependent : dependencies.getOrDefault(closure.get(i), List.of())) {
                if (!closure.contains(dependent)) {
                    closure.add(dependent);
                }
            }
        }
        return closure;
    }

    /** Whether a number is finite and above 0, as a multipleOf must be. */
    private static boolean isPositive(Number number) {
        try {
            return decimal(number).signum() > 0;
        } catch (NumberFormatException notFinite) {
            return false;
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
        if (!hasDeclaredType(value, schema) || !isNamedValue(value, schema)) {
            return false;
        }
        if (value instanceof Number number) {
            return withinNumericBounds(number, schema)
                    && (!(schema.get("multipleOf") instanceof Number step)
                            || decimal(number).remainder(decimal(step)).signum() == 0);
        }
        if (value instanceof List<?> list) {
            return list.size() >= intOrDefault(schema.get("minItems"), 0)
                    && list.size() <= intOrDefault(schema.get("maxItems"), Integer.MAX_VALUE)
                    && (!Boolean.TRUE.equals(schema.get("uniqueItems"))
                            || jsonValues(list).size() == list.size())
                    && itemsMeet(list, schema);
        }
        if (value instanceof Map<?, ?> object) {
            return membersMeet(byName(object), schema);
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

    /**
     * Whether every item meets its schema: the items schema, or with a list of items, the one at its
     * position and additionalItems after them.
     */
    private static boolean itemsMeet(List<?> list, Map<?, ?> schema) {
        Object items = schema.get("items");
        for (int position = 0; position < list.size(); position++) {
            Object itemSchema = items instanceof List<?> tuple
                    ? (position < tuple.size() ? tuple.get(position) : schema.get("additionalItems"))
                    : items;
            if (!meets(list.get(position), itemSchema)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether an object's members meet the object keywords: each member its property's schema, or
     * additionalProperties if it has none; the member count minProperties and maxProperties; and the
     * names required and those that each member's dependencies require.
     */
    private static boolean membersMeet(Map<String, @Nullable Object> members, Map<?, ?> schema) {
        Map<?, ?> properties = schema.get("properties") instanceof Map<?, ?> declared ? declared : Map.of();
        for (Map.Entry<String, @Nullable Object> member : members.entrySet()) {
            String name = member.getKey();
            Object memberSchema =
                    properties.containsKey(name) ? properties.get(name) : schema.get("additionalProperties");
            if (!meets(member.getValue(), memberSchema)) {
                return false;
            }
        }
        Set<String> names = members.keySet();
        return names.size() >= count(schema, "minProperties", 0)
                && names.size() <= count(schema, "maxProperties", Integer.MAX_VALUE)
                && names.containsAll(names(schema.get("required")))
                && propertyDependencies(schema).entrySet().stream()
                        .allMatch(dependency ->
                                !names.contains(dependency.getKey()) || names.containsAll(dependency.getValue()));
    }

    /** Whether a value meets a subschema: true or a missing one allows any value, and false none. */
    private static boolean meets(@Nullable Object value, @Nullable Object subschema) {
        return subschema instanceof Map<?, ?> map ? satisfies(value, map) : !Boolean.FALSE.equals(subschema);
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

    /**
     * Whether a value is the const, and one of the enum, where the schema names them, compared as JSON
     * values. Every member of the enum is read as one, so an enum with a member that is no JSON value
     * fails in any order.
     */
    private static boolean isNamedValue(@Nullable Object value, Map<?, ?> schema) {
        boolean constant = schema.containsKey("const");
        if (!constant && !(schema.get("enum") instanceof List<?>)) {
            return true;
        }
        Object json = jsonValue(value);
        return (!constant || Objects.equals(json, json(schema.get("const"))))
                && (!(schema.get("enum") instanceof List<?> members)
                        || jsonValues(members).contains(json));
    }

    /** The distinct JSON values among explicit values. */
    private static Set<@Nullable Object> jsonValues(List<?> values) {
        Set<@Nullable Object> json = new HashSet<>();
        for (Object value : values) {
            json.add(json(value));
        }
        return json;
    }

    /** An explicit value, such as a const or a generated item, as a JSON value. */
    private static @Nullable Object json(@Nullable Object explicit) {
        return jsonValue(JsonSchemaSupport.normalizeStructuredValue(explicit));
    }

    /**
     * A value as JSON sees it, so that two values are the same JSON value when these are equal: a number
     * is its exact value, whatever holds it, so 1 equals 1.0; an array is the list of its items, in order;
     * an object is its members under the names they are written with; and anything written as a string
     * is its text. NaN and the infinities are rejected, as no JSON number, and so is an object with two
     * keys written under one name.
     */
    private static @Nullable Object jsonValue(@Nullable Object value) {
        if (value == null || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Number number) {
            return decimal(number).stripTrailingZeros();
        }
        if (value instanceof List<?> items) {
            List<@Nullable Object> json = new ArrayList<>(items.size());
            for (Object item : items) {
                json.add(jsonValue(item));
            }
            return json;
        }
        if (value instanceof Map<?, ?> object) {
            Map<String, @Nullable Object> members = byName(object);
            for (Map.Entry<String, @Nullable Object> member : members.entrySet()) {
                member.setValue(jsonValue(member.getValue()));
            }
            return members;
        }
        return value.toString();
    }

    /**
     * An object's members under the names the JSON writer gives them, so the key 1 is the member "1". Two
     * keys written under one name, such as 1 and "1", would make two members of that name, so they are
     * rejected rather than one hiding the other.
     */
    private static Map<String, @Nullable Object> byName(Map<?, ?> object) {
        Map<String, @Nullable Object> members = new LinkedHashMap<>();
        for (Map.Entry<?, ?> member : object.entrySet()) {
            String name = String.valueOf(member.getKey());
            if (members.containsKey(name)) {
                throw new IllegalArgumentException("two members of a JSON object are written as \"" + name + "\"");
            }
            members.put(name, member.getValue());
        }
        return members;
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
        boolean openAbove = !schema.containsKey("maximum") && !schema.containsKey("exclusiveMaximum");
        boolean openBelow = !schema.containsKey("minimum") && !schema.containsKey("exclusiveMinimum");
        // A missing bound defaults within the declared one: a lone minimum above the int range must not
        // keep the int maximum.
        if (openAbove && min <= Integer.MAX_VALUE) {
            max = Integer.MAX_VALUE;
        }
        if (openBelow && max >= Integer.MIN_VALUE) {
            min = Integer.MIN_VALUE;
        }
        if (schema.get("multipleOf") instanceof Number multipleOf) {
            BigDecimal step = wholeStep(decimal(multipleOf));
            BigInteger first = quotient(BigDecimal.valueOf(min), step, RoundingMode.CEILING);
            BigInteger last = quotient(BigDecimal.valueOf(max), step, RoundingMode.FLOOR);
            // A default is this parser's own choice, so where it leaves no multiple, the open side reaches
            // the nearest one, if a long holds it.
            if (first.compareTo(last) > 0) {
                if (openAbove) {
                    last = first.min(quotient(highest, step, RoundingMode.FLOOR));
                } else if (openBelow) {
                    first = last.max(quotient(lowest, step, RoundingMode.CEILING));
                }
            }
            return multiples(step, first, last, true, path, schema);
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
        if (schema.get("multipleOf") instanceof Number multipleOf) {
            return decimalMultiples(decimal(multipleOf), schema, path);
        }
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

    /**
     * The multiples of a step that a number's bounds allow. Each bound gives, exactly, the least or
     * greatest whole k for which k × step is within it. A missing bound defaults from the declared one, as
     * for any number; a default is this parser's own choice, so where it leaves no multiple, the open side
     * reaches the nearest one.
     */
    private static SchemaValueProvider decimalMultiples(BigDecimal step, Map<String, Object> schema, String path) {
        BigDecimal floorValue = null;
        BigInteger lowest = null;
        if (schema.get("minimum") instanceof Number minimum) {
            floorValue = decimal(minimum);
            lowest = quotient(floorValue, step, RoundingMode.CEILING);
        }
        if (exclusiveBound(schema, "exclusiveMinimum", "minimum") instanceof Number exclusive) {
            BigDecimal bound = decimal(exclusive);
            BigInteger above = quotient(bound, step, RoundingMode.FLOOR).add(BigInteger.ONE);
            floorValue = floorValue == null ? bound : floorValue.max(bound);
            lowest = lowest == null ? above : lowest.max(above);
        }
        BigDecimal ceilingValue = null;
        BigInteger highest = null;
        if (schema.get("maximum") instanceof Number maximum) {
            ceilingValue = decimal(maximum);
            highest = quotient(ceilingValue, step, RoundingMode.FLOOR);
        }
        if (exclusiveBound(schema, "exclusiveMaximum", "maximum") instanceof Number exclusive) {
            BigDecimal bound = decimal(exclusive);
            BigInteger below = quotient(bound, step, RoundingMode.CEILING).subtract(BigInteger.ONE);
            ceilingValue = ceilingValue == null ? bound : ceilingValue.min(bound);
            highest = highest == null ? below : highest.min(below);
        }
        BigDecimal limit = BigDecimal.valueOf(NUMBER_DEFAULT_LIMIT);
        BigDecimal top = ceilingValue;
        if (top == null) {
            top = floorValue == null || floorValue.compareTo(limit) < 0
                    ? limit
                    : floorValue.add(limit).add(limit);
        }
        BigInteger last = highest;
        if (last == null) {
            last = quotient(top, step, RoundingMode.FLOOR);
            if (lowest != null) {
                last = last.max(lowest);
            }
        }
        BigInteger first = lowest;
        if (first == null) {
            BigDecimal bottom = top.compareTo(limit.negate()) > 0
                    ? limit.negate()
                    : top.subtract(limit).subtract(limit);
            first = quotient(bottom, step, RoundingMode.CEILING).min(last);
        }
        return multiples(step, first, last, false, path, schema);
    }

    /**
     * Draws k × step for a whole k from lowest to highest, exactly. The draw takes 64 more random bits
     * than the count of multiples needs, so every k is equally likely to within 2^-64, with no retry. An
     * integer stays an Integer where every multiple fits an int, and a Long otherwise; a number is a
     * BigDecimal.
     */
    private static SchemaValueProvider multiples(
            BigDecimal step,
            BigInteger lowest,
            BigInteger highest,
            boolean integer,
            String path,
            Map<String, Object> schema) {
        if (lowest.compareTo(highest) > 0) {
            throw emptyRange("multipleOf", path, schema);
        }
        BigInteger count = highest.subtract(lowest).add(BigInteger.ONE);
        int bits = count.bitLength() + 64;
        boolean fitsInt = integer
                && step.multiply(new BigDecimal(lowest)).compareTo(BigDecimal.valueOf(Integer.MIN_VALUE)) >= 0
                && step.multiply(new BigDecimal(highest)).compareTo(BigDecimal.valueOf(Integer.MAX_VALUE)) <= 0;
        return ctx -> {
            BigDecimal value = step.multiply(new BigDecimal(lowest.add(new BigInteger(bits, ctx.random()).mod(count))));
            if (!integer) {
                return plain(value);
            }
            if (fitsInt) {
                return value.intValueExact();
            }
            return value.longValueExact();
        };
    }

    /** The least whole multiple of a step, since an integer that is a multiple of 2.5 is one of 5. */
    private static BigDecimal wholeStep(BigDecimal step) {
        BigDecimal stripped = step.stripTrailingZeros();
        if (stripped.scale() <= 0) {
            return stripped;
        }
        BigInteger unscaled = stripped.unscaledValue();
        return new BigDecimal(unscaled.divide(unscaled.gcd(BigInteger.TEN.pow(stripped.scale()))));
    }

    /** A quotient rounded to a whole number, exactly. */
    private static BigInteger quotient(BigDecimal dividend, BigDecimal divisor, RoundingMode rounding) {
        return dividend.divide(divisor, 0, rounding).toBigIntegerExact();
    }

    /** A decimal without trailing zeros or a positive exponent, so 2.50 is 2.5 and 3E+2 is 300. */
    private static BigDecimal plain(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }

    private static IllegalArgumentException emptyRange(String kind, String path, Map<String, Object> schema) {
        return new IllegalArgumentException("JSON Schema " + kind + " range has no values at " + path + ": " + schema);
    }

    private static SchemaValueProvider resolveArray(
            String fieldName, String path, Map<String, Object> schema, Field field, GeneratorConfig config) {
        // A list of items is the tuple form of the drafts up to 2019-09: each position has its own schema,
        // and additionalItems is the schema of the items after them. A false schema allows no item, so it
        // ends every array at its position.
        Object items = schema.get("items");
        List<SchemaValueProvider> positions = new ArrayList<>();
        int reach = Integer.MAX_VALUE;
        SchemaValueProvider rest;
        if (items instanceof List<?> tuple) {
            for (int position = 0; position < tuple.size(); position++) {
                Object itemSchema = tuple.get(position);
                if (Boolean.FALSE.equals(itemSchema)) {
                    reach = Math.min(reach, position);
                }
                positions.add(itemProvider(
                        itemSchema,
                        fieldName + "[" + position + "]",
                        path + ".items[" + position + "]",
                        field,
                        config));
            }
            Object additional = schema.get("additionalItems");
            if (Boolean.FALSE.equals(additional)) {
                reach = Math.min(reach, tuple.size());
            }
            rest = itemProvider(additional, fieldName + "[]", path + ".additionalItems", field, config);
        } else {
            if (Boolean.FALSE.equals(items)) {
                reach = 0;
            }
            rest = itemProvider(items, fieldName + "[]", path + ".items", field, config);
        }

        // As with string lengths, a missing bound defaults within the declared one. Its default is the
        // tuple, or for other arrays the configured collection size, within what the items allow.
        int natural = items instanceof List<?> ? Math.min(positions.size(), reach) : config.getMaxCollectionSize();
        int naturalMin = items instanceof List<?> ? natural : config.getMinCollectionSize();
        int maxItems = Math.min(
                reach,
                intOrDefault(schema.get("maxItems"), Math.max(natural, intOrDefault(schema.get("minItems"), 0))));
        int minItems = intOrDefault(schema.get("minItems"), Math.min(naturalMin, maxItems));
        if (minItems > maxItems) {
            throw emptyRange("array size", path, schema);
        }

        if (Boolean.TRUE.equals(schema.get("uniqueItems"))) {
            // While parsing, a search with a fixed seed looks for maxItems distinct items, and gives the
            // same verdict on every run. Fewer than minItems fail here; fewer than maxItems are the most an
            // array then holds, so items with few distinct values never spend the whole budget, which
            // would multiply at every level of nested arrays.
            int distinct = distinctItems(
                            positions,
                            rest,
                            new SchemaContext(config.getLocale(), new Random(0L), 0),
                            maxItems,
                            minItems,
                            path)
                    .size();
            return ctx -> distinctItems(positions, rest, ctx, size(ctx, minItems, distinct), minItems, path);
        }
        return ctx -> {
            int size = size(ctx, minItems, maxItems);
            List<Object> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(itemAt(positions, rest, i).generate(ctx));
            }
            return list;
        };
    }

    /** The provider of the items a subschema allows, where true or a missing schema allows any: a string. */
    @SuppressWarnings("unchecked")
    private static SchemaValueProvider itemProvider(
            @Nullable Object itemSchema, String name, String path, Field field, GeneratorConfig config) {
        return itemSchema instanceof Map<?, ?> map
                ? resolveProvider(name, path, (Map<String, Object>) map, field, config)
                : randomText(config);
    }

    /** The provider of the item at a position: the tuple's own, or the one for the items after them. */
    private static SchemaValueProvider itemAt(
            List<SchemaValueProvider> positions, SchemaValueProvider rest, int position) {
        return position < positions.size() ? positions.get(position) : rest;
    }

    private static int size(SchemaContext ctx, int minItems, int maxItems) {
        return minItems == maxItems ? minItems : minItems + ctx.random().nextInt(maxItems - minItems + 1);
    }

    /**
     * Draws items until there are size distinct ones, as JSON values, with at most
     * {@value #UNIQUE_ITEM_ATTEMPTS} draws for each item. Items that run out of distinct values sooner keep
     * the ones found, which fails only below minItems. An item is drawn for the position it would fill, so
     * an array ends at a position whose schema has no new value.
     */
    private static List<Object> distinctItems(
            List<SchemaValueProvider> positions,
            SchemaValueProvider rest,
            SchemaContext ctx,
            int size,
            int minItems,
            String path) {
        List<Object> list = new ArrayList<>(size);
        Set<@Nullable Object> seen = new HashSet<>();
        long attempts = (long) UNIQUE_ITEM_ATTEMPTS * size;
        for (long attempt = 0; attempt < attempts && list.size() < size; attempt++) {
            Object item = itemAt(positions, rest, list.size()).generate(ctx);
            if (seen.add(json(item))) {
                list.add(item);
            }
        }
        if (list.size() < minItems) {
            throw new IllegalArgumentException("JSON Schema uniqueItems cannot be met at " + path + ": " + attempts
                    + " items drawn held " + list.size() + " distinct values, below minItems " + minItems);
        }
        return list;
    }

    /**
     * An object holds every property it declares, as it always has, unless its other keywords ask
     * otherwise. A property that is required, or that a dependency names, is generated through
     * additionalProperties if it is not declared; maxProperties leaves out optional properties, chosen at
     * random, until the object fits, and minProperties adds as few additional properties as it needs. A
     * property whose dependencies cannot be generated is left out. Whatever cannot be met fails here.
     */
    @SuppressWarnings("unchecked")
    private static SchemaValueProvider resolveNestedObject(
            String path, Map<String, Object> schema, Field field, GeneratorConfig config) {
        // The properties under the names they are written with; a name that is no string is left out.
        Map<String, Object> declared = new LinkedHashMap<>();
        if (schema.get("properties") instanceof Map<?, ?> properties) {
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                if (entry.getKey() instanceof String name) {
                    declared.put(name, entry.getValue());
                }
            }
        }
        Object additional = schema.get("additionalProperties");
        boolean extensible = !Boolean.FALSE.equals(additional);
        Map<String, Object> additionalSchema =
                additional instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
        Map<String, List<String>> dependencies = propertyDependencies(schema);
        List<String> required = names(schema.get("required"));

        // A declared property is generated from its schema, where true is the empty schema; false, or a
        // value that is no schema, leaves it out. Any other property comes from additionalProperties.
        Map<String, SchemaValueProvider> providers = new LinkedHashMap<>();
        for (Map.Entry<String, Object> property : declared.entrySet()) {
            Object propertySchema = Boolean.TRUE.equals(property.getValue()) ? Map.of() : property.getValue();
            if (propertySchema instanceof Map<?, ?> map) {
                String name = property.getKey();
                providers.put(
                        name,
                        resolveProvider(name, path + ".properties." + name, (Map<String, Object>) map, field, config));
            }
        }
        // Like every schema, additionalProperties is resolved even where no object needs it.
        SchemaValueProvider additionalValue =
                resolveProvider("", path + ".additionalProperties", additionalSchema, field, config);
        Predicate<String> available =
                name -> providers.containsKey(name) || (extensible && !declared.containsKey(name));

        List<String> base = new ArrayList<>();
        for (String name : required) {
            for (String member : dependents(name, dependencies)) {
                if (!available.test(member)) {
                    throw new IllegalArgumentException((member.equals(name)
                                    ? "JSON Schema required property '" + name + "'"
                                    : "JSON Schema property '" + member
                                            + "', which dependencies require beside required property '" + name
                                            + "',")
                            + " cannot be generated at " + path + ": " + unavailable(member, declared));
                }
                if (!base.contains(member)) {
                    base.add(member);
                }
            }
        }
        // Every declared property whose dependencies can be generated, in the order written, then the
        // others that are required or that a dependency names.
        Set<String> chosen = new HashSet<>(base);
        Set<String> order = new LinkedHashSet<>(declared.keySet());
        order.addAll(base);
        for (String name : providers.keySet()) {
            List<String> needed = dependents(name, dependencies);
            order.addAll(needed);
            if (needed.stream().allMatch(available)) {
                chosen.addAll(needed);
            }
        }
        List<String> members = order.stream().filter(chosen::contains).toList();
        Map<String, SchemaValueProvider> memberProviders = new LinkedHashMap<>();
        for (String name : members) {
            SchemaValueProvider provider = providers.get(name);
            memberProviders.put(
                    name,
                    provider != null
                            ? provider
                            : resolveProvider(name, path + ".additionalProperties", additionalSchema, field, config));
        }

        int fewest = count(schema, "minProperties", 0);
        int most = count(schema, "maxProperties", Integer.MAX_VALUE);
        if (fewest > most) {
            throw emptyRange("property count", path, schema);
        }
        if (base.size() > most) {
            throw new IllegalArgumentException("JSON Schema object at " + path + " must hold " + base.size()
                    + " properties, the required ones and those their dependencies require, above maxProperties "
                    + most);
        }
        if (!extensible && members.size() < fewest) {
            throw new IllegalArgumentException("JSON Schema object at " + path + " can hold at most "
                    + members.size() + " properties, as additionalProperties is false, below minProperties "
                    + fewest);
        }

        // Properties that depend on each other are left out together, after every property that depends
        // on them. Without additional properties to make up for it, the range must be as wide as each such
        // group, so that leaving one out never takes an object below minProperties.
        Map<String, Tie> ties = new LinkedHashMap<>();
        if (members.size() > most) {
            for (String name : members) {
                if (!base.contains(name)) {
                    Set<String> group = new LinkedHashSet<>();
                    Set<String> needing = new HashSet<>();
                    for (String other : members) {
                        if (dependents(other, dependencies).contains(name)) {
                            needing.add(other);
                            if (dependents(name, dependencies).contains(other)) {
                                group.add(other);
                            }
                        }
                    }
                    if (!extensible && most - fewest + 1 < group.size()) {
                        throw new IllegalArgumentException("JSON Schema object at " + path + " can leave out "
                                + group + " only together, as their dependencies tie them, but minProperties "
                                + fewest + " and maxProperties " + most
                                + " leave a narrower range, and additionalProperties is false");
                    }
                    ties.put(name, new Tie(group, needing));
                }
            }
        }

        // Additional properties are named property1, property2, and so on, past every name the schema uses.
        Set<String> taken = new HashSet<>(declared.keySet());
        taken.addAll(required);
        dependencies.forEach((name, needed) -> {
            taken.add(name);
            taken.addAll(needed);
        });
        return ctx -> {
            Set<String> kept = new HashSet<>(members);
            while (kept.size() > most) {
                // Only a group that no other kept property depends on can be left out.
                List<Set<String>> outermost = new ArrayList<>();
                for (Map.Entry<String, Tie> tie : ties.entrySet()) {
                    if (kept.contains(tie.getKey()) && tie.getValue().isOutermost(kept)) {
                        outermost.add(tie.getValue().group());
                    }
                }
                kept.removeAll(outermost.get(ctx.random().nextInt(outermost.size())));
            }
            Map<String, Object> object = new LinkedHashMap<>();
            for (Map.Entry<String, SchemaValueProvider> member : memberProviders.entrySet()) {
                if (kept.contains(member.getKey())) {
                    object.put(member.getKey(), member.getValue().generate(ctx));
                }
            }
            for (int next = 1; object.size() < fewest; next++) {
                String name = "property" + next;
                if (!taken.contains(name)) {
                    object.put(name, additionalValue.generate(ctx));
                }
            }
            return object;
        };
    }

    /**
     * An optional property's tie to others: the group of properties that depend on each other with it,
     * which are left out together, and every property that depends on it, the group included.
     */
    private record Tie(Set<String> group, Set<String> needing) {

        /** Whether no kept property outside the group depends on it, so the group can be left out. */
        boolean isOutermost(Set<String> kept) {
            return needing.stream().filter(kept::contains).allMatch(group::contains);
        }
    }

    /** Why a property cannot be generated. */
    private static String unavailable(String name, Map<String, Object> declared) {
        if (!declared.containsKey(name)) {
            return "it is not in properties, and additionalProperties is false";
        }
        return Boolean.FALSE.equals(declared.get(name)) ? "its schema is false" : "its schema is no schema object";
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
