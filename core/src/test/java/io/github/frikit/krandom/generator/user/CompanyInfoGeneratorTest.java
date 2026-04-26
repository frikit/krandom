/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("CompanyInfoGenerator")
class CompanyInfoGeneratorTest {

    @Test
    @DisplayName("generates a coherent structured company payload")
    void generateCompanyInfo() {
        CompanyInfo info = new CompanyInfoGenerator(Locale.US).generate();

        assertNotNull(info);
        assertFalse(info.name().isBlank());
        assertFalse(info.industry().isBlank());
        assertFalse(info.catchPhrase().isBlank());
        assertFalse(info.buzzword().isBlank());
        assertFalse(info.email().isBlank());
        assertFalse(info.website().isBlank());
        assertFalse(info.phone().isBlank());
        assertNotNull(info.address());

        String host = URI.create(info.website()).getHost();
        assertNotNull(host);
        String domain = host.startsWith("www.") ? host.substring(4) : host;
        assertEquals(domain, info.email().substring(info.email().indexOf('@') + 1));
        assertEquals("US", info.address().countryAbbr());
    }

    @Test
    @DisplayName("seeded generation is reproducible")
    void seededGeneration() {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .locale(Locale.GERMANY)
                                                .seed(42L)
                                                .build();

        CompanyInfoGenerator one = new CompanyInfoGenerator(config);
        CompanyInfoGenerator two = new CompanyInfoGenerator(config);

        assertEquals(one.generate(), two.generate());
    }

    @Test
    @DisplayName("constructors and factories reject nulls and expose locale")
    void constructorValidation() {
        assertThrows(NullPointerException.class, () -> new CompanyInfoGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new CompanyInfoGenerator((GeneratorConfig) null));
        assertEquals(Locale.US, new CompanyInfoGenerator(Locale.US).getLocale());
        assertNotNull(Generators.ofCompanyInfo().generate());
        assertNotNull(Generators.ofCompanyInfo(Locale.US).generate());
        assertNotNull(Generators.ofCompanyInfo(GeneratorConfig.defaults()).generate());
    }
}
