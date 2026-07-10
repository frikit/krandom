/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.finance;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware bank account artifacts (number, name, transaction type).
 *
 * <p>Use an explicit {@link GeneratorConfig} and select
 * {@link BankingSafetyPolicy#REALISTIC_UNCLASSIFIED} only for isolated compatibility fixtures.
 * The default configured policy is {@link BankingSafetyPolicy#DISABLED}.
 */
public final class BankAccountGenerator implements Generator<String> {

    private static final String[] EN_ACCOUNT_NAMES = {
        "Checking Account", "Savings Account", "Business Account", "Joint Account", "Payroll Account"
    };
    private static final String[] DE_ACCOUNT_NAMES = {
        "Girokonto", "Sparkonto", "Geschaeftskonto", "Gemeinschaftskonto", "Gehaltskonto"
    };
    private static final String[] FR_ACCOUNT_NAMES = {
        "Compte courant", "Compte epargne", "Compte entreprise", "Compte joint", "Compte salaire"
    };
    private static final String[] ES_ACCOUNT_NAMES = {
        "Cuenta corriente", "Cuenta de ahorro", "Cuenta empresarial", "Cuenta conjunta", "Cuenta nomina"
    };
    private static final String[] IT_ACCOUNT_NAMES = {
        "Conto corrente", "Conto risparmio", "Conto aziendale", "Conto cointestato", "Conto stipendio"
    };

    private static final String[] EN_TRANSACTION_TYPES = {
        "deposit", "withdrawal", "payment", "transfer", "refund", "fee", "interest", "chargeback"
    };
    private static final String[] DE_TRANSACTION_TYPES = {
        "einzahlung", "abhebung", "zahlung", "ueberweisung", "rueckerstattung", "gebuehr", "zins", "rueckbuchung"
    };
    private static final String[] FR_TRANSACTION_TYPES = {
        "depot", "retrait", "paiement", "virement", "remboursement", "frais", "interet", "retrofacturation"
    };
    private static final String[] ES_TRANSACTION_TYPES = {
        "deposito", "retiro", "pago", "transferencia", "reembolso", "comision", "interes", "contracargo"
    };
    private static final String[] IT_TRANSACTION_TYPES = {
        "deposito", "prelievo", "pagamento", "bonifico", "rimborso", "commissione", "interesse", "storno"
    };

    private final Locale                locale;
    private final Random                random;
    private final BankingSafetyPolicy bankingSafetyPolicy;

    /**
     * @deprecated Use {@link #BankAccountGenerator(GeneratorConfig)}. This 1.6 bridge retains
     *             realistic but unclassified output; v2 configuration fails closed by default.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public BankAccountGenerator() {
        this(GeneratorConfig.builder().bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED).build());
    }

    /**
     * @deprecated Use {@link #BankAccountGenerator(GeneratorConfig)}. This 1.6 bridge retains
     *             realistic but unclassified output; v2 configuration fails closed by default.
     */
    @Deprecated(since = "1.6", forRemoval = true)
    public BankAccountGenerator(Locale locale) {
        this(GeneratorConfig.builder()
                            .locale(Objects.requireNonNull(locale, "locale must not be null"))
                            .bankingSafetyPolicy(BankingSafetyPolicy.REALISTIC_UNCLASSIFIED)
                            .build());
    }

    public BankAccountGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        this.bankingSafetyPolicy = config.getBankingSafetyPolicy();
    }

    private static int lengthByCountry(String country) {
        return switch (country) {
            case "GB" -> 8;
            case "DE", "US", "ES" -> 10;
            case "FR" -> 11;
            case "IT", "CN" -> 12;
            case "BR" -> 9;
            case "JP" -> 7;
            case "AU" -> 9;
            default -> 10;
        };
    }

    /**
     * Generates a locale-shaped account number.
     */
    @Override
    public String generate() {
        return generateAccountNumber();
    }

    public String generateAccountNumber() {
        bankingSafetyPolicy.requireRealisticOutput();
        return randomDigits(lengthByCountry(locale.getCountry()));
    }

    public String generateAccountName() {
        String[] values = switch (locale.getLanguage()) {
            case "de" -> DE_ACCOUNT_NAMES;
            case "fr" -> FR_ACCOUNT_NAMES;
            case "es" -> ES_ACCOUNT_NAMES;
            case "it" -> IT_ACCOUNT_NAMES;
            default -> EN_ACCOUNT_NAMES;
        };
        return values[random.nextInt(values.length)];
    }

    public String generateTransactionType() {
        String[] values = switch (locale.getLanguage()) {
            case "de" -> DE_TRANSACTION_TYPES;
            case "fr" -> FR_TRANSACTION_TYPES;
            case "es" -> ES_TRANSACTION_TYPES;
            case "it" -> IT_TRANSACTION_TYPES;
            default -> EN_TRANSACTION_TYPES;
        };
        return values[random.nextInt(values.length)];
    }

    public Locale getLocale() {
        return locale;
    }

    private String randomDigits(int length) {
        StringBuilder out = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            out.append((char) ('0' + random.nextInt(10)));
        }
        return out.toString();
    }
}
