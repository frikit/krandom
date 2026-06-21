/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific set of Chinese zodiac animal names.
 *
 * <p>{@link #getAnimals()} must return exactly 12 names in the cycle order used by
 * {@code animals.get(year mod 12)}, starting with Monkey: {@code [Monkey, Rooster, Dog, Pig, Rat, Ox,
 * Tiger, Rabbit, Dragon, Snake, Horse, Goat]}.
 */
public interface ChineseZodiacDataProvider {

    /**
     * The locale this provider supplies animal names for.
     */
    Locale getLocale();

    /**
     * The 12 localized animal names in cycle order (Monkey first).
     */
    List<String> getAnimals();
}
