/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Built-in blood-type distribution backed by classpath resources.
 *
 * <p>Distributions are loaded from {@code krandom/bloodtypes/<locale>.txt}, one {@code TYPE WEIGHT}
 * entry per line (for example {@code "O+ 37"}). Blank lines and {@code #} comments are ignored.
 */
final class BuiltInBloodTypeDataProvider implements BloodTypeDataProvider {

    private final Locale locale;
    private final List<String> types;
    private final List<Integer> weights;

    BuiltInBloodTypeDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "krandom/bloodtypes/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInBloodTypeDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        String[] lines = LocaleTextResourceLoader.load(resourcePath);
        List<String> parsedTypes = new ArrayList<>(lines.length);
        List<Integer> parsedWeights = new ArrayList<>(lines.length);
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            parsedTypes.add(parts[0]);
            parsedWeights.add(Integer.parseInt(parts[1]));
        }
        this.types = List.copyOf(parsedTypes);
        this.weights = List.copyOf(parsedWeights);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getTypes() {
        return types;
    }

    @Override
    public List<Integer> getWeights() {
        return weights;
    }
}
