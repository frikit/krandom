/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.javaapi;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.GeneratorProfile;
import org.github.krandom.generator.base.IntGenerator;
import org.github.krandom.generator.location.CountryGenerator;
import org.github.krandom.generator.network.URLGenerator;
import org.github.krandom.generator.provider.ProviderHub;
import org.github.krandom.generator.schema.Field;
import org.github.krandom.generator.schema.Schema;
import org.github.krandom.generator.schema.SchemaValueProvider;
import org.github.krandom.generator.text.SentenceGenerator;
import org.github.krandom.generator.text.WordGenerator;
import org.github.krandom.generator.user.EmailGenerator;
import org.github.krandom.generator.user.FullNameGenerator;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Java-focused facade over core generator factories.
 */
public final class Generators {

    private Generators() {
    }

    public static GeneratorConfig defaults() {
        return GeneratorConfig.defaults();
    }

    public static GeneratorConfig.Builder configBuilder() {
        return GeneratorConfig.builder();
    }

    public static GeneratorConfig config(GeneratorProfile profile) {
        return Objects.requireNonNull(profile, "profile").createConfig();
    }

    public static GeneratorConfig config(GeneratorProfile profile, Locale locale) {
        return Objects.requireNonNull(profile, "profile").createConfig(locale);
    }

    public static IntGenerator ofInt() {
        return org.github.krandom.generator.Generators.ofInt();
    }

    public static IntGenerator ofInt(int min, int max) {
        return org.github.krandom.generator.Generators.ofInt(min, max);
    }

    public static IntGenerator ofInt(int min, int max, long seed) {
        return org.github.krandom.generator.Generators.ofInt(min, max, seed);
    }

    public static WordGenerator ofWord() {
        return org.github.krandom.generator.Generators.ofWord();
    }

    public static SentenceGenerator ofSentence() {
        return org.github.krandom.generator.Generators.ofSentence();
    }

    public static URLGenerator ofUrl() {
        return org.github.krandom.generator.Generators.ofUrl();
    }

    public static EmailGenerator ofEmail() {
        return org.github.krandom.generator.Generators.ofEmail();
    }

    public static FullNameGenerator ofFullName() {
        return org.github.krandom.generator.Generators.ofFullName();
    }

    public static CountryGenerator ofCountry() {
        return org.github.krandom.generator.Generators.ofCountry();
    }

    public static <T> Generator<T> forType(Class<T> type) {
        return org.github.krandom.generator.Generators.forType(type);
    }

    public static Field ofField() {
        return org.github.krandom.generator.Generators.ofField();
    }

    public static Field ofField(Locale locale) {
        return org.github.krandom.generator.Generators.ofField(locale);
    }

    public static Schema ofSchema(Map<String, SchemaValueProvider> fields) {
        return org.github.krandom.generator.Generators.ofSchema(fields);
    }

    public static Schema ofSchema(Locale locale, Map<String, SchemaValueProvider> fields) {
        return org.github.krandom.generator.Generators.ofSchema(locale, fields);
    }

    public static Schema ofSchema(GeneratorConfig config, Map<String, SchemaValueProvider> fields) {
        return org.github.krandom.generator.Generators.ofSchema(config, fields);
    }

    public static ProviderHub ofProviderHub() {
        return org.github.krandom.generator.Generators.ofProviderHub();
    }

    public static ProviderHub ofProviderHub(Locale locale) {
        return org.github.krandom.generator.Generators.ofProviderHub(locale);
    }

    public static ProviderHub ofProviderHub(GeneratorConfig config) {
        return org.github.krandom.generator.Generators.ofProviderHub(config);
    }

    public static ProviderHub ofProviderHub(GeneratorProfile profile) {
        return org.github.krandom.generator.Generators.ofProviderHub(profile);
    }

    public static ProviderHub ofProviderHub(Locale locale, GeneratorProfile profile) {
        return org.github.krandom.generator.Generators.ofProviderHub(locale, profile);
    }

    public static ProviderHub ofProviderHub(GeneratorConfig config, GeneratorProfile profile) {
        return org.github.krandom.generator.Generators.ofProviderHub(config, profile);
    }
}
