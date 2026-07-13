/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.provider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Provider catalog documentation")
class ProviderCatalogDocumentationTest {

    @Test
    @DisplayName("checked-in provider reference is rendered from the catalog")
    void checkedInProviderReferenceIsRenderedFromCatalog() throws IOException {
        Path reference = Path.of(System.getProperty("krandom.rootDir"), "docs", "reference", "provider-catalog.md");

        assertEquals(ProviderCatalogDocumentation.render(), Files.readString(reference));
    }
}
