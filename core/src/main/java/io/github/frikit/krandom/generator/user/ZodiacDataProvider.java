/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.List;
import java.util.Locale;

/**
 * Contract for a locale-specific set of Western zodiac sign names.
 *
 * <p>The date boundaries of the tropical zodiac are universal; only the sign <em>names</em> differ by
 * language. Implement this interface and register an instance with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerZodiacProvider(ZodiacDataProvider)}
 * to add or override validated localized sign names for one configuration. The global
 * {@link ZodiacDataRegistry} remains a compatibility bridge.
 *
 * <p>{@link #getSigns()} must return exactly 12 names in canonical zodiac order, starting with the
 * Aries sign: {@code [Aries, Taurus, Gemini, Cancer, Leo, Virgo, Libra, Scorpio, Sagittarius,
 * Capricorn, Aquarius, Pisces]}.
 *
 */
public interface ZodiacDataProvider {

    /**
     * The locale this provider supplies sign names for.
     */
    Locale getLocale();

    /**
     * The 12 localized sign names in canonical zodiac order (Aries first).
     */
    List<String> getSigns();
}
