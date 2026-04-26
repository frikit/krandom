/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Loads street-address component lists from classpath resource files.
 */
final class StreetAddressResourceLoader {

    private StreetAddressResourceLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Loads lines from a classpath resource.
     *
     * @param resourcePath path relative to classpath root
     * @return non-empty array of values
     */
    static String[] load(String resourcePath) {
        InputStream is = StreetAddressResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("Street resource not found: " + resourcePath);
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
            throw new IllegalStateException("Failed to read street resource: " + resourcePath, e);
        }
    }
}
