/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import java.util.Locale;

/**
 * Enumeration of major world currencies conforming to ISO 4217 standards.
 *
 * <p>This enum includes the 50 most widely used currencies, with special focus on currencies
 * used in the 10 supported locales (USD, EUR, GBP, AUD, BRL, JPY, CNY).
 *
 * <p>Each currency includes:
 * <ul>
 *   <li>ISO 4217 3-letter alphabetic code</li>
 *   <li>Full official currency name</li>
 *   <li>Currency symbol</li>
 *   <li>ISO 4217 3-digit numeric code</li>
 * </ul>
 */
public enum Currency {

    // Primary currencies for supported locales (priority order)
    USD("USD", "United States Dollar", "$", "840"),
    EUR("EUR", "Euro", "€", "978"),
    GBP("GBP", "British Pound Sterling", "£", "826"),
    AUD("AUD", "Australian Dollar", "A$", "036"),
    BRL("BRL", "Brazilian Real", "R$", "986"),
    JPY("JPY", "Japanese Yen", "¥", "392"),
    CNY("CNY", "Chinese Yuan Renminbi", "¥", "156"),

    // Other major world currencies (alphabetical)
    AED("AED", "United Arab Emirates Dirham", "د.إ", "784"),
    AFN("AFN", "Afghan Afghani", "؋", "971"),
    ARS("ARS", "Argentine Peso", "$", "032"),
    CAD("CAD", "Canadian Dollar", "C$", "124"),
    CHF("CHF", "Swiss Franc", "CHF", "756"),
    CLP("CLP", "Chilean Peso", "$", "152"),
    COP("COP", "Colombian Peso", "$", "170"),
    CZK("CZK", "Czech Koruna", "Kč", "203"),
    DKK("DKK", "Danish Krone", "kr", "208"),
    EGP("EGP", "Egyptian Pound", "£", "818"),
    HKD("HKD", "Hong Kong Dollar", "HK$", "344"),
    HUF("HUF", "Hungarian Forint", "Ft", "348"),
    IDR("IDR", "Indonesian Rupiah", "Rp", "360"),
    ILS("ILS", "Israeli New Shekel", "₪", "376"),
    INR("INR", "Indian Rupee", "₹", "356"),
    KRW("KRW", "South Korean Won", "₩", "410"),
    MXN("MXN", "Mexican Peso", "$", "484"),
    MYR("MYR", "Malaysian Ringgit", "RM", "458"),
    NOK("NOK", "Norwegian Krone", "kr", "578"),
    NZD("NZD", "New Zealand Dollar", "NZ$", "554"),
    PHP("PHP", "Philippine Peso", "₱", "608"),
    PKR("PKR", "Pakistani Rupee", "₨", "586"),
    PLN("PLN", "Polish Złoty", "zł", "985"),
    RON("RON", "Romanian Leu", "lei", "946"),
    RUB("RUB", "Russian Ruble", "₽", "643"),
    SAR("SAR", "Saudi Riyal", "﷼", "682"),
    SEK("SEK", "Swedish Krona", "kr", "752"),
    SGD("SGD", "Singapore Dollar", "S$", "702"),
    THB("THB", "Thai Baht", "฿", "764"),
    TRY("TRY", "Turkish Lira", "₺", "949"),
    TWD("TWD", "New Taiwan Dollar", "NT$", "901"),
    UAH("UAH", "Ukrainian Hryvnia", "₴", "980"),
    VND("VND", "Vietnamese Dong", "₫", "704"),
    ZAR("ZAR", "South African Rand", "R", "710"),

    // Additional commonly used currencies
    BGN("BGN", "Bulgarian Lev", "лв", "975"),
    CRC("CRC", "Costa Rican Colón", "₡", "188"),
    HRK("HRK", "Croatian Kuna", "kn", "191"),
    ISK("ISK", "Icelandic Króna", "kr", "352"),
    KWD("KWD", "Kuwaiti Dinar", "د.ك", "414"),
    PEN("PEN", "Peruvian Sol", "S/", "604"),
    QAR("QAR", "Qatari Riyal", "﷼", "634"),
    UYU("UYU", "Uruguayan Peso", "$U", "858");

    private final String code;
    private final String name;
    private final String symbol;
    private final String numericCode;

    Currency(String code, String name, String symbol, String numericCode) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.numericCode = numericCode;
    }

    /**
     * Returns the primary currency for the given locale.
     *
     * <p>Mappings for supported locales:
     * <ul>
     *   <li>en_US → USD</li>
     *   <li>en_GB → GBP</li>
     *   <li>en_AU → AUD</li>
     *   <li>de_DE → EUR</li>
     *   <li>fr_FR → EUR</li>
     *   <li>es_ES → EUR</li>
     *   <li>it_IT → EUR</li>
     *   <li>pt_BR → BRL</li>
     *   <li>ja_JP → JPY</li>
     *   <li>zh_CN → CNY</li>
     * </ul>
     *
     * @param locale the locale to get currency for
     * @return the currency for the locale, or null if locale is null or not supported
     */
    public static Currency forLocale(Locale locale) {
        if (locale == null) {
            return null;
        }

        String country = locale.getCountry();

        return switch (country) {
            case "US" -> USD;
            case "GB" -> GBP;
            case "AU" -> AUD;
            case "DE", "FR", "ES", "IT" -> EUR;
            case "BR" -> BRL;
            case "JP" -> JPY;
            case "CN" -> CNY;
            default -> null;
        };
    }

    /**
     * Returns the currency with the given ISO 4217 code.
     *
     * @param code the ISO 4217 code to look up
     * @return the currency with that code, or null if not found
     */
    public static Currency fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (Currency currency : values()) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        return null;
    }

    /**
     * Returns the ISO 4217 3-letter alphabetic code.
     *
     * @return the currency code (e.g., "USD", "EUR")
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns the full official currency name.
     *
     * @return the currency name (e.g., "United States Dollar")
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the currency symbol.
     *
     * @return the currency symbol (e.g., "$", "€")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the ISO 4217 3-digit numeric code.
     *
     * @return the numeric code (e.g., "840", "978")
     */
    public String getNumericCode() {
        return numericCode;
    }

    /**
     * Converts this currency to a CurrencyInfo record.
     *
     * @return a CurrencyInfo containing all currency data
     */
    public CurrencyInfo toInfo() {
        return new CurrencyInfo(code, name, symbol, numericCode);
    }
}
