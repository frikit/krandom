/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware restaurant cuisine/type names, e.g. {@code "Steakhouse"} for English or
 * {@code "Стейк-хаус"} for Russian.
 *
 * <p>Types are resolved from {@link RestaurantTypeDataRegistry} for the configured locale; locales
 * without a built-in file fall back to the bundled English types.
 *
 * <pre>{@code
 *   String en = new RestaurantTypeGenerator().generate();                     // e.g. "Italian"
 *   String ru = new RestaurantTypeGenerator(Locale.of("ru","RU")).generate(); // e.g. "Итальянский"
 * }</pre>
 */
public final class RestaurantTypeGenerator implements Generator<String> {

    private static final RestaurantTypeDataProvider DEFAULT_PROVIDER =
        new BuiltInRestaurantTypeDataProvider(Locale.ROOT, "krandom/restaurant_types/default.txt");

    private final List<String> types;
    private final Random random;

    /**
     * Creates a generator using the default configuration (and its locale).
     */
    public RestaurantTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator for the given locale.
     *
     * @param locale the locale whose type names to use; falls back to English if no built-in file
     *               exists; must not be {@code null}
     */
    public RestaurantTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a generator from explicit configuration (locale + optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public RestaurantTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        RestaurantTypeDataProvider provider = RestaurantTypeDataRegistry.forLocale(config.getLocale());
        if (provider == null) {
            provider = DEFAULT_PROVIDER;
        }
        this.types = provider.getTypes();
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random restaurant cuisine/type name in the configured locale.
     *
     * @return a localized type such as {@code "Italian"}; never {@code null}
     */
    @Override
    public String generate() {
        return types.get(random.nextInt(types.size()));
    }
}
