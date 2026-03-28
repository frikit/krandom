/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.file;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates locale-aware directory paths.
 */
public final class DirPathGenerator implements Generator<String> {

    private static final String[] EN = { "home", "users", "projects", "data", "logs", "tmp", "docs", "assets" };
    private static final String[] DE = { "benutzer", "projekte", "daten", "protokolle", "temp", "dokumente" };
    private static final String[] FR = { "utilisateurs", "projets", "donnees", "journaux", "temp", "documents" };
    private static final String[] ES = { "usuarios", "proyectos", "datos", "registros", "temp", "documentos" };
    private static final String[] IT = { "utenti", "progetti", "dati", "registri", "temp", "documenti" };
    private static final String[] PT = { "usuarios", "projetos", "dados", "logs", "temp", "documentos" };
    private static final String[] JA = { "yuuzaa", "purojekuto", "deeta", "rogu", "tmp", "dokyumento" };
    private static final String[] ZH = { "yonghu", "xiangmu", "shuju", "rizhi", "tmp", "wenjian" };

    private final Locale locale;
    private final Random random;

    public DirPathGenerator() {
        this(GeneratorConfig.defaults());
    }

    public DirPathGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    public DirPathGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.getSeed().isPresent()
                      ? new Random(config.getSeed().getAsLong())
                      : new SecureRandom();
    }

    private static String[] wordsFor(Locale locale) {
        return switch (locale.getLanguage()) {
            case "de" -> DE;
            case "fr" -> FR;
            case "es" -> ES;
            case "it" -> IT;
            case "pt" -> PT;
            case "ja" -> JA;
            case "zh" -> ZH;
            default -> EN;
        };
    }

    @Override
    public String generate() {
        int depth = random.nextInt(2, 5); // [2,4]
        String[] words = wordsFor(locale);
        StringBuilder path = new StringBuilder("/");
        for (int i = 0; i < depth; i++) {
            if (i > 0) {
                path.append('/');
            }
            path.append(words[random.nextInt(words.length)]);
        }
        return path.toString();
    }

    public Locale getLocale() {
        return locale;
    }
}
