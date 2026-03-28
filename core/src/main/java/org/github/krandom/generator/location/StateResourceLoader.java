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
import java.util.ArrayList;
import java.util.List;

/**
 * Loads state/province name lists from classpath resource files.
 *
 * <p>Each resource file is a plain UTF-8 text file with alternating lines:
 * full state name, then abbreviation (or empty line if no abbreviation).
 * Blank lines and lines starting with {@code #} are ignored.
 *
 * <p>Example format:
 * <pre>
 * # State names - English (US)
 * Alabama
 * AL
 * Alaska
 * AK
 * Arizona
 * AZ
 * </pre>
 */
final class StateResourceLoader {

    private StateResourceLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Loads state names and abbreviations from a classpath resource file.
     *
     * @param resourcePath path relative to the classpath root
     *                     (e.g. {@code "krandom/states/en_US_states.txt"})
     * @return a result object containing arrays of state names and abbreviations
     * @throws IllegalStateException if the resource cannot be found or read
     */
    static StateData load(String resourcePath) {
        InputStream is = StateResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("State resource not found: " + resourcePath);
        }
        return load(is, resourcePath);
    }

    static StateData load(InputStream is, String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> states = new ArrayList<>();
            List<String> abbreviations = new ArrayList<>();

            List<String> allLines = reader.lines()
                                          .map(String::trim)
                                          .filter(line -> !line.startsWith("#"))
                                          .toList();

            for (int i = 0; i < allLines.size(); i++) {
                String line = allLines.get(i);
                if (line.isEmpty()) {
                    continue;
                }

                // First line is the state name
                states.add(line);

                // Next line (if exists and not empty) is the abbreviation
                if (i + 1 < allLines.size()) {
                    String nextLine = allLines.get(i + 1);
                    if (!nextLine.isEmpty()) {
                        abbreviations.add(nextLine);
                        i++; // Skip the abbreviation line in next iteration
                    } else {
                        abbreviations.add("");
                    }
                } else {
                    abbreviations.add("");
                }
            }

            return new StateData(
                states.toArray(String[]::new),
                abbreviations.toArray(String[]::new)
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read state resource: " + resourcePath, e);
        }
    }

    /**
     * Container for state names and their abbreviations.
     */
    static class StateData {

        final String[] states;
        final String[] abbreviations;

        StateData(String[] states, String[] abbreviations) {
            this.states = states;
            this.abbreviations = abbreviations;
        }
    }
}
