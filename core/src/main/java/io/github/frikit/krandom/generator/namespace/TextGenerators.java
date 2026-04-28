/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.text.*;

/**
 * Fluent namespace for text-related generators.
 *
 * <p>Usage: {@code Generators.text().sentence().generate()}
 */
public final class TextGenerators {

    private final GeneratorConfig config;

    public TextGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public TextGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public LoremIpsumGenerator loremIpsum() { return new LoremIpsumGenerator(config); }

    public LoremIpsumGenerator loremIpsum(LoremIpsumGenerator.Mode mode) { return new LoremIpsumGenerator(mode, config); }

    public WordGenerator word() { return new WordGenerator(config); }

    public SyllableGenerator syllable() { return new SyllableGenerator(config); }

    public SentenceGenerator sentence() { return new SentenceGenerator(config); }

    public ParagraphGenerator paragraph() { return new ParagraphGenerator(config); }

    public TextGenerator text() { return new TextGenerator(config); }

    public TemplateStringGenerator template(String template) { return new TemplateStringGenerator(template, config); }

    public TemplateStringGenerator template(String template, long seed) { return new TemplateStringGenerator(template, seed); }

    public ProviderTemplateGenerator providerTemplate(String template) {
        return new ProviderTemplateGenerator(template, config);
    }
}
