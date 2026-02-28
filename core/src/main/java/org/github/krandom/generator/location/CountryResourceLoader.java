/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads country name lists from classpath resource files.
 *
 * <p>Each resource file is a plain UTF-8 text file with one country name per line.
 * Blank lines and lines starting with {@code #} are ignored.
 */
final class CountryResourceLoader {

    private CountryResourceLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Loads country names from a classpath resource file.
     *
     * @param resourcePath path relative to the classpath root
     *                     (e.g. {@code "krandom/countries/en_US_countries.txt"})
     * @return non-empty array of country name strings
     * @throws IllegalStateException if the resource cannot be found or read
     */
    static String[] load(String resourcePath) {
        InputStream is = CountryResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("Country resource not found: " + resourcePath);
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
            throw new IllegalStateException("Failed to read country resource: " + resourcePath, e);
        }
    }
}
