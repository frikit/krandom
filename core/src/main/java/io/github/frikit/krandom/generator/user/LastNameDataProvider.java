/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific last-name data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerLastNameProvider(LastNameDataProvider)}
 * to extend or override last-name data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .registerLastNameProvider(new LastNameDataProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String[] getLastNames() { return new String[]{"김", "이", "박", "최", "정"}; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .registryContext(context)
 *     .build();
 * LastNameGenerator gen = new LastNameGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link LastNameDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 */
public interface LastNameDataProvider {

    /**
     * The locale this provider supplies data for.
     */
    Locale getLocale();

    /**
     * Last names (family names / surnames) for this locale.
     */
    String[] getLastNames();
}
