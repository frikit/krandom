/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.e2e.support;

import io.github.frikit.krandom.generator.Generators;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Small helper that derives coherent, name-based email addresses and usernames, so a generated
 * identity reads like one real person instead of three unrelated values.
 */
public final class Emails {

    private static final String[] DOMAINS = {"gmail.com", "outlook.com", "yahoo.com", "proton.me", "icloud.com"};

    private Emails() {
    }

    /** The {@code first.last} local-part (lower-case ASCII; accents and punctuation stripped). */
    public static String localPart(String first, String last) {
        return slug(first) + "." + slug(last);
    }

    /** A {@code first.last@domain} address whose domain is chosen reproducibly from the seed. */
    public static String fromName(String first, String last, long seed) {
        String domain = DOMAINS[Generators.ofInt(0, DOMAINS.length - 1, seed).generate()];
        return localPart(first, last) + "@" + domain;
    }

    /** A handle-style username such as {@code hlloyd42}, derived from the same name. */
    public static String username(String first, String last, long seed) {
        int suffix = Generators.ofInt(10, 999, seed).generate();
        return slug(first).charAt(0) + slug(last) + suffix;
    }

    private static String slug(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String cleaned = normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return cleaned.isEmpty() ? "user" : cleaned;
    }
}
