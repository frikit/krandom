/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware commerce-style product and price data.
 */
public final class CommerceGenerator implements Generator<String> {

    private static final String[] EN_ADJECTIVES = {"Small", "Sleek", "Rustic", "Practical", "Premium", "Ergonomic"};
    private static final String[] DE_ADJECTIVES = {"Klein", "Elegant", "Robust", "Praktisch", "Premium", "Ergonomisch"};
    private static final String[] FR_ADJECTIVES = {"Petit", "Elegant", "Rustique", "Pratique", "Premium", "Ergonomique"};
    private static final String[] ES_ADJECTIVES = {"Pequeno", "Elegante", "Rustico", "Practico", "Premium", "Ergonomico"};
    private static final String[] IT_ADJECTIVES = {"Piccolo", "Elegante", "Rustico", "Pratico", "Premium", "Ergonomico"};

    private static final String[] EN_MATERIALS = {"Steel", "Wooden", "Concrete", "Plastic", "Granite", "Cotton"};
    private static final String[] DE_MATERIALS = {"Stahl", "Holz", "Beton", "Kunststoff", "Granit", "Baumwolle"};
    private static final String[] FR_MATERIALS = {"Acier", "Bois", "Beton", "Plastique", "Granit", "Coton"};
    private static final String[] ES_MATERIALS = {"Acero", "Madera", "Hormigon", "Plastico", "Granito", "Algodon"};
    private static final String[] IT_MATERIALS = {"Acciaio", "Legno", "Cemento", "Plastica", "Granito", "Cotone"};

    private static final String[] EN_PRODUCTS = {"Chair", "Table", "Computer", "Keyboard", "Shoes", "Watch"};
    private static final String[] DE_PRODUCTS = {"Stuhl", "Tisch", "Computer", "Tastatur", "Schuhe", "Uhr"};
    private static final String[] FR_PRODUCTS = {"Chaise", "Table", "Ordinateur", "Clavier", "Chaussures", "Montre"};
    private static final String[] ES_PRODUCTS = {"Silla", "Mesa", "Computadora", "Teclado", "Zapatos", "Reloj"};
    private static final String[] IT_PRODUCTS = {"Sedia", "Tavolo", "Computer", "Tastiera", "Scarpe", "Orologio"};

    private static final String[] EN_DEPARTMENTS = {"Books", "Electronics", "Outdoors", "Home", "Toys", "Garden"};
    private static final String[] DE_DEPARTMENTS = {"Buecher", "Elektronik", "Outdoor", "Haushalt", "Spielzeug", "Garten"};
    private static final String[] FR_DEPARTMENTS = {"Livres", "Electronique", "Plein air", "Maison", "Jouets", "Jardin"};
    private static final String[] ES_DEPARTMENTS = {"Libros", "Electronica", "Exterior", "Hogar", "Juguetes", "Jardin"};
    private static final String[] IT_DEPARTMENTS = {"Libri", "Elettronica", "Esterno", "Casa", "Giocattoli", "Giardino"};

    private static final String[] EN_COLORS = {"red", "blue", "green", "black", "white", "silver"};
    private static final String[] DE_COLORS = {"rot", "blau", "gruen", "schwarz", "weiss", "silber"};
    private static final String[] FR_COLORS = {"rouge", "bleu", "vert", "noir", "blanc", "argent"};
    private static final String[] ES_COLORS = {"rojo", "azul", "verde", "negro", "blanco", "plata"};
    private static final String[] IT_COLORS = {"rosso", "blu", "verde", "nero", "bianco", "argento"};

    private final Locale locale;
    private final Random random;

    public CommerceGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CommerceGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public CommerceGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        return generateProductName();
    }

    public String generateProductName() {
        return generateAdjective() + " " + generateMaterial() + " " + generateProduct();
    }

    public String generateProductDescription() {
        return "A " + generateAdjective() + " " + generateProduct() + " in " + generateColor() + ".";
    }

    public String generateDepartment() {
        return pick(departments());
    }

    public String generateMaterial() {
        return pick(materials());
    }

    public String generateAdjective() {
        return pick(adjectives());
    }

    public String generateColor() {
        return pick(colors());
    }

    public String generateProduct() {
        return pick(products());
    }

    public BigDecimal generatePrice() {
        return generatePrice(BigDecimal.valueOf(1), BigDecimal.valueOf(999));
    }

    public BigDecimal generatePrice(BigDecimal min, BigDecimal max) {
        Objects.requireNonNull(min, "min must not be null");
        Objects.requireNonNull(max, "max must not be null");
        if (min.signum() < 0) {
            throw new IllegalArgumentException("min must be >= 0");
        }
        if (max.compareTo(min) < 0) {
            throw new IllegalArgumentException("max must be >= min");
        }
        BigDecimal span = max.subtract(min);
        BigDecimal ratio = BigDecimal.valueOf(random.nextDouble());
        return min.add(span.multiply(ratio)).setScale(2, RoundingMode.HALF_UP);
    }

    public Locale getLocale() {
        return locale;
    }

    private String[] adjectives() {
        return switch (locale.getLanguage()) {
            case "de" -> DE_ADJECTIVES;
            case "fr" -> FR_ADJECTIVES;
            case "es" -> ES_ADJECTIVES;
            case "it" -> IT_ADJECTIVES;
            default -> EN_ADJECTIVES;
        };
    }

    private String[] materials() {
        return switch (locale.getLanguage()) {
            case "de" -> DE_MATERIALS;
            case "fr" -> FR_MATERIALS;
            case "es" -> ES_MATERIALS;
            case "it" -> IT_MATERIALS;
            default -> EN_MATERIALS;
        };
    }

    private String[] products() {
        return switch (locale.getLanguage()) {
            case "de" -> DE_PRODUCTS;
            case "fr" -> FR_PRODUCTS;
            case "es" -> ES_PRODUCTS;
            case "it" -> IT_PRODUCTS;
            default -> EN_PRODUCTS;
        };
    }

    private String[] departments() {
        return switch (locale.getLanguage()) {
            case "de" -> DE_DEPARTMENTS;
            case "fr" -> FR_DEPARTMENTS;
            case "es" -> ES_DEPARTMENTS;
            case "it" -> IT_DEPARTMENTS;
            default -> EN_DEPARTMENTS;
        };
    }

    private String[] colors() {
        return switch (locale.getLanguage()) {
            case "de" -> DE_COLORS;
            case "fr" -> FR_COLORS;
            case "es" -> ES_COLORS;
            case "it" -> IT_COLORS;
            default -> EN_COLORS;
        };
    }

    private String pick(String[] values) {
        return values[random.nextInt(values.length)];
    }
}
