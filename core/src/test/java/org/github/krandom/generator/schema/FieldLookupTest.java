/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.schema;

import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.provider.ConflictPolicy;
import org.github.krandom.generator.text.WordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("FieldLookup")
class FieldLookupTest {

    @Test
    @DisplayName("supported references are exposed")
    void supportedReferences() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        Set<String> refs = lookup.supportedReferences();
        assertTrue(refs.contains("person.full_name"));
        assertTrue(refs.contains("company.info"));
        assertTrue(refs.contains("commerce.order_info"));
        assertTrue(refs.contains("finance.payment_info"));
        assertTrue(refs.contains("finance.currency_iso_code"));
        assertTrue(refs.contains("text.paragraph"));
        assertTrue(refs.contains("code.uuid4"));
        assertTrue(lookup.has("order_info"));
        assertTrue(lookup.has("payment_info"));
        assertTrue(lookup.has("finance.currency"));
        assertTrue(lookup.has("finance.money"));
        assertTrue(lookup.has("code.uuid"));
        assertTrue(lookup.has("address.street_address"));
        assertEquals("finance.currency_iso_code", lookup.aliases().get("finance.currency"));
        assertEquals("code.uuid4", lookup.aliases().get("code.uuid"));
        assertEquals("commerce.order_info", lookup.aliases().get("order_info"));
    }

    @Test
    @DisplayName("resolves and generates values for all built-in references")
    void resolveAllBuiltins() {
        GeneratorConfig config = GeneratorConfig.builder().seed(77L).locale(Locale.US).build();
        FieldLookup lookup = new FieldLookup(config);
        SchemaContext context = new SchemaContext(Locale.US, new Random(1L), 0);

        for (String ref : lookup.supportedReferences()) {
            Object value = lookup.resolve(ref).generate(context);
            assertNotNull(value, "Expected non-null for " + ref);
        }
    }

    @Test
    @DisplayName("built-in references expose JSON Schema metadata without generation")
    void builtInReferencesExposeJsonSchemaMetadata() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.builder().seed(77L).locale(Locale.US).build());

        assertEquals("string", lookup.resolve("person.email").jsonSchema().get("type"));
        assertEquals("email", lookup.resolve("person.email").jsonSchema().get("format"));
        assertEquals("integer", lookup.resolve("datetime.timestamp").jsonSchema().get("type"));

        @SuppressWarnings("unchecked")
        Map<String, Object> orderProperties = (Map<String, Object>) lookup.resolve("commerce.order_info")
                                                                          .jsonSchema()
                                                                          .get("properties");
        assertEquals("string", ((Map<?, ?>) orderProperties.get("orderNumber")).get("type"));
        assertEquals("object", ((Map<?, ?>) orderProperties.get("customer")).get("type"));
    }

    @Test
    @DisplayName("validation for null, blank and unknown references")
    void validation() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        assertThrows(NullPointerException.class, () -> lookup.resolve(null));
        assertThrows(IllegalArgumentException.class, () -> lookup.resolve(" "));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> lookup.resolve("person.unknown"));
        assertTrue(ex.getMessage().contains("Supported references"));
    }

    @Test
    @DisplayName("constructor rejects null config")
    void nullConfig() {
        assertThrows(NullPointerException.class, () -> new FieldLookup(null));
    }

    @Test
    @DisplayName("custom references and aliases are supported")
    void customReferencesAndAliases() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        SchemaContext context = new SchemaContext(Locale.US, new Random(1L), 7);

        lookup.register("custom.order_id", ctx -> "ORD-" + ctx.recordIndex());
        lookup.registerAlias("custom.order", "custom.order_id");

        assertTrue(lookup.has("custom.order_id"));
        assertTrue(lookup.has("custom.order"));
        assertFalse(lookup.has("custom.missing"));
        assertEquals("custom.order_id", lookup.aliases().get("custom.order"));
        assertEquals("ORD-7", lookup.resolve("custom.order").generate(context));
        assertTrue(lookup.supportedReferences().contains("custom.order_id"));
    }

    @Test
    @DisplayName("provider-backed references can be registered from provider factories")
    void providerBackedReferences() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.builder().seed(7L).locale(Locale.US).build());

        lookup.registerProvider("text.word.provider", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord);

        Object value = lookup.resolve("text.word.provider").generate(new SchemaContext(Locale.US, new Random(1L), 0));
        assertTrue(value instanceof String);
        assertFalse(((String) value).isBlank());
    }

    @Test
    @DisplayName("custom registration honors conflict policies and validation")
    void customRegistrationConflictPolicies() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        SchemaContext context = new SchemaContext(Locale.US, new Random(1L), 2);

        lookup.register("custom.code", ctx -> "A");
        assertThrows(IllegalArgumentException.class, () -> lookup.register("custom.code", ctx -> "B"));

        lookup.register("custom.code", ctx -> "B", ConflictPolicy.REPLACE);
        assertEquals("B", lookup.resolve("custom.code").generate(context));

        lookup.registerAlias("custom.alias", "custom.code");
        assertThrows(IllegalArgumentException.class, () -> lookup.registerAlias("custom.alias", "custom.code"));
        lookup.registerAlias("custom.alias", "custom.code", ConflictPolicy.REPLACE);
        lookup.registerAlias("custom.code", "custom.code", ConflictPolicy.REPLACE);

        IllegalArgumentException unknownTarget = assertThrows(IllegalArgumentException.class,
                                                              () -> lookup.registerAlias("custom.missing", "missing.ref"));
        assertTrue(unknownTarget.getMessage().contains("Target field reference"));

        IllegalArgumentException aliasConflict = assertThrows(IllegalArgumentException.class,
                                                              () -> lookup.registerAlias("person.full_name",
                                                                                        "custom.code",
                                                                                        ConflictPolicy.REPLACE));
        assertTrue(aliasConflict.getMessage().contains("Alias conflicts"));

        lookup.register("custom.alias", ctx -> "C", ConflictPolicy.REPLACE);
        assertEquals("C", lookup.resolve("custom.alias").generate(context));
    }

    @Test
    @DisplayName("provider-backed registration validates the resolved provider type")
    void providerBackedRegistrationTypeValidation() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        lookup.registerProvider("broken.provider", cfg -> "not-a-word-generator", WordGenerator.class,
                                WordGenerator::generateWord, ConflictPolicy.REPLACE);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> lookup.resolve("broken.provider")
                                                               .generate(new SchemaContext(Locale.US,
                                                                                           new Random(1L),
                                                                                           0)));
        assertTrue(ex.getMessage().contains("not " + WordGenerator.class.getName()));
    }

    @Test
    @DisplayName("resolve protects against an inconsistent alias pointing to a missing provider")
    void resolveProtectsAgainstInconsistentAliasState() throws ReflectiveOperationException {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());

        Field aliasesField = FieldLookup.class.getDeclaredField("aliases");
        aliasesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> aliases = (Map<String, String>) aliasesField.get(lookup);
        aliases.put("broken.alias", "broken.provider");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> lookup.resolve("broken.alias"));
        assertTrue(ex.getMessage().contains("Unknown field reference"));
    }
}
