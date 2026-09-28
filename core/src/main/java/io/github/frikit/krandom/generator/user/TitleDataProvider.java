/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific title data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerTitleProvider(TitleDataProvider)}
 * to extend or override title data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .registerTitleProvider(new TitleDataProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String[] getTitles() { return new String[]{"씨", "님", "박사", "교수"}; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .registryContext(context)
 *     .build();
 * TitleGenerator gen = new TitleGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link TitleDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}. Custom registrations take
 * precedence over the built-in data for the same locale key.
 */
public interface TitleDataProvider {

    /**
     * The locale this provider supplies data for.
     *
     * @return non-null locale
     */
    Locale getLocale();

    /**
     * Returns the title strings for this locale.
     *
     * @return non-null, non-empty array of title strings
     */
    String[] getTitles();
}
