/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware bank type labels.
 *
 * <p>Labels are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link BankTypeDataRegistry}; locales without built-in data fall back to the bundled English
 * labels.
 */
public final class BankTypeGenerator implements Generator<String> {

    private static final BankTypeDataProvider DEFAULT_PROVIDER =
        new BuiltInBankTypeDataProvider(Locale.ROOT, "default");

    private final List<String> bankTypes;
    private final Random       random;

    public BankTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BankTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public BankTypeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        BankTypeDataProvider provider = config.getRegistryContext().bankTypeProvider(config.getLocale());
        this.bankTypes = (provider != null ? provider : DEFAULT_PROVIDER).getBankTypes();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return bankTypes.get(random.nextInt(bankTypes.size()));
    }
}
