/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.GeneratorConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JobTypeGenerator")
class JobTypeGeneratorTest {

    private static void assertOneOf(JobTypeGenerator generator, List<String> allowed) {
        String value = generator.generate();
        assertTrue(allowed.contains(value), "Unexpected job type value: " + value);
    }

    @Test
    @DisplayName("constructors validate input and expose locale")
    void constructors() {
        JobTypeGenerator def = new JobTypeGenerator();
        assertEquals(Locale.US, def.getLocale());
        assertNotNull(def.generate());

        JobTypeGenerator de = new JobTypeGenerator(Locale.GERMANY);
        assertEquals(Locale.GERMANY, de.getLocale());
        assertNotNull(de.generate());

        assertThrows(NullPointerException.class, () -> new JobTypeGenerator((Locale) null));
        assertThrows(NullPointerException.class, () -> new JobTypeGenerator((GeneratorConfig) null));
    }

    @Test
    @DisplayName("seeded generators are reproducible")
    void seededReproducibility() {
        GeneratorConfig cfg = GeneratorConfig.builder().seed(77L).locale(Locale.GERMANY).build();
        JobTypeGenerator a = new JobTypeGenerator(cfg);
        JobTypeGenerator b = new JobTypeGenerator(cfg);
        for (int i = 0; i < 20; i++) {
            assertEquals(a.generate(), b.generate());
        }
    }

    @Test
    @DisplayName("locale-specific vocabularies are used for all supported language branches")
    void localeBranches() {
        assertOneOf(new JobTypeGenerator(Locale.US), List.of(
            "Full-time", "Part-time", "Contract", "Temporary", "Internship",
            "Freelance", "Apprenticeship", "Seasonal", "Volunteer", "Remote"
        ));
        assertOneOf(new JobTypeGenerator(Locale.GERMANY), List.of(
            "Vollzeit", "Teilzeit", "Vertrag", "Befristet", "Praktikum",
            "Freelance", "Ausbildung", "Saisonal", "Ehrenamt", "Remote"
        ));
        assertOneOf(new JobTypeGenerator(Locale.FRANCE), List.of(
            "Temps plein", "Temps partiel", "Contrat", "Temporaire", "Stage",
            "Freelance", "Apprentissage", "Saisonnier", "Bénévole", "À distance"
        ));
        assertOneOf(new JobTypeGenerator(Locale.of("es", "ES")), List.of(
            "Tiempo completo", "Medio tiempo", "Contrato", "Temporal", "Pasantía",
            "Freelance", "Aprendizaje", "Estacional", "Voluntario", "Remoto"
        ));
        assertOneOf(new JobTypeGenerator(Locale.ITALY), List.of(
            "Tempo pieno", "Part-time", "Contratto", "Temporaneo", "Tirocinio",
            "Freelance", "Apprendistato", "Stagionale", "Volontario", "Remoto"
        ));
        assertOneOf(new JobTypeGenerator(Locale.of("pt", "BR")), List.of(
            "Tempo integral", "Meio período", "Contrato", "Temporário", "Estágio",
            "Freelancer", "Aprendizagem", "Sazonal", "Voluntário", "Remoto"
        ));
        assertOneOf(new JobTypeGenerator(Locale.JAPAN), List.of(
            "正社員", "パートタイム", "契約", "臨時", "インターンシップ",
            "フリーランス", "見習い", "季節雇用", "ボランティア", "リモート"
        ));
        assertOneOf(new JobTypeGenerator(Locale.CHINA), List.of(
            "全职", "兼职", "合同制", "临时", "实习",
            "自由职业", "学徒制", "季节性", "志愿者", "远程"
        ));
        assertOneOf(new JobTypeGenerator(Locale.of("xx", "YY")), List.of(
            "Full-time", "Part-time", "Contract", "Temporary", "Internship",
            "Freelance", "Apprenticeship", "Seasonal", "Volunteer", "Remote"
        ));
    }
}
