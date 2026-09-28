/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.identifier.EanGenerator;
import io.github.frikit.krandom.generator.identifier.IsbnGenerator;
import io.github.frikit.krandom.generator.identifier.UpcGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware commerce-style product and price data.
 *
 * <p>Product vocabulary and the product-name/description formats are resolved through the
 * configuration's {@code DataRegistryContext}, which defaults to {@link CommerceDataRegistry};
 * locales without built-in data fall back to the bundled English vocabulary.
 */
public final class CommerceGenerator implements Generator<String> {

    private static final CommerceDataProvider DEFAULT_PROVIDER =
        new BuiltInCommerceDataProvider(Locale.ROOT, "default");

    private final Locale        locale;
    private final Random        random;
    private final CommerceDataProvider vocabulary;
    private final UpcGenerator  upcGenerator;
    private final IsbnGenerator isbn10Generator;
    private final IsbnGenerator isbn13Generator;
    private final EanGenerator  eanGenerator;

    public CommerceGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CommerceGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public CommerceGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
        CommerceDataProvider provider = config.getRegistryContext().commerceProvider(locale);
        this.vocabulary = provider != null ? provider : DEFAULT_PROVIDER;
        this.upcGenerator = new UpcGenerator(config.forChildStream("upc"));
        this.isbn10Generator = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_10, config.forChildStream("isbn10"));
        this.isbn13Generator = new IsbnGenerator(IsbnGenerator.IsbnType.ISBN_13, config.forChildStream("isbn13"));
        this.eanGenerator = new EanGenerator(config.forChildStream("ean"));
    }

    @Override
    public String generate() {
        return generateProductName();
    }

    public String generateProductName() {
        String adjective = generateAdjective();
        String material = generateMaterial();
        String product = generateProduct();
        return vocabulary.getProductNameFormat()
                         .replace("{adjective}", adjective)
                         .replace("{material}", material)
                         .replace("{product}", product);
    }

    public String generateProductDescription() {
        String adjective = generateAdjective();
        String product = generateProduct();
        String color = generateColor();
        return vocabulary.getProductDescriptionFormat()
                         .replace("{adjective}", adjective)
                         .replace("{product}", product)
                         .replace("{color}", color);
    }

    public String generateDepartment() {
        return pick(departments());
    }

    /**
     * Generates a product category aliasing the department provider.
     *
     * @return category/department label
     */
    public String generateCategory() {
        return generateDepartment();
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

    /**
     * Generates a UPC-A product code.
     *
     * @return 12-digit UPC-A code
     */
    public String generateUpc() {
        return upcGenerator.generate();
    }

    /**
     * Generates an EAN-8 product code.
     *
     * @return 8-digit EAN code
     */
    public String generateEan8() {
        return eanGenerator.generateEan8();
    }

    /**
     * Generates an EAN-13 product code.
     *
     * @return 13-digit EAN code
     */
    public String generateEan13() {
        return eanGenerator.generateEan13();
    }

    /**
     * Generates an ISBN-10 product code.
     *
     * @return ISBN-10 code
     */
    public String generateIsbn10() {
        return isbn10Generator.generate();
    }

    /**
     * Generates an ISBN-13 product code.
     *
     * @return ISBN-13 code
     */
    public String generateIsbn13() {
        return isbn13Generator.generate();
    }

    /**
     * Generates a product code, randomly choosing between UPC, EAN-13, and ISBN-13.
     *
     * @return product code
     */
    public String generateProductCode() {
        return switch (random.nextInt(3)) {
            case 0 -> generateUpc();
            case 1 -> generateEan13();
            default -> generateIsbn13();
        };
    }

    /**
     * Generates a structured product payload.
     *
     * @return product payload
     */
    public ProductInfo generateProductInfo() {
        return new ProductInfo(
            generateProductName(),
            generateProductDescription(),
            generateCategory(),
            generateMaterial(),
            generateUpc(),
            generateIsbn13()
        );
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

    private List<String> adjectives() {
        return vocabulary.getAdjectives();
    }

    private List<String> materials() {
        return vocabulary.getMaterials();
    }

    private List<String> products() {
        return vocabulary.getProducts();
    }

    private List<String> departments() {
        return vocabulary.getDepartments();
    }

    private List<String> colors() {
        return vocabulary.getColors();
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
