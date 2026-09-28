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
 * Generates locale-aware fictional bank names.
 *
 * <p>Names are resolved through the configuration's {@code DataRegistryContext}, which defaults to
 * {@link BankNameDataRegistry}; locales without built-in data fall back to the bundled English
 * names.
 */
public final class BankNameGenerator implements Generator<String> {

    private static final BankNameDataProvider DEFAULT_PROVIDER =
        new BuiltInBankNameDataProvider(Locale.ROOT, "default");

    private final List<String> bankNames;
    private final Random       random;

    public BankNameGenerator() {
        this(GeneratorConfig.defaults());
    }

    public BankNameGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public BankNameGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        BankNameDataProvider provider = config.getRegistryContext().bankNameProvider(config.getLocale());
        this.bankNames = (provider != null ? provider : DEFAULT_PROVIDER).getBankNames();
        this.random = config.createRandom();
    }

    @Override
    public String generate() {
        return bankNames.get(random.nextInt(bankNames.size()));
    }
}
