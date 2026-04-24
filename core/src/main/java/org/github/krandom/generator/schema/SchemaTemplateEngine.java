/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.text.TemplateStringGenerator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves registry-backed schema templates and payload shells.
 */
final class SchemaTemplateEngine {

    private static final Pattern PLACEHOLDER      = Pattern.compile("\\{\\{\\s*([^{}]+?)\\s*}}");
    private static final Pattern FULL_PLACEHOLDER = Pattern.compile("^\\{\\{\\s*([^{}]+?)\\s*}}$");

    private final FieldLookup              lookup;
    private final TemplateStringGenerator  templateGenerator;

    SchemaTemplateEngine(FieldLookup lookup, GeneratorConfig config) {
        this.lookup = Objects.requireNonNull(lookup, "lookup must not be null");
        this.templateGenerator = new TemplateStringGenerator("", Objects.requireNonNull(config, "config must not be null"));
    }

    SchemaValueProvider template(String template) {
        String value = Objects.requireNonNull(template, "template must not be null");
        return SchemaValueProvider.withJsonSchema(context -> renderInterpolatedString(value, context),
                                                  JsonSchemaSupport.string());
    }

    SchemaValueProvider templatePayload(Object template) {
        Objects.requireNonNull(template, "template must not be null");
        return SchemaValueProvider.withJsonSchema(context -> renderPayload(template, context),
                                                  inferPayloadSchema(template));
    }

    private Object renderPayload(Object template, SchemaContext context) {
        if (template == null) {
            return null;
        }
        if (template instanceof String text) {
            Matcher matcher = FULL_PLACEHOLDER.matcher(text);
            if (matcher.matches()) {
                return lookup.resolve(matcher.group(1)).generate(context);
            }
            return renderInterpolatedString(text, context);
        }
        if (template instanceof Map<?, ?> map) {
            Map<Object, Object> resolved = new LinkedHashMap<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                resolved.put(entry.getKey(), renderPayload(entry.getValue(), context));
            }
            return resolved;
        }
        if (template instanceof Iterable<?> items) {
            List<Object> resolved = new ArrayList<>();
            for (Object item : items) {
                resolved.add(renderPayload(item, context));
            }
            return resolved;
        }
        if (template.getClass().isArray()) {
            int length = Array.getLength(template);
            Object[] resolved = new Object[length];
            for (int i = 0; i < length; i++) {
                resolved[i] = renderPayload(Array.get(template, i), context);
            }
            return resolved;
        }
        return template;
    }

    private Map<String, Object> inferPayloadSchema(Object template) {
        if (template == null) {
            return JsonSchemaSupport.nullType();
        }
        if (template instanceof String text) {
            Matcher matcher = FULL_PLACEHOLDER.matcher(text);
            if (matcher.matches()) {
                return lookup.resolve(matcher.group(1)).jsonSchema();
            }
            return JsonSchemaSupport.string();
        }
        if (template instanceof Map<?, ?> map) {
            Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    properties.put(key, inferPayloadSchema(entry.getValue()));
                }
            }
            return JsonSchemaSupport.object(properties);
        }
        if (template instanceof Iterable<?> items) {
            Object firstNonNull = null;
            for (Object item : items) {
                if (item != null) {
                    firstNonNull = item;
                    break;
                }
            }
            return JsonSchemaSupport.array(firstNonNull == null
                                           ? JsonSchemaSupport.nullType()
                                           : inferPayloadSchema(firstNonNull));
        }
        if (template.getClass().isArray()) {
            int length = Array.getLength(template);
            for (int i = 0; i < length; i++) {
                Object item = Array.get(template, i);
                if (item != null) {
                    return JsonSchemaSupport.array(inferPayloadSchema(item));
                }
            }
            return JsonSchemaSupport.array(JsonSchemaSupport.nullType());
        }
        return JsonSchemaSupport.infer(template);
    }

    private String renderInterpolatedString(String template, SchemaContext context) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder(template.length());
        int cursor = 0;
        while (matcher.find()) {
            appendLiteral(out, template.substring(cursor, matcher.start()));
            Object value = lookup.resolve(matcher.group(1)).generate(context);
            out.append(String.valueOf(value));
            cursor = matcher.end();
        }
        appendLiteral(out, template.substring(cursor));
        return out.toString();
    }

    private void appendLiteral(StringBuilder out, String literal) {
        if (!literal.isEmpty()) {
            out.append(templateGenerator.bothify(literal));
        }
    }
}
