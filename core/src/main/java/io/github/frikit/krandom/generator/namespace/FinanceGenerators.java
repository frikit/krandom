/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.finance.*;

import java.util.Locale;

/**
 * Fluent namespace for finance-related generators.
 *
 * <p>Usage: {@code Generators.finance().creditCard().generate()}
 */
public final class FinanceGenerators {

    private final GeneratorConfig config;

    public FinanceGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public FinanceGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public CreditCardGenerator creditCard() { return new CreditCardGenerator(config); }

    public CreditCardInfoGenerator creditCardInfo() { return new CreditCardInfoGenerator(config); }

    public CardExpirationGenerator cardExpiration() { return new CardExpirationGenerator(config); }

    public CurrencyGenerator currency() { return new CurrencyGenerator(config); }

    public CurrencyPairGenerator currencyPair() { return new CurrencyPairGenerator(config); }

    public MoneyGenerator money() { return new MoneyGenerator(config); }
    public MoneyGenerator money(Locale locale) { return new MoneyGenerator(withLocale(locale)); }

    public BankAccountGenerator bankAccount() { return new BankAccountGenerator(config); }

    public BankNameGenerator bankName() { return new BankNameGenerator(config); }

    public BankTypeGenerator bankType() { return new BankTypeGenerator(config); }

    public BankInfoGenerator bankInfo() { return new BankInfoGenerator(config); }

    public BankCountryGenerator bankCountry() { return new BankCountryGenerator(config); }

    public BicGenerator bic() { return new BicGenerator(config); }

    public BbanGenerator bban() { return new BbanGenerator(config); }

    public IbanGenerator iban() { return new IbanGenerator(config); }

    public AbaRoutingGenerator abaRouting() { return new AbaRoutingGenerator(config); }

    public IsinGenerator isin() { return new IsinGenerator(config); }

    public CusipGenerator cusip() { return new CusipGenerator(config); }

    public EinGenerator ein() { return new EinGenerator(config); }

    public CryptoAddressGenerator cryptoAddress() { return new CryptoAddressGenerator(config); }

    public InvoiceInfoGenerator invoiceInfo() { return new InvoiceInfoGenerator(config); }

    public PaymentInfoGenerator paymentInfo() { return new PaymentInfoGenerator(config); }

    private GeneratorConfig withLocale(Locale locale) {
        return config.toBuilder().locale(locale).build();
    }
}
