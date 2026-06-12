/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates strings from DataFaker-style {@code #{Provider.method}} expressions by resolving
 * each token through the {@link ProviderHub}.
 *
 * <p>This is a migration-compatibility adapter: it accepts the most common DataFaker
 * expression tokens (case-insensitive, camelCase or snake_case) and maps them onto kRandom
 * providers. Literal text around tokens is preserved.
 *
 * <pre>{@code
 *   String value = new DataFakerExpressionGenerator("#{Name.firstName} <#{Internet.emailAddress}>")
 *       .generate();
 * }</pre>
 *
 * <p>Unknown tokens fail fast at construction with the list of supported expressions. For
 * kRandom-native templates prefer {@code Generators.ofProviderTemplate("{firstname}-##")}.
 */
public final class DataFakerExpressionGenerator implements Generator<String> {

    private static final Pattern TOKEN = Pattern.compile("#\\{([A-Za-z._]+)}");

    private static final Map<String, String> PROVIDER_TOKENS = Map.ofEntries(
        Map.entry("name.firstname", "person.first_name"),
        Map.entry("name.first_name", "person.first_name"),
        Map.entry("name.lastname", "person.last_name"),
        Map.entry("name.last_name", "person.last_name"),
        Map.entry("name.fullname", "person.full_name"),
        Map.entry("name.full_name", "person.full_name"),
        Map.entry("name.name", "person.full_name"),
        Map.entry("name.username", "person.username"),
        Map.entry("internet.emailaddress", "person.email"),
        Map.entry("internet.email_address", "person.email"),
        Map.entry("internet.username", "person.username"),
        Map.entry("internet.url", "internet.url"),
        Map.entry("internet.domainname", "internet.domain"),
        Map.entry("internet.domain_name", "internet.domain"),
        Map.entry("internet.uuid", "code.uuid"),
        Map.entry("address.city", "address.city"),
        Map.entry("address.country", "address.country"),
        Map.entry("address.state", "address.state"),
        Map.entry("address.streetaddress", "address.street_address"),
        Map.entry("address.street_address", "address.street_address"),
        Map.entry("address.zipcode", "address.postal_code"),
        Map.entry("address.zip_code", "address.postal_code"),
        Map.entry("address.postcode", "address.postal_code"),
        Map.entry("phonenumber.phonenumber", "address.phone_number"),
        Map.entry("phone_number.phone_number", "address.phone_number"),
        Map.entry("company.name", "company.name"),
        Map.entry("company.buzzword", "company.buzzword"),
        Map.entry("company.catchphrase", "company.catch_phrase"),
        Map.entry("company.catch_phrase", "company.catch_phrase"),
        Map.entry("company.industry", "company.industry"),
        Map.entry("money.currency", "finance.currency"),
        Map.entry("lorem.word", "text.word"),
        Map.entry("lorem.sentence", "text.sentence"),
        Map.entry("lorem.paragraph", "text.paragraph"));

    /** Parsed expression: String literals interleaved with provider-backed generators. */
    private final List<Object> segments = new ArrayList<>();

    /**
     * Parses {@code expression} with default configuration.
     *
     * @param expression DataFaker-style expression, e.g. {@code "#{Name.firstName}"}
     */
    public DataFakerExpressionGenerator(String expression) {
        this(expression, GeneratorConfig.defaults());
    }

    /**
     * Parses {@code expression}, resolving providers with the given configuration.
     *
     * @param expression DataFaker-style expression, e.g. {@code "Hi #{Name.firstName}"}
     * @param config     configuration (seed, locale) applied to resolved providers
     * @throws IllegalArgumentException if the expression contains an unsupported token
     */
    public DataFakerExpressionGenerator(String expression, GeneratorConfig config) {
        Objects.requireNonNull(expression, "expression must not be null");
        Objects.requireNonNull(config, "config must not be null");
        ProviderHub hub = new ProviderHub(config);
        Matcher matcher = TOKEN.matcher(expression);
        int last = 0;
        while (matcher.find()) {
            if (matcher.start() > last) {
                segments.add(expression.substring(last, matcher.start()));
            }
            String key = matcher.group(1).toLowerCase(Locale.ROOT);
            String providerName = PROVIDER_TOKENS.get(key);
            if (providerName == null) {
                throw new IllegalArgumentException("Unsupported DataFaker expression '#{" + matcher.group(1)
                                                   + "}'. Supported expressions: " + supportedExpressions());
            }
            segments.add(hub.get(providerName));
            last = matcher.end();
        }
        if (last < expression.length()) {
            segments.add(expression.substring(last));
        }
    }

    /**
     * DataFaker expression tokens supported by this adapter, in lower-case form.
     */
    public static Set<String> supportedExpressions() {
        return new TreeSet<>(PROVIDER_TOKENS.keySet());
    }

    @Override
    public String generate() {
        StringBuilder out = new StringBuilder();
        for (Object segment : segments) {
            out.append(segment instanceof Generator<?> generator ? String.valueOf(generator.generate()) : segment);
        }
        return out.toString();
    }
}
