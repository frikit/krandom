/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.LocaleDataProvider;
import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific set of personal-pronoun sets.
 *
 * <p>Each entry is a {@code subject/object} pair (e.g. {@code "they/them"}, {@code "он/его"}). Implement
 * this interface and register an instance with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#register(io.github.frikit.krandom.generator.Vocabulary, io.github.frikit.krandom.generator.LocaleDataProvider)}
 * to add or override validated pronoun sets for one configuration. {@link PronounDataRegistry}
 * holds the built-in pronoun sets served by
 * {@link io.github.frikit.krandom.generator.DataRegistryContext#globalDefault()}.
 */
public interface PronounDataProvider extends LocaleDataProvider {

    /**
     * The locale this provider supplies pronoun sets for.
     */
    Locale getLocale();

    /**
     * The localized pronoun sets, each in {@code subject/object} form; must be non-empty.
     */
    List<String> getPronounSets();
}
