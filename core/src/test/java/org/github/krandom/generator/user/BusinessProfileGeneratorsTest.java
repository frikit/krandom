/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.Generators;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Business profile generators")
class BusinessProfileGeneratorsTest {

    @Test
    @DisplayName("Industry generator returns non-empty values and supports seeding")
    void industryGenerator() {
        IndustryGenerator any = new IndustryGenerator(GeneratorConfig.builder().seed(1L).build());
        IndustryGenerator a = new IndustryGenerator(GeneratorConfig.builder().seed(1L).build());
        IndustryGenerator b = new IndustryGenerator(GeneratorConfig.builder().seed(1L).build());
        assertFalse(any.generate().isBlank());
        assertEquals(a.generate(), b.generate());
        assertThrows(NullPointerException.class, () -> new IndustryGenerator(null));
    }

    @Test
    @DisplayName("Company URL generator returns valid-looking URLs")
    void companyUrlGenerator() {
        assertNotNull(new CompanyUrlGenerator().generate());
        CompanyUrlGenerator gen = new CompanyUrlGenerator(Locale.US);
        CompanyUrlGenerator seededA = new CompanyUrlGenerator(GeneratorConfig.builder().seed(12L).build());
        CompanyUrlGenerator seededB = new CompanyUrlGenerator(GeneratorConfig.builder().seed(12L).build());
        String url = gen.generate();
        String fromName = gen.generateFromCompanyName();
        String fromBlankName = gen.generateFromCompanyName("___");
        assertTrue(url.matches("https?://www\\.[a-z0-9]+(\\.[a-z]{2,})+"));
        assertTrue(fromName.matches("https://www\\.[a-z0-9]+\\.[a-z]{2,}"));
        assertTrue(fromBlankName.matches("https://www\\.company\\.[a-z]{2,}"));
        assertEquals(seededA.generate(), seededB.generate());
        assertThrows(NullPointerException.class, () -> new CompanyUrlGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> gen.generateFromCompanyName(null));
    }

    @Test
    @DisplayName("Job and profile generators return non-empty values")
    void jobAndProfileGenerators() {
        assertNotNull(new JobFieldGenerator().generate());
        assertNotNull(new JobTypeGenerator().generate());
        assertNotNull(new SeniorityGenerator().generate());
        assertNotNull(new PositionGenerator().generate());
        assertNotNull(new EducationalAttainmentGenerator().generate());
        assertNotNull(new MaritalStatusGenerator().generate());

        JobFieldGenerator field = new JobFieldGenerator(GeneratorConfig.builder().seed(2L).build());
        JobTypeGenerator type = new JobTypeGenerator(GeneratorConfig.builder().seed(22L).build());
        SeniorityGenerator seniority = new SeniorityGenerator(GeneratorConfig.builder().seed(3L).build());
        PositionGenerator position = new PositionGenerator(GeneratorConfig.builder().seed(4L).build());
        EducationalAttainmentGenerator education = new EducationalAttainmentGenerator(GeneratorConfig.builder().seed(5L).build());
        MaritalStatusGenerator marital = new MaritalStatusGenerator(GeneratorConfig.builder().seed(6L).build());

        assertFalse(field.generate().isBlank());
        assertFalse(type.generate().isBlank());
        assertFalse(seniority.generate().isBlank());
        assertFalse(position.generate().isBlank());
        assertFalse(education.generate().isBlank());
        assertFalse(marital.generate().isBlank());
    }

    @Test
    @DisplayName("Null config validation on profile generators")
    void nullConfigValidation() {
        assertThrows(NullPointerException.class, () -> new JobFieldGenerator(null));
        assertThrows(NullPointerException.class, () -> new JobTypeGenerator((GeneratorConfig) null));
        assertThrows(NullPointerException.class, () -> new JobTypeGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new SeniorityGenerator(null));
        assertThrows(NullPointerException.class, () -> new PositionGenerator(null));
        assertThrows(NullPointerException.class, () -> new EducationalAttainmentGenerator(null));
        assertThrows(NullPointerException.class, () -> new MaritalStatusGenerator(null));
    }

    @Test
    @DisplayName("Generators factory methods expose business/profile generators")
    void generatorFactoryCoverage() {
        assertNotNull(Generators.ofIndustry().generate());
        assertNotNull(Generators.ofCompanyUrl().generate());
        assertNotNull(Generators.ofJobField().generate());
        assertNotNull(Generators.ofJobType().generate());
        assertNotNull(Generators.ofSeniority().generate());
        assertNotNull(Generators.ofPosition().generate());
        assertNotNull(Generators.ofEducationalAttainment().generate());
        assertNotNull(Generators.ofMaritalStatus().generate());
    }

    @Test
    @DisplayName("JobType generator supports locale-specific values")
    void jobTypeLocaleSupport() {
        JobTypeGenerator de = new JobTypeGenerator(Locale.GERMANY);
        JobTypeGenerator ja = new JobTypeGenerator(Locale.JAPAN);
        assertEquals(Locale.GERMANY, de.getLocale());
        assertEquals(Locale.JAPAN, ja.getLocale());
        assertNotNull(de.generate());
        assertNotNull(ja.generate());
    }
}
