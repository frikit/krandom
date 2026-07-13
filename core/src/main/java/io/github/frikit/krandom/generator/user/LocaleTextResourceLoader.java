/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads single-entry-per-line text datasets (names, professions, titles, …) from classpath
 * resource files.
 *
 * <p>Each resource file is a plain UTF-8 text file with one entry per line. Blank lines and lines
 * starting with {@code #} (header comments) are ignored.
 */
public final class LocaleTextResourceLoader {

    private LocaleTextResourceLoader() {
    }

    /**
     * Loads entries from a classpath resource file.
     *
     * @param resourcePath path relative to the classpath root (e.g. {@code "krandom/professions/en_US.txt"})
     * @return non-empty array of entry strings
     * @throws IllegalStateException if the resource cannot be found or read
     */
    public static String[] load(String resourcePath) {
        InputStream is = LocaleTextResourceLoader.class.getResourceAsStream("/" + resourcePath);
        if (is == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }
        return load(is, resourcePath);
    }

    static String[] load(InputStream is, String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines()
                         .map(String::trim)
                         .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                         .toArray(String[]::new);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read resource: " + resourcePath, e);
        }
    }
}
