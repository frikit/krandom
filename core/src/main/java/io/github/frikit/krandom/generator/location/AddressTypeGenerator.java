/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.location;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Vocabulary;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware address types by property use: residential, commercial, industrial,
 * agricultural, institutional or mixed-use, for example {@code "Residential"} in English or
 * {@code "Wohnen"} in German.
 *
 * <p>The six types follow common land-use classifications, the same axis carriers use when they tell
 * a residential from a commercial delivery address. For what an address is used for in a record
 * (home, billing, shipping, and so on), use {@link AddressPurposeGenerator}.
 *
 * <p>Values are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link AddressTypeDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Every built-in locale lists the same six types in the same order. Output is reproducible for
 * a fixed seed and locale.
 */
public final class AddressTypeGenerator implements Generator<String> {

    private static final AddressTypeDataProvider DEFAULT_PROVIDER =
            new BuiltInAddressTypeDataProvider(Locale.ROOT, "default");

    private final List<String> addressTypes;
    private final Random random;

    /**
     * Creates an address-type generator with default configuration.
     */
    public AddressTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an address-type generator for the given locale.
     *
     * @param locale locale whose address types to use; must not be {@code null}
     */
    public AddressTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                .locale(Objects.requireNonNull(locale, "locale must not be null"))
                .build());
    }

    /**
     * Creates an address-type generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public AddressTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        AddressTypeDataProvider provider =
                config.getRegistryContext().provider(Vocabulary.ADDRESS_TYPE, config.getLocale());
        this.addressTypes = (provider != null ? provider : DEFAULT_PROVIDER).getAddressTypes();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return addressTypes.get(random.nextInt(addressTypes.size()));
    }
}
