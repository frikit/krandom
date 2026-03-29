/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.text;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates char-limited text blocks and text collections.
 */
public final class TextGenerator implements Generator<String> {

    private static final List<String> DEFAULT_WORDS = List.of(
        "alpha", "beta", "gamma", "delta", "vector", "signal", "stream", "token",
        "cloud", "matrix", "engine", "system", "future", "global", "local", "secure"
    );

    private static final Map<String, List<String>> WORDS_BY_LANGUAGE = wordsByLanguage();

    private final Random random;
    private final Locale locale;

    public TextGenerator() {
        this(GeneratorConfig.defaults());
    }

    public TextGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public TextGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.locale = config.getLocale();
        this.random = config.createRandom();
    }

    private static List<String> defaultWordsForLocale(Locale locale) {
        List<String> words = WORDS_BY_LANGUAGE.get(locale.getLanguage());
        return words == null ? DEFAULT_WORDS : words;
    }

    private static Map<String, List<String>> wordsByLanguage() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("en", DEFAULT_WORDS);
        map.put("de", List.of(
            "daten", "modell", "system", "signal", "prozess", "analyse", "plattform", "netz",
            "sicher", "service", "struktur", "modul", "logik", "wert", "kontext", "ziel"
        ));
        map.put("fr", List.of(
            "donnee", "modele", "systeme", "signal", "processus", "analyse", "plateforme", "reseau",
            "secure", "service", "structure", "module", "logique", "valeur", "contexte", "objectif"
        ));
        map.put("es", List.of(
            "dato", "modelo", "sistema", "senal", "proceso", "analisis", "plataforma", "red",
            "seguro", "servicio", "estructura", "modulo", "logica", "valor", "contexto", "objetivo"
        ));
        map.put("it", List.of(
            "dato", "modello", "sistema", "segnale", "processo", "analisi", "piattaforma", "rete",
            "sicuro", "servizio", "struttura", "modulo", "logica", "valore", "contesto", "obiettivo"
        ));
        map.put("pt", List.of(
            "dado", "modelo", "sistema", "sinal", "processo", "analise", "plataforma", "rede",
            "seguro", "servico", "estrutura", "modulo", "logica", "valor", "contexto", "objetivo"
        ));
        map.put("ja", List.of(
            "data", "model", "system", "signal", "process", "analysis", "platform", "network",
            "secure", "service", "module", "logic", "value", "context", "future", "core"
        ));
        map.put("zh", List.of(
            "data", "model", "system", "signal", "process", "analysis", "platform", "network",
            "secure", "service", "module", "logic", "value", "context", "future", "core"
        ));
        return Map.copyOf(map);
    }

    @Override
    public String generate() {
        return generate(200);
    }

    public String generate(int maxChars) {
        return generate(new TextOptions(maxChars, null, false, true));
    }

    public List<String> generateTexts(int count, int maxChars) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive, got: " + count);
        }
        List<String> texts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            texts.add(generate(maxChars));
        }
        return texts;
    }

    public String generate(TextOptions options) {
        Objects.requireNonNull(options, "options must not be null");
        List<String> vocabulary = options.extWordList() == null || options.extWordList().isEmpty()
                                  ? defaultWordsForLocale(locale)
                                  : options.extWordList();

        int targetChars = Math.max(1, options.maxChars());
        int wordCount = options.variableWordCount()
                        ? 4 + random.nextInt(10)
                        : 8;

        List<String> words = pickWords(vocabulary, wordCount, options.uniqueWords());
        StringBuilder out = new StringBuilder(targetChars + 8);
        for (String word : words) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(word);
            if (out.length() >= targetChars) {
                out.setLength(targetChars);
                break;
            }
        }
        if (out.isEmpty()) {
            out.append(locale.getLanguage().equals("de") ? "text" : "lorem");
        }
        if (!out.toString().endsWith(".")) {
            if (out.length() == targetChars) {
                out.setCharAt(out.length() - 1, '.');
            } else {
                out.append('.');
            }
        }
        return out.toString();
    }

    /**
     * Generates text with default options.
     * Alias for {@link #generate()}.
     *
     * @return generated text
     */
    public String generateText() {
        return generate();
    }

    private List<String> pickWords(List<String> vocabulary, int count, boolean unique) {
        if (!unique) {
            List<String> words = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                words.add(vocabulary.get(random.nextInt(vocabulary.size())));
            }
            return words;
        }
        Set<String> selected = new LinkedHashSet<>();
        while (selected.size() < count && selected.size() < vocabulary.size()) {
            selected.add(vocabulary.get(random.nextInt(vocabulary.size())));
        }
        return new ArrayList<>(selected);
    }


    public record TextOptions(
        int maxChars,
        List<String> extWordList,
        boolean uniqueWords,
        boolean variableWordCount
    ) {

        public TextOptions {
            if (maxChars <= 0) {
                throw new IllegalArgumentException("maxChars must be positive, got: " + maxChars);
            }
        }
    }
}
