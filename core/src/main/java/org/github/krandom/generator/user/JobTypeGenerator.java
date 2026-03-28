/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates employment type values.
 */
public final class JobTypeGenerator implements Generator<String> {

    private static final String[] JOB_TYPES_EN = {
        "Full-time",
        "Part-time",
        "Contract",
        "Temporary",
        "Internship",
        "Freelance",
        "Apprenticeship",
        "Seasonal",
        "Volunteer",
        "Remote"
    };

    private static final String[] JOB_TYPES_DE = {
        "Vollzeit", "Teilzeit", "Vertrag", "Befristet", "Praktikum",
        "Freelance", "Ausbildung", "Saisonal", "Ehrenamt", "Remote"
    };

    private static final String[] JOB_TYPES_FR = {
        "Temps plein", "Temps partiel", "Contrat", "Temporaire", "Stage",
        "Freelance", "Apprentissage", "Saisonnier", "Bénévole", "À distance"
    };

    private static final String[] JOB_TYPES_ES = {
        "Tiempo completo", "Medio tiempo", "Contrato", "Temporal", "Pasantía",
        "Freelance", "Aprendizaje", "Estacional", "Voluntario", "Remoto"
    };

    private static final String[] JOB_TYPES_IT = {
        "Tempo pieno", "Part-time", "Contratto", "Temporaneo", "Tirocinio",
        "Freelance", "Apprendistato", "Stagionale", "Volontario", "Remoto"
    };

    private static final String[] JOB_TYPES_PT = {
        "Tempo integral", "Meio período", "Contrato", "Temporário", "Estágio",
        "Freelancer", "Aprendizagem", "Sazonal", "Voluntário", "Remoto"
    };

    private static final String[] JOB_TYPES_JA = {
        "正社員", "パートタイム", "契約", "臨時", "インターンシップ",
        "フリーランス", "見習い", "季節雇用", "ボランティア", "リモート"
    };

    private static final String[] JOB_TYPES_ZH = {
        "全职", "兼职", "合同制", "临时", "实习",
        "自由职业", "学徒制", "季节性", "志愿者", "远程"
    };

    private final GeneratorConfig config;
    private final Random          random;
    private final String[]        jobTypes;

    public JobTypeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public JobTypeGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public JobTypeGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
        this.jobTypes = resolveJobTypes(config.getLocale());
    }

    private static String[] resolveJobTypes(Locale locale) {
        String language = locale.getLanguage();
        return switch (language) {
            case "de" -> JOB_TYPES_DE;
            case "fr" -> JOB_TYPES_FR;
            case "es" -> JOB_TYPES_ES;
            case "it" -> JOB_TYPES_IT;
            case "pt" -> JOB_TYPES_PT;
            case "ja" -> JOB_TYPES_JA;
            case "zh" -> JOB_TYPES_ZH;
            default -> JOB_TYPES_EN;
        };
    }

    @Override
    public String generate() {
        return jobTypes[random.nextInt(jobTypes.length)];
    }

    public Locale getLocale() {
        return config.getLocale();
    }
}
