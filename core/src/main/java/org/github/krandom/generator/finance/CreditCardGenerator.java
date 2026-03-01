/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.finance;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Random;

/**
 * Generates valid credit card numbers conforming to the Luhn algorithm and industry standards.
 *
 * <p>This generator produces realistic credit card numbers for testing purposes, supporting major
 * card types including Visa, Mastercard, American Express, Discover, JCB, and Diners Club.
 * All generated card numbers pass Luhn checksum validation and follow proper IIN (Issuer
 * Identification Number) and length rules.
 *
 * <p><strong>Supported Card Types:</strong>
 * <ul>
 *   <li><strong>Visa</strong>: Prefix 4, length 16 digits (also supports 13-digit legacy format)
 *       <ul><li>Example: "4532 1488 0343 6467"</li></ul>
 *   </li>
 *   <li><strong>Mastercard</strong>: Prefixes 51-55 or 2221-2720, length 16 digits
 *       <ul><li>Example: "5425 2334 3010 9903"</li></ul>
 *   </li>
 *   <li><strong>American Express</strong>: Prefixes 34 or 37, length 15 digits, 4-digit CVV
 *       <ul><li>Example: "3782 822463 10005"</li></ul>
 *   </li>
 *   <li><strong>Discover</strong>: Prefixes 6011, 644-649, or 65, length 16 digits
 *       <ul><li>Example: "6011 1111 1111 1117"</li></ul>
 *   </li>
 *   <li><strong>JCB</strong>: Prefixes 3528-3589, length 16 digits
 *       <ul><li>Example: "3530 1113 3330 0000"</li></ul>
 *   </li>
 *   <li><strong>Diners Club</strong>: Prefixes 300-305, 36, or 38, length 14 digits
 *       <ul><li>Example: "3056 930902 5904"</li></ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 *   // Random card type
 *   CreditCardGenerator gen = new CreditCardGenerator();
 *   String cardNumber = gen.generate();  // "4532 1488 0343 6467" (formatted)
 *   
 *   // Specific card type
 *   CreditCardGenerator visaGen = new CreditCardGenerator(CardType.VISA);
 *   String visaCard = visaGen.generate();  // Always generates Visa cards
 *   
 *   // Unformatted number
 *   String unformatted = visaGen.generate(false);  // "4532148803436467"
 *   
 *   // Full card information
 *   CardInfo info = visaGen.generateWithType();
 *   System.out.println(info.cardNumber());      // "4532 1488 0343 6467"
 *   System.out.println(info.cardType());        // VISA
 *   System.out.println(info.cvv());             // "123"
 *   System.out.println(info.expirationDate());  // "12/28"
 * }</pre>
 *
 * <p><strong>Seeded Generation:</strong>
 * <pre>{@code
 *   GeneratorConfig config = GeneratorConfig.builder()
 *       .seed(42L)
 *       .build();
 *   CreditCardGenerator gen = new CreditCardGenerator(config);
 *   
 *   // Reproducible output
 *   String card1 = gen.generate();
 *   String card2 = gen.generate();
 * }</pre>
 *
 * <p><strong>Batch Generation:</strong>
 * <pre>{@code
 *   CreditCardGenerator gen = new CreditCardGenerator(CardType.MASTERCARD);
 *   
 *   // Generate list
 *   List<String> cards = gen.generateList(10);
 *   
 *   // Generate stream
 *   gen.stream()
 *      .limit(100)
 *      .forEach(System.out::println);
 * }</pre>
 *
 * <p><strong>Note:</strong> All generated card numbers are for testing purposes only and should
 * not be used for real financial transactions. They pass Luhn validation but are not associated
 * with any real accounts.
 */
public final class CreditCardGenerator implements Generator<String> {
    
    private static final DateTimeFormatter EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");
    
    private final GeneratorConfig config;
    private final Random random;
    private final CardType cardType;
    
    /**
     * Creates a generator that produces random card types using default configuration.
     */
    public CreditCardGenerator() {
        this(GeneratorConfig.defaults(), CardType.RANDOM);
    }
    
