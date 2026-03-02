/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.commerce;

/**
 * GoFakeit-style product payload.
 *
 * @param name product name
 * @param description product description
 * @param category product category/department
 * @param material product material
 * @param upc UPC-A value
 * @param isbn ISBN-13 value
 */
public record ProductInfo(
        String name,
        String description,
        String category,
        String material,
        String upc,
        String isbn
) {
}
