/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.ChildStreamPolicy;
import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.provider.ProviderSchemaProjection;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Each built-in reference in a seeded Schema gives, for each value, what a generator built for that value
 * gives: the definition of the named streams since 3.0.1, whichever way FieldLookup gets there.
 */
@DisplayName("built-in references in a seeded Schema give what a build for each value gives")
class SchemaFreshBuildOracleTest {

    private static final int RECORDS = 40;

    @Test
    @DisplayName("every built-in schema reference, in several locales and both child-stream policies")
    void everyBuiltInReferenceMatchesAFreshBuildForEachValue() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);
        for (Locale locale : List.of(Locale.US, Locale.GERMANY, Locale.forLanguageTag("pt-BR"))) {
            for (ChildStreamPolicy policy : ChildStreamPolicy.values()) {
                GeneratorConfig config = GeneratorConfig.builder()
                        .locale(locale)
                        .clock(clock)
                        .childStreamPolicy(policy)
                        .seed(73L)
                        .build();
                for (ProviderDescriptor<?> descriptor : ProviderCatalog.schemaBuiltIns()) {
                    assertDescriptor(config, descriptor);
                }
            }
        }
    }

    private static <T> void assertDescriptor(GeneratorConfig config, ProviderDescriptor<T> descriptor) {
        long recipeSeed = config.getGenerationRecipe().orElseThrow().getSeed();
        for (ProviderSchemaProjection<T> projection : descriptor.getSchemaProjections()) {
            String reference = projection.getReference();
            // A value whose generation fails, such as banking data under the default safety policy, is compared
            // by the class of what it throws.
            List<Object> expected = new ArrayList<>();
            try {
                for (int record = 0; record < RECORDS; record++) {
                    long seed = new Random(GenerationRecipe.deriveChildSeed(
                                    recipeSeed, "schema|record=" + record + "|field=1:v"))
                            .nextLong();
                    GeneratorConfig valueConfig = config.toBuilder().seed(seed).build();
                    T valueProvider = descriptor.create(valueConfig);
                    try {
                        expected.add(projection.extract(valueProvider, valueConfig));
                    } catch (RuntimeException failure) {
                        expected.add("threw " + failure.getClass().getName());
                    }
                }
            } catch (UnsupportedOperationException unsupportedLocale) {
                continue;
            }
            Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
            fields.put("v", new Field(config).bind(reference));
            Schema schema = new Schema(config, fields);
            List<Object> actual = new ArrayList<>();
            for (int record = 0; record < RECORDS; record++) {
                try {
                    actual.add(schema.generate().get("v"));
                } catch (SchemaGenerationException failure) {
                    actual.add("threw " + failure.getCause().getClass().getName());
                }
            }
            assertEquals(expected, actual, reference + " " + config.getLocale() + " " + config.getChildStreamPolicy());
        }
    }
}
