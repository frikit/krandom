/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates currency codes, names, and symbols conforming to ISO 4217 standards.
 *
 * <p>This generator produces realistic currency data for testing purposes, supporting
 * 50+ major world currencies with special emphasis on currencies used in the 10 supported
 * locales (USD, EUR, GBP, AUD, BRL, JPY, CNY).
 *
 * <p><strong>Supported Locales and Currencies:</strong>
 * <ul>
 *   <li><strong>en_US</strong> → USD (United States Dollar) - $</li>
 *   <li><strong>en_GB</strong> → GBP (British Pound Sterling) - £</li>
 *   <li><strong>en_AU</strong> → AUD (Australian Dollar) - A$</li>
 *   <li><strong>de_DE</strong> → EUR (Euro) - €</li>
 *   <li><strong>fr_FR</strong> → EUR (Euro) - €</li>
 *   <li><strong>es_ES</strong> → EUR (Euro) - €</li>
 *   <li><strong>it_IT</strong> → EUR (Euro) - €</li>
 *   <li><strong>pt_BR</strong> → BRL (Brazilian Real) - R$</li>
 *   <li><strong>ja_JP</strong> → JPY (Japanese Yen) - ¥</li>
 *   <li><strong>zh_CN</strong> → CNY (Chinese Yuan Renminbi) - ¥</li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * // Random currency from any supported currency
 * CurrencyGenerator gen = new CurrencyGenerator();
 * String code = gen.generate();              // "JPY"
 * String name = gen.getName();               // "Japanese Yen"
 * String symbol = gen.getSymbol();           // "¥"
 * String numeric = gen.getNumericCode();     // "392"
 * 
 * // Get complete currency information
 * CurrencyInfo info = gen.generateWithInfo();
 * System.out.println(info.code());           // "EUR"
 * System.out.println(info.name());           // "Euro"
 * System.out.println(info.symbol());         // "€"
 * System.out.println(info.numericCode());    // "978"
 * }</pre>
 *
 * <p><strong>Locale-Aware Generation:</strong>
 * <pre>{@code
 * // Generate currency for specific locale
 * Locale usLocale = new Locale("en", "US");
 * String usCurrency = gen.generate(usLocale);  // "USD"
 * 
 * Locale jpLocale = new Locale("ja", "JP");
 * CurrencyInfo jpInfo = gen.generateWithInfo(jpLocale);
 * System.out.println(jpInfo.code());           // "JPY"
 * System.out.println(jpInfo.symbol());         // "¥"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 * // Reproducible currency generation
 * CurrencyGenerator gen1 = new CurrencyGenerator(new GeneratorConfig(12345L));
 * CurrencyGenerator gen2 = new CurrencyGenerator(new GeneratorConfig(12345L));
 * gen1.generate().equals(gen2.generate());  // true (same sequence)
 * }</pre>
 *
 * <p><strong>Batch Generation:</strong>
 * <pre>{@code
 * // Generate multiple currencies
 * List<String> codes = gen.generateList(10);
 * Stream<CurrencyInfo> stream = gen.streamWithInfo().limit(5);
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads. Each instance uses its own
 * Random instance for generating values.
 *
 * <p><strong>ISO 4217 Compliance:</strong>
 * All generated currency data conforms to ISO 4217 standards, including alphabetic codes,
 * numeric codes, currency names, and symbols.
 *
 * @see Currency
 * @see CurrencyInfo
 */
public final class CurrencyGenerator implements Generator<String> {
    
    private static final Currency[] ALL_CURRENCIES = Currency.values();
    
    private final GeneratorConfig config;
    private final Random random;
    
    /**
     * Creates a new CurrencyGenerator with default configuration.
     */
    public CurrencyGenerator() {
        this(GeneratorConfig.defaults());
    }
    
    /**
     * Creates a new CurrencyGenerator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CurrencyGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }
    
    /**
     * Generates a random ISO 4217 currency code.
     *
     * <p>Returns a 3-letter currency code selected randomly from all supported currencies.
     *
     * @return a random currency code (e.g., "USD", "EUR", "JPY")
     */
    @Override
    public String generate() {
        return getRandomCurrency().getCode();
    }
    
    /**
     * Generates a currency code appropriate for the given locale.
     *
     * <p>Returns the primary currency for the locale's country:
     * <ul>
     *   <li>en_US → USD</li>
     *   <li>en_GB → GBP</li>
     *   <li>en_AU → AUD</li>
     *   <li>de_DE, fr_FR, es_ES, it_IT → EUR</li>
     *   <li>pt_BR → BRL</li>
     *   <li>ja_JP → JPY</li>
     *   <li>zh_CN → CNY</li>
     * </ul>
     *
     * <p>If the locale is null or not recognized, returns a random currency code.
     *
     * @param locale the locale to generate currency for
     * @return the currency code for the locale, or a random code if locale is not recognized
     */
    public String generate(Locale locale) {
        Currency currency = Currency.forLocale(locale);
        if (currency == null) {
            return generate();
        }
        return currency.getCode();
    }
    
    /**
     * Generates complete currency information for a random currency.
     *
     * <p>Returns a CurrencyInfo object containing code, name, symbol, and numeric code.
     *
     * @return complete information for a random currency
     */
    public CurrencyInfo generateWithInfo() {
        return getRandomCurrency().toInfo();
    }
    
    /**
     * Generates complete currency information for the given locale.
     *
     * <p>Returns a CurrencyInfo object for the locale's primary currency.
     * If the locale is null or not recognized, returns information for a random currency.
     *
     * @param locale the locale to generate currency for
     * @return complete information for the locale's currency, or a random currency if locale is not recognized
     */
    public CurrencyInfo generateWithInfo(Locale locale) {
        Currency currency = Currency.forLocale(locale);
        if (currency == null) {
            return generateWithInfo();
        }
        return currency.toInfo();
    }
    
    /**
     * Generates a random currency name.
     *
     * <p>Returns the full official name of a randomly selected currency.
     *
     * @return a random currency name (e.g., "United States Dollar", "Euro")
     */
    public String getName() {
        return getRandomCurrency().getName();
    }
    
    /**
     * Generates a currency name for the given locale.
     *
     * <p>Returns the name of the locale's primary currency.
     * If the locale is null or not recognized, returns a random currency name.
     *
     * @param locale the locale to generate currency name for
     * @return the currency name for the locale, or a random name if locale is not recognized
     */
    public String getName(Locale locale) {
        Currency currency = Currency.forLocale(locale);
        if (currency == null) {
            return getName();
        }
        return currency.getName();
    }
    
    /**
     * Generates a random currency symbol.
     *
     * <p>Returns the symbol of a randomly selected currency.
     *
     * @return a random currency symbol (e.g., "$", "€", "£", "¥")
     */
    public String getSymbol() {
        return getRandomCurrency().getSymbol();
    }
    
    /**
     * Generates a currency symbol for the given locale.
     *
     * <p>Returns the symbol of the locale's primary currency.
     * If the locale is null or not recognized, returns a random currency symbol.
     *
     * @param locale the locale to generate currency symbol for
     * @return the currency symbol for the locale, or a random symbol if locale is not recognized
     */
    public String getSymbol(Locale locale) {
        Currency currency = Currency.forLocale(locale);
        if (currency == null) {
            return getSymbol();
        }
        return currency.getSymbol();
    }
    
    /**
     * Generates a random ISO 4217 numeric code.
     *
     * <p>Returns the 3-digit numeric code of a randomly selected currency.
     *
     * @return a random currency numeric code (e.g., "840", "978", "392")
     */
    public String getNumericCode() {
        return getRandomCurrency().getNumericCode();
    }
    
    /**
     * Generates a currency numeric code for the given locale.
     *
     * <p>Returns the numeric code of the locale's primary currency.
     * If the locale is null or not recognized, returns a random currency numeric code.
     *
     * @param locale the locale to generate currency numeric code for
     * @return the currency numeric code for the locale, or a random code if locale is not recognized
     */
    public String getNumericCode(Locale locale) {
        Currency currency = Currency.forLocale(locale);
        if (currency == null) {
            return getNumericCode();
        }
        return currency.getNumericCode();
    }

    /**
     * Generates a map-shaped currency payload similar to Faker's {@code currency()} dict contract.
     *
     * @return map with keys: {@code code}, {@code name}, {@code symbol}, {@code numeric_code}
     */
    public Map<String, String> generateAsMap() {
        CurrencyInfo info = generateWithInfo();
        return toMap(info);
    }

    /**
     * Generates a locale-aware map-shaped currency payload.
     *
     * @param locale locale used to select primary currency
     * @return map with keys: {@code code}, {@code name}, {@code symbol}, {@code numeric_code}
     */
    public Map<String, String> generateAsMap(Locale locale) {
        CurrencyInfo info = generateWithInfo(locale);
        return toMap(info);
    }
    
    /**
     * Generates a stream of complete currency information.
     *
     * <p>Each element in the stream is a CurrencyInfo object containing
     * code, name, symbol, and numeric code for a randomly selected currency.
     *
     * @return an infinite stream of CurrencyInfo objects
     */
    public java.util.stream.Stream<CurrencyInfo> streamWithInfo() {
        return java.util.stream.Stream.generate(this::generateWithInfo);
    }
    
    /**
     * Generates a list of complete currency information.
     *
     * <p>Returns a list of CurrencyInfo objects, each containing
     * code, name, symbol, and numeric code for a randomly selected currency.
     *
     * @param count the number of currency info objects to generate
     * @return a list of CurrencyInfo objects
     * @throws IllegalArgumentException if count is negative
     */
    public java.util.List<CurrencyInfo> generateListWithInfo(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
        return streamWithInfo().limit(count).toList();
    }
    
    /**
     * Returns a random currency from all supported currencies.
     *
     * @return a randomly selected Currency
     */
    private Currency getRandomCurrency() {
        return ALL_CURRENCIES[random.nextInt(ALL_CURRENCIES.length)];
    }

    private static Map<String, String> toMap(CurrencyInfo info) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("code", info.code());
        map.put("name", info.name());
        map.put("symbol", info.symbol());
        map.put("numeric_code", info.numericCode());
        return map;
    }
}
