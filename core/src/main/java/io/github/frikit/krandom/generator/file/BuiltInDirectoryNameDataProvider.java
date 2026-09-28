/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.file;

import io.github.frikit.krandom.generator.locale.SupportedLocale;
import io.github.frikit.krandom.generator.user.LocaleTextResourceLoader;

import java.util.List;
import java.util.Locale;

/**
 * Built-in directory names backed by classpath resources.
 *
 * <p>Entries are loaded from {@code krandom/directory_names/<locale>.txt}, one per line; blank lines and {@code #}
 * comments are ignored.
 */
final class BuiltInDirectoryNameDataProvider implements DirectoryNameDataProvider {

    private final Locale locale;
    private final List<String> directoryNames;

    BuiltInDirectoryNameDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), supportedLocale.resourcePrefix());
    }

    BuiltInDirectoryNameDataProvider(Locale locale, String resourcePrefix) {
        this.locale = locale;
        this.directoryNames = List.of(LocaleTextResourceLoader.load("krandom/directory_names/" + resourcePrefix + ".txt"));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getDirectoryNames() {
        return directoryNames;
    }
}
