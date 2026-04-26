/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Loads word phonetic profiles from classpath resources.
 *
 * <p>Format:
 * <pre>
 * onsets=a,b,c
 * nuclei=a,e,i,o,u
 * codas=,n,r,s
 * </pre>
 */
final class WordPhoneticsLoader {

    private WordPhoneticsLoader() {
    }

    static WordPhonetics load(String resourcePath) {
        InputStream is = WordPhoneticsLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new IllegalStateException("Word phonetics resource not found: " + resourcePath);
        }
        return load(is, resourcePath);
    }

    static WordPhonetics load(InputStream is, String resourcePath) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines()
                                       .map(String::trim)
                                       .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                                       .toList();

            String[] onsets = csv(valuesAfter(lines, "onsets="));
            String[] nuclei = csv(valuesAfter(lines, "nuclei="));
            String[] codas = csv(valuesAfter(lines, "codas="));
            return new WordPhonetics(onsets, nuclei, codas);
        } catch (IOException | UncheckedIOException e) {
            throw new IllegalStateException("Failed to read word phonetics resource: " + resourcePath, e);
        }
    }

    private static String valuesAfter(List<String> lines, String prefix) {
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length());
            }
        }
        throw new IllegalStateException("Invalid word phonetics resource, missing '" + prefix + "'");
    }

    private static String[] csv(String value) {
        return value.split(",", -1);
    }
}
