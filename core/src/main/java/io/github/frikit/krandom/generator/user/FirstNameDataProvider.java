/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import java.util.Locale;

/**
 * Contract for a locale-specific first-name data source.
 *
 * <p>Implement this interface and register an instance on a configuration-scoped context with
 * {@link io.github.frikit.krandom.generator.DataRegistryContext.Builder#registerFirstNameProvider(FirstNameDataProvider)}
 * to extend or override first-name data for any locale — including locales not built into the library.
 *
 * <pre>{@code
 * DataRegistryContext context = DataRegistryContext.builder()
 *     .registerFirstNameProvider(new FirstNameDataProvider() {
 *         public Locale getLocale() { return Locale.of("ko", "KR"); }
 *         public String[] getMaleFirstNames()   { return new String[]{"민준", "서준", "예준"}; }
 *         public String[] getFemaleFirstNames() { return new String[]{"서연", "서윤", "지우"}; }
 *     })
 *     .build();
 * GeneratorConfig config = GeneratorConfig.builder()
 *     .locale(Locale.of("ko", "KR"))
 *     .registryContext(context)
 *     .build();
 * FirstNameGenerator gen = new FirstNameGenerator(config);
 * }</pre>
 *
 * <p>The built-in baseline is seeded by {@link FirstNameDataRegistry} from
 * {@link io.github.frikit.krandom.generator.locale.SupportedLocale}.
 */
public interface FirstNameDataProvider {

    /**
     * The locale this provider supplies data for.
     */
    Locale getLocale();

    /**
     * Male first names for this locale.
     */
    String[] getMaleFirstNames();

    /**
     * Female first names for this locale.
     */
    String[] getFemaleFirstNames();
}
