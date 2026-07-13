/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.identifier.UUIDGenerator;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates structured payment payloads derived from coherent invoice data.
 *
 * <p>When banking-identifier generation is disabled, bank-method payloads retain only an opaque
 * test reference and do not construct an account or routing number.
 */
public final class PaymentInfoGenerator implements Generator<PaymentInfo> {

    private static final String[] PAYMENT_METHODS = {
        "CARD", "ACH", "WIRE"
    };
    private static final String[] PAYMENT_STATUSES = {
        "AUTHORIZED", "CAPTURED", "SETTLED", "FAILED", "REFUNDED"
    };
    private static final String[] CARD_PROCESSORS = {
        "Stripe", "Adyen", "Braintree", "Checkout.com", "Square"
    };
    private static final String[] BANK_PROCESSORS = {
        "Plaid", "Dwolla", "GoCardless", "Modern Treasury"
    };

    private final GeneratorConfig          config;
    private final Random                   random;
    private final UUIDGenerator            uuidGenerator;
    private final InvoiceInfoGenerator     invoiceInfoGenerator;
    private final CreditCardInfoGenerator  creditCardInfoGenerator;
    private final BankInfoGenerator        bankInfoGenerator;

    /**
     * Creates a payment-info generator using default configuration ({@link Locale#US}).
     */
    public PaymentInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a payment-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public PaymentInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a payment-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public PaymentInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.uuidGenerator = new UUIDGenerator(config);
        this.invoiceInfoGenerator = new InvoiceInfoGenerator(config);
        this.creditCardInfoGenerator = new CreditCardInfoGenerator(config);
        this.bankInfoGenerator = new BankInfoGenerator(config);
    }

    @Override
    public PaymentInfo generate() {
        InvoiceInfo invoice = invoiceInfoGenerator.generate();
        String method = PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)];
        String status = PAYMENT_STATUSES[random.nextInt(PAYMENT_STATUSES.length)];
        LocalDate authorizedOn = invoice.issuedOn().plusDays(random.nextInt(0, 8));
        LocalDate settledOn = requiresSettlement(status) ? authorizedOn.plusDays(random.nextInt(0, 6)) : null;

        return new PaymentInfo(
            prefixedId("PAY"),
            invoice.orderNumber(),
            invoice.invoiceNumber(),
            status,
            method,
            selectProcessor(method),
            authorizedOn,
            settledOn,
            invoice.currencyCode(),
            invoice.total(),
            invoice.customer(),
            invoice.billingAddress(),
            instrumentReference(method)
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private String prefixedId(String prefix) {
        String compact = uuidGenerator.generateV4String().replace("-", "").toUpperCase(Locale.ROOT);
        return prefix + "-" + compact.substring(0, 12);
    }

    private String selectProcessor(String method) {
        String[] processors = "CARD".equals(method) ? CARD_PROCESSORS : BANK_PROCESSORS;
        return processors[random.nextInt(processors.length)];
    }

    private String instrumentReference(String method) {
        if ("CARD".equals(method)) {
            return "**** **** **** " + tailDigits(creditCardInfoGenerator.generate().number(), 4);
        }
        if (config.getBankingSafetyPolicy() == BankingSafetyPolicy.DISABLED) {
            return "ACCT-TEST-" + String.format(Locale.ROOT, "%04d", random.nextInt(10_000));
        }
        return "ACCT-" + tailDigits(bankInfoGenerator.generate().accountNumber(), 4);
    }

    private boolean requiresSettlement(String status) {
        return !"AUTHORIZED".equals(status) && !"FAILED".equals(status);
    }

    private String tailDigits(String value, int count) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() <= count) {
            return digits;
        }
        return digits.substring(digits.length() - count);
    }
}
