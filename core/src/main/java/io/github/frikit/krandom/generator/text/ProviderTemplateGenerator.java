/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.TextFormatProvider;
import io.github.frikit.krandom.generator.schema.FieldLookup;
import io.github.frikit.krandom.generator.schema.SchemaContext;

import java.util.Objects;
import java.util.Random;

/**
 * Resolves provider tokens inside a string template.
 *
 * <p>Provider tokens use single braces, for example {@code "{firstname} <{email}>"}.
 * Literal braces are escaped as {@code "{{"} and {@code "}}"}. Literal text keeps the
 * existing {@code #}, {@code ?}, and {@code ^} formatting semantics.
 */
public final class ProviderTemplateGenerator implements Generator<String> {

    private final String             template;
    private final FieldLookup        lookup;
    private final TextFormatProvider textFormat;
    private final GeneratorConfig    config;
    private final Random             contextRandom;
    private int recordIndex;

    /**
     * Creates a provider-template generator with default configuration.
     *
     * @param template template text
     */
    public ProviderTemplateGenerator(String template) {
        this(template, GeneratorConfig.defaults());
    }

    /**
     * Creates a deterministic provider-template generator using the supplied seed.
     *
     * @param template template text
     * @param seed     deterministic seed
     */
    public ProviderTemplateGenerator(String template, long seed) {
        this(template, GeneratorConfig.builder().seed(seed).build());
    }

    /**
     * Creates a provider-template generator with explicit configuration.
     *
     * @param template template text
     * @param config   generator configuration
     */
    public ProviderTemplateGenerator(String template, GeneratorConfig config) {
        this(template, new FieldLookup(Objects.requireNonNull(config, "config must not be null")), config);
    }

    /**
     * Creates a provider-template generator backed by an existing field lookup.
     *
     * @param template template text
     * @param lookup   lookup used to resolve provider tokens
     */
    public ProviderTemplateGenerator(String template, FieldLookup lookup) {
        this(template,
             lookup,
             Objects.requireNonNull(lookup, "lookup must not be null").getConfig());
    }

    /**
     * Creates a provider-template generator backed by an existing field lookup and explicit
     * formatting configuration.
     *
     * @param template template text
     * @param lookup   lookup used to resolve provider tokens
     * @param config   configuration used for literal {@code #}, {@code ?}, and {@code ^} formatting
     */
    public ProviderTemplateGenerator(String template, FieldLookup lookup, GeneratorConfig config) {
        this.template = Objects.requireNonNull(template, "template must not be null");
        this.lookup = Objects.requireNonNull(lookup, "lookup must not be null");
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.textFormat = new TextFormatProvider(config);
        this.contextRandom = config.createRandom();
    }

    /**
     * Resolves provider tokens and literal formatting placeholders.
     *
     * @return rendered template
     */
    @Override
    public String generate() {
        StringBuilder out = new StringBuilder(template.length());
        StringBuilder literal = new StringBuilder();
        SchemaContext context = new SchemaContext(config.getLocale(), contextRandom, recordIndex++);

        for (int i = 0; i < template.length(); i++) {
            char c = template.charAt(i);
            if (c == '{') {
                if (hasNext(i) && template.charAt(i + 1) == '{') {
                    literal.append('{');
                    i++;
                    continue;
                }
                flushLiteral(out, literal);
                int end = template.indexOf('}', i + 1);
                if (end < 0) {
                    throw new IllegalArgumentException("Unclosed provider template token starting at index " + i);
                }
                String token = template.substring(i + 1, end).trim();
                if (token.isEmpty()) {
                    throw new IllegalArgumentException("Provider template token must not be blank");
                }
                out.append(String.valueOf(lookup.resolve(token).generate(context)));
                i = end;
            } else if (c == '}' && hasNext(i) && template.charAt(i + 1) == '}') {
                literal.append('}');
                i++;
            } else {
                literal.append(c);
            }
        }
        flushLiteral(out, literal);
        return out.toString();
    }

    private boolean hasNext(int index) {
        return index + 1 < template.length();
    }

    private void flushLiteral(StringBuilder out, StringBuilder literal) {
        if (literal.isEmpty()) {
            return;
        }
        String formatted = textFormat.hexify(textFormat.template(literal.toString()));
        out.append(formatted);
        literal.setLength(0);
    }
}