    /**
     * Creates a generator for the specified card type using default configuration.
     *
     * @param cardType the card type to generate; must not be {@code null}
     * @throws NullPointerException if {@code cardType} is {@code null}
     */
    public CreditCardGenerator(CardType cardType) {
        this(GeneratorConfig.defaults(), cardType);
    }
    
    /**
     * Creates a generator using the given configuration and random card types.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public CreditCardGenerator(GeneratorConfig config) {
        this(config, CardType.RANDOM);
    }
    
    /**
     * Creates a generator using the given configuration and card type.
     *
     * @param config the generator configuration; must not be {@code null}
     * @param cardType the card type to generate; must not be {@code null}
     * @throws NullPointerException if {@code config} or {@code cardType} is {@code null}
     */
    public CreditCardGenerator(GeneratorConfig config, CardType cardType) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.cardType = Objects.requireNonNull(cardType, "cardType must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>Generates a formatted credit card number (e.g., "4532 1488 0343 6467").
     *
     * @return a formatted card number; never {@code null}
     */
    @Override
    public String generate() {
        return generate(true);
    }
    
    /**
     * Generates a credit card number with optional formatting.
     *
     * @param formatted {@code true} for formatted output with spaces (e.g., "4532 1488 0343 6467"),
     *                  {@code false} for unformatted digits only (e.g., "4532148803436467")
     * @return a card number string; never {@code null}
     */
    public String generate(boolean formatted) {
        CardType type = selectCardType();
        String cardNumber = generateCardNumber(type);
        return formatted ? formatCardNumber(cardNumber, type) : cardNumber;
    }
    
    /**
     * Generates complete card information including card number, type, CVV, and expiration date.
     *
     * @return a {@link CardInfo} object with all card details; never {@code null}
     */
    public CardInfo generateWithType() {
        CardType type = selectCardType();
        String cardNumber = generateCardNumber(type);
        String formattedNumber = formatCardNumber(cardNumber, type);
        String cvv = getCvv(type);
        String expirationDate = getExpirationDate();
        
        return new CardInfo(formattedNumber, type, cvv, expirationDate);
    }

    /**
     * Generates a credit card number without formatting separators.
     * Faker-style alias: {@code credit_card_number()}.
     *
     * @return unformatted card number digits
     */
    public String generateNumber() {
        return generate(false);
    }

    /**
     * Generates an expiration date in MM/YY format.
     * Faker-style alias: {@code credit_card_expire()}.
     *
     * @return expiration date in MM/YY format
     */
    public String generateExpiry() {
        return getExpirationDate();
    }

    /**
     * Generates a card security code (CVV/CVC).
     * Faker-style alias: {@code credit_card_security_code()}.
     *
     * @return security code digits
     */
    public String generateSecurityCode() {
        return getCvv();
    }

    /**
     * Generates a card provider display name.
     * Faker-style alias: {@code credit_card_provider()}.
     *
     * @return card provider name
     */
    public String generateProvider() {
        return selectCardType().getDisplayName();
    }

    /**
     * Generates a multi-line card payload similar to Faker's {@code credit_card_full()}.
     *
     * @return multi-line full card payload
     */
    public String generateFull() {
        CardInfo info = generateWithType();
        return info.cardType().getDisplayName()
                + "\n"
                + info.cardNumber()
                + "\n"
                + info.expirationDate()
                + " "
                + info.cvv();
    }
    
    /**
     * Generates a CVV (Card Verification Value) code.
     *
     * <p>Returns a 3-digit CVV for most card types, or a 4-digit CVV for American Express.
     *
     * @return a CVV string (3 or 4 digits); never {@code null}
     */
    public String getCvv() {
        return getCvv(selectCardType());
    }
    
    /**
     * Generates an expiration date in MM/YY format.
     *
     * <p>The generated date is always in the future, between 1 month and 5 years from now.
     *
     * @return an expiration date string in MM/YY format; never {@code null}
     */
    public String getExpirationDate() {
        LocalDate now = LocalDate.now();
        int monthsToAdd = 1 + random.nextInt(60); // 1-60 months (up to 5 years)
        LocalDate expiryDate = now.plusMonths(monthsToAdd);
        return expiryDate.format(EXPIRY_FORMATTER);
    }
    
    /**
     * Returns the card type this generator is configured to produce.
     *
     * @return the card type; never {@code null}
     */
    public CardType getCardType() {
        return cardType;
    }
    
    // ── Private helper methods ────────────────────────────────────────────────
    
    private CardType selectCardType() {
        if (cardType != CardType.RANDOM) {
            return cardType;
        }
        
        // Randomly select a card type (excluding RANDOM itself)
        CardType[] types = {
            CardType.VISA,
            CardType.MASTERCARD,
            CardType.AMEX,
            CardType.DISCOVER,
            CardType.JCB,
            CardType.DINERS_CLUB
        };
        return types[random.nextInt(types.length)];
    }
    
    private String generateCardNumber(CardType type) {
        // Select a prefix pattern
        String prefix = selectPrefix(type);
        
        // Select card length
        int cardLength = type.getCardLengths().get(random.nextInt(type.getCardLengths().size()));
        
        // Generate the rest of the digits (except the last check digit)
        StringBuilder builder = new StringBuilder(prefix);
        int remainingDigits = cardLength - prefix.length() - 1; // -1 for check digit
        
        for (int i = 0; i < remainingDigits; i++) {
            builder.append(random.nextInt(10));
        }
        
        // Calculate and append Luhn check digit
        int checkDigit = calculateLuhnCheckDigit(builder.toString());
        builder.append(checkDigit);
        
        return builder.toString();
    }
    
    private String selectPrefix(CardType type) {
        String pattern = type.getPrefixPatterns().get(random.nextInt(type.getPrefixPatterns().size()));
        
        // Handle range patterns (e.g., "51-55", "2221-2720")
        if (pattern.contains("-")) {
            String[] parts = pattern.split("-");
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            int value = start + random.nextInt(end - start + 1);
            return String.valueOf(value);
        }
        
        return pattern;
    }
    
    /**
     * Calculates the Luhn check digit for a given card number prefix.
     *
     * @param number the card number without check digit
     * @return the check digit (0-9)
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true; // Start from the rightmost digit (which will be doubled)
        
        // Process from right to left
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9; // Same as (digit / 10) + (digit % 10)
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        // The check digit is the amount needed to make the sum a multiple of 10
        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;
    }
    
    /**
     * Validates a card number using the Luhn algorithm.
     *
     * @param cardNumber the card number to validate (digits only)
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        
        // Remove any non-digit characters
        cardNumber = cardNumber.replaceAll("\\D", "");
        
        int sum = 0;
        boolean alternate = false;
        
        // Process from right to left
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit - 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10) == 0;
    }
    
    private String formatCardNumber(String cardNumber, CardType type) {
        // Different formatting for different card types
        return switch (type) {
            case AMEX -> formatAmex(cardNumber);              // 4-6-5
            case DINERS_CLUB -> formatDiners(cardNumber);     // 4-6-4
            default -> formatStandard(cardNumber);            // 4-4-4-4 (or 4-4-4-1 for 13-digit)
        };
    }
    
    private String formatStandard(String cardNumber) {
        // Format as 4-4-4-4 for 16-digit or 4-4-4-1 for 13-digit
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < cardNumber.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                formatted.append(' ');
            }
            formatted.append(cardNumber.charAt(i));
        }
        return formatted.toString();
    }
    
    private String formatAmex(String cardNumber) {
        // Format as 4-6-5
        return cardNumber.substring(0, 4) + " " +
               cardNumber.substring(4, 10) + " " +
               cardNumber.substring(10);
    }
    
    private String formatDiners(String cardNumber) {
        // Format as 4-6-4
        return cardNumber.substring(0, 4) + " " +
               cardNumber.substring(4, 10) + " " +
               cardNumber.substring(10);
    }
    
    private String getCvv(CardType type) {
        int cvvLength = type.getCvvLength();
        int maxValue = (int) Math.pow(10, cvvLength);
        int cvvValue = random.nextInt(maxValue);
        return String.format("%0" + cvvLength + "d", cvvValue);
    }
}
