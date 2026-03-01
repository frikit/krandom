/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company catch phrases similar to Faker's {@code catch_phrase()}.
 */
public final class CompanyCatchPhraseGenerator implements Generator<String> {

    private static final LocaleCatchPhraseData EN = new LocaleCatchPhraseData(
            List.of("Adaptive", "Unified", "Trusted", "Intelligent", "Future-ready", "Effortless", "Secure"),
            List.of("Platform", "Network", "Experience", "Engine", "Ecosystem", "Suite", "Framework"),
            List.of("for modern teams", "for digital growth", "for global scale", "for measurable impact")
    );

    private static final Map<String, LocaleCatchPhraseData> DATA_BY_LANGUAGE = dataByLanguage();

    private final Locale locale;
    private final Random random;

    public CompanyCatchPhraseGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyCatchPhraseGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyCatchPhraseGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        LocaleCatchPhraseData data = DATA_BY_LANGUAGE.getOrDefault(locale.getLanguage(), EN);
        return data.adjectives().get(random.nextInt(data.adjectives().size())) + " "
                + data.nouns().get(random.nextInt(data.nouns().size())) + " "
                + data.taglines().get(random.nextInt(data.taglines().size()));
    }

    private static Map<String, LocaleCatchPhraseData> dataByLanguage() {
        Map<String, LocaleCatchPhraseData> map = new HashMap<>();
        map.put("en", EN);
        map.put("es", new LocaleCatchPhraseData(
                List.of("Innovacion", "Confianza", "Escala", "Impacto", "Agilidad", "Claridad"),
                List.of("Plataforma", "Red", "Experiencia", "Motor", "Ecosistema", "Suite"),
                List.of("para equipos modernos", "para crecimiento digital", "para escala global", "para impacto medible")
        ));
        map.put("de", new LocaleCatchPhraseData(
                List.of("Sicher", "Vernetzt", "Skalierbar", "Effizient", "Intelligent", "Modern"),
                List.of("Plattform", "Netzwerk", "Erlebnis", "Engine", "Oekosystem", "Suite"),
                List.of("fuer moderne teams", "fuer digitales wachstum", "fuer globale skalierung", "fuer messbaren nutzen")
        ));
        map.put("fr", new LocaleCatchPhraseData(
                List.of("Fiable", "Unifie", "Intelligent", "Moderne", "Agile", "Securise"),
                List.of("Plateforme", "Reseau", "Experience", "Moteur", "Ecosysteme", "Suite"),
                List.of("pour equipes modernes", "pour croissance numerique", "pour echelle globale", "pour impact mesurable")
        ));
        return Map.copyOf(map);
    }

    private record LocaleCatchPhraseData(List<String> adjectives, List<String> nouns, List<String> taglines) {
    }
}
