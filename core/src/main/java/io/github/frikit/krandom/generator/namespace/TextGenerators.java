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

    /**
     * Returns a seeded template generator that ignores this namespace's configuration.
     *
     * @param template template text; must not be {@code null}
     * @param seed     raw seed
     * @return template generator
     * @deprecated raw seeds bypass replayable recipes and this namespace's configuration; use
     *             {@code Generators.text(GeneratorConfig.builder().seed(seed).build()).template(template)}
     *             or {@link #template(String)} on a seeded namespace, which produces the same values.
     */
    @Deprecated(since = "2.6.0")
    public TemplateStringGenerator template(String template, long seed) { return new TemplateStringGenerator(template, seed); }

    public ProviderTemplateGenerator providerTemplate(String template) {
        return new ProviderTemplateGenerator(template, config);
    }
}
