/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.commerce;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProductInfoGenerator")
class ProductInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured product payload")
    void generateProductInfo() {
        ProductInfo info = new ProductInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertTrue(!info.name().isBlank());
        assertTrue(!info.description().isBlank());
        assertTrue(!info.category().isBlank());
        assertTrue(!info.material().isBlank());
        assertTrue(info.upc().matches("\\d{12}"));
        assertTrue(info.isbn().matches("\\d{13}"));
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        ProductInfoGenerator one = new ProductInfoGenerator(config);
        ProductInfoGenerator two = new ProductInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new ProductInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new ProductInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new ProductInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofProductInfo().generate());
        assertNotNull(Generators.ofProductInfo(Locale.US).generate());
        assertNotNull(Generators.ofProductInfo(GeneratorConfig.defaults()).generate());
    }
}
