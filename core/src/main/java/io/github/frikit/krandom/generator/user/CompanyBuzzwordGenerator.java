/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates company buzzword phrases similar to Faker's {@code bs()}.
 */
public final class CompanyBuzzwordGenerator implements Generator<String> {

    private static final LocaleBuzzwordData EN = new LocaleBuzzwordData(
        List.of("streamline", "empower", "leverage", "optimize", "synergize", "scale", "deliver", "enable"),
        List.of("cross-platform", "end-to-end", "best-in-class", "frictionless", "cloud-native", "data-driven"),
        List.of("solutions", "workflows", "infrastructure", "platforms", "experiences", "capabilities")
    );

    private static final Map<String, LocaleBuzzwordData> DATA_BY_LANGUAGE = dataByLanguage();

    private final Locale locale;
    private final Random random;

    public CompanyBuzzwordGenerator() {
        this(GeneratorConfig.defaults());
    }

    public CompanyBuzzwordGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public CompanyBuzzwordGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.createRandom();
    }

    private static Map<String, LocaleBuzzwordData> dataByLanguage() {
        Map<String, LocaleBuzzwordData> map = new HashMap<>();
        map.put("en", EN);
        map.put("de", new LocaleBuzzwordData(
            List.of("digitalisiere", "staerke", "optimiere", "automatisiere", "skaliere", "verbinde"),
            List.of("datenzentrierte", "cloudbasierte", "nahtlose", "integrierte", "effiziente", "modulare"),
            List.of("prozesse", "plattformen", "netzwerke", "services", "workflows", "loesungen")
        ));
        map.put("fr", new LocaleBuzzwordData(
            List.of("optimiser", "renforcer", "accelerer", "structurer", "connecter", "transformer"),
            List.of("numerique", "agile", "integree", "modulaire", "fiable", "performante"),
            List.of("processus", "plateformes", "services", "workflows", "ecosystemes", "solutions")
        ));
        map.put("es", new LocaleBuzzwordData(
            List.of("optimizar", "potenciar", "acelerar", "integrar", "escalar", "automatizar"),
            List.of("digital", "agil", "integrada", "modular", "segura", "eficiente"),
            List.of("procesos", "plataformas", "servicios", "flujos", "ecosistemas", "soluciones")
        ));
        return Map.copyOf(map);
    }

    @Override
    public String generate() {
        LocaleBuzzwordData data = DATA_BY_LANGUAGE.getOrDefault(locale.getLanguage(), EN);
        return data.lead().get(random.nextInt(data.lead().size())) + " "
               + data.middle().get(random.nextInt(data.middle().size())) + " "
               + data.tail().get(random.nextInt(data.tail().size()));
    }


    private record LocaleBuzzwordData(List<String> lead, List<String> middle, List<String> tail) {

    }
}
