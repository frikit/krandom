/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.location;

import org.github.krandom.generator.locale.SupportedLocale;

import java.util.Locale;

/**
 * Built-in state provider backed by classpath state resources.
 */
final class BuiltInStateDataProvider implements StateDataProvider {

    private final Locale locale;
    private final String[] states;
    private final String[] abbreviations;

    BuiltInStateDataProvider(SupportedLocale supportedLocale) {
        this.locale = supportedLocale.locale();
        String resourcePrefix = supportedLocale.resourcePrefix();
        StateResourceLoader.StateData data = StateResourceLoader.load("krandom/states/" + resourcePrefix + "_states.txt");
        this.states = data.states;
        this.abbreviations = data.abbreviations;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String[] getStates() {
        return states.clone();
    }

    @Override
    public String[] getAbbreviations() {
        return abbreviations.clone();
    }
}
