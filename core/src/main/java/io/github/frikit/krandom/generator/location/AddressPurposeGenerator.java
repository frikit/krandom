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
 * Generates locale-aware address purposes, what an address is used for in a record: home, work,
 * billing, shipping, mailing, temporary or previous, for example {@code "Billing address"} in English
 * or {@code "Rechnungsadresse"} in German.
 *
 * <p>The seven purposes cover the address uses of vCard ({@code ADR} types), HL7 FHIR
 * ({@code Address.use}) and the usual CRM and e-commerce address types. English wording follows the
 * locale: {@code en_GB} says "Delivery address" and "Correspondence address", {@code en_AU} "Delivery
 * address" and "Postal address". For the kind of property at an address (residential, commercial, and
 * so on), use {@link AddressTypeGenerator}.
 *
 * <p>Values are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link AddressPurposeDataRegistry}; locales without built-in data fall back to the bundled English
 * list. Every built-in locale lists the same seven purposes in the same order. Output is reproducible
 * for a fixed seed and locale.
 */
public final class AddressPurposeGenerator implements Generator<String> {

    private static final AddressPurposeDataProvider DEFAULT_PROVIDER =
            new BuiltInAddressPurposeDataProvider(Locale.ROOT, "default");

    private final List<String> addressPurposes;
    private final Random random;

    /**
     * Creates an address-purpose generator with default configuration.
     */
    public AddressPurposeGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates an address-purpose generator for the given locale.
     *
     * @param locale locale whose address purposes to use; must not be {@code null}
     */
    public AddressPurposeGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                .locale(Objects.requireNonNull(locale, "locale must not be null"))
                .build());
    }

    /**
     * Creates an address-purpose generator with the specified configuration (locale + optional seed).
     *
     * @param config generator configuration; must not be {@code null}
     */
    public AddressPurposeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        AddressPurposeDataProvider provider =
                config.getRegistryContext().provider(Vocabulary.ADDRESS_PURPOSE, config.getLocale());
        this.addressPurposes = (provider != null ? provider : DEFAULT_PROVIDER).getAddressPurposes();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return addressPurposes.get(random.nextInt(addressPurposes.size()));
    }
}
