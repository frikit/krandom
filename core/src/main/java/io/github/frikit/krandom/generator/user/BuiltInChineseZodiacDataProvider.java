/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.locale.SupportedLocale;

import java.util.List;
import java.util.Locale;

/**
 * Built-in Chinese zodiac animal names backed by classpath resources.
 *
 * <p>Names are loaded from {@code krandom/chinese_zodiac/<locale>.txt}: one localized animal name per
 * line, in cycle order (Monkey first). Blank lines and {@code #} comments are ignored.
 */
final class BuiltInChineseZodiacDataProvider implements ChineseZodiacDataProvider {

    private final Locale locale;
    private final List<String> animals;

    BuiltInChineseZodiacDataProvider(SupportedLocale supportedLocale) {
        this(supportedLocale.locale(), "krandom/chinese_zodiac/" + supportedLocale.resourcePrefix() + ".txt");
    }

    BuiltInChineseZodiacDataProvider(Locale locale, String resourcePath) {
        this.locale = locale;
        this.animals = List.of(LocaleTextResourceLoader.load(resourcePath));
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public List<String> getAnimals() {
        return animals;
    }
}
