/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy;
import io.github.frikit.krandom.generator.location.PhoneNumberSafetyPolicy;
import io.github.frikit.krandom.generator.provider.ConflictPolicy;
import io.github.frikit.krandom.generator.provider.ProviderCatalog;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;
import io.github.frikit.krandom.generator.provider.ProviderSchemaProjection;
import io.github.frikit.krandom.generator.text.WordGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    @DisplayName("built-in schema references and aliases are defined by the provider catalog")
    void builtInsAreDefinedByProviderCatalog() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        Set<String> references = new java.util.HashSet<>();
        Map<String, String> aliases = new java.util.HashMap<>();

        for (ProviderDescriptor<?> descriptor : ProviderCatalog.schemaBuiltIns()) {
            for (ProviderSchemaProjection<?> projection : descriptor.getSchemaProjections()) {
                references.add(projection.getReference());
                for (String alias : projection.getAliases()) {
                    aliases.put(alias, projection.getReference());
                }
            }
        }

        assertEquals(references, lookup.supportedReferences());
        assertEquals(aliases, lookup.aliases());
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

        @SuppressWarnings("unchecked")
        Map<String, Object> paymentProperties = (Map<String, Object>) lookup.resolve("finance.payment_info")
                                                                            .jsonSchema()
                                                                            .get("properties");
        assertTrue(allowsNull((Map<?, ?>) paymentProperties.get("settledOn")));

        @SuppressWarnings("unchecked")
        Map<String, Object> shipmentProperties = (Map<String, Object>) lookup.resolve("commerce.shipment_info")
                                                                             .jsonSchema()
                                                                             .get("properties");
        assertTrue(allowsNull((Map<?, ?>) shipmentProperties.get("deliveredOn")));
    }

    @Test
    @DisplayName("classified schema references expose their selected safety policy")
    void classifiedReferencesExposeSelectedSafetyPolicy() {
        FieldLookup defaults = new FieldLookup(GeneratorConfig.defaults());
        FieldLookup validatorFixtures = new FieldLookup(GeneratorConfig.builder()
                                                                      .paymentCardSafetyPolicy(PaymentCardSafetyPolicy.CHECKSUM_VALID)
                                                                      .phoneNumberSafetyPolicy(
                                                                          PhoneNumberSafetyPolicy.REALISTIC_UNCLASSIFIED)
                                                                      .build());

        assertEquals("GUARANTEED", safetyMetadata(defaults, "finance.credit_card_number").get("formatValidity"));
        assertEquals("CONFIGURATION_DEPENDENT",
                     safetyMetadata(defaults, "finance.credit_card_number").get("checksumValidity"));
        assertEquals(Map.of("setting", "payment.card-safety-policy", "selected", "TEST_SAFE_NON_ROUTABLE"),
                     safetyMetadata(defaults, "finance.credit_card_number").get("policy"));
        assertEquals(Map.of("setting", "phone-number.safety-policy", "selected", "TEST_SAFE_WHERE_AVAILABLE"),
                     safetyMetadata(defaults, "address.phone_number").get("policy"));
        assertEquals(Map.of("setting", "payment.card-safety-policy", "selected", "CHECKSUM_VALID"),
                     safetyMetadata(validatorFixtures, "finance.credit_card_number").get("policy"));
        assertEquals(Map.of("setting", "phone-number.safety-policy", "selected", "REALISTIC_UNCLASSIFIED"),
                     safetyMetadata(validatorFixtures, "address.phone_number").get("policy"));
        assertNull(defaults.resolve("finance.cvv").jsonSchema().get("x-krandom-safety"));

        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) new Schema(Map.of(
            "card", defaults.resolve("finance.credit_card_number"))).toJsonSchema().get("properties");
        assertEquals(safetyMetadata(defaults, "finance.credit_card_number"),
                     ((Map<?, ?>) properties.get("card")).get("x-krandom-safety"));
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
    @DisplayName("reference and alias views are immutable snapshots")
    void referenceAndAliasViewsAreImmutableSnapshots() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        Set<String> references = lookup.supportedReferences();
        Map<String, String> aliases = lookup.aliases();

        lookup.register("custom.snapshot", ctx -> "snapshot");
        lookup.registerAlias("snapshot", "custom.snapshot");

        assertFalse(references.contains("custom.snapshot"));
        assertFalse(aliases.containsKey("snapshot"));
        assertThrows(UnsupportedOperationException.class, () -> references.add("other"));
        assertThrows(UnsupportedOperationException.class, () -> aliases.put("other", "custom.snapshot"));
    }

    @Test
    @DisplayName("provider-backed references can be registered from provider factories")
    void providerBackedReferences() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.builder().seed(7L).locale(Locale.US).build());

        lookup.registerProvider("custom.counter",
                                CountingProvider::new,
                                CountingProvider.class,
                                CountingProvider::next,
                                Map.of("type", "string"));

        SchemaValueProvider provider = lookup.resolve("custom.counter");
        assertEquals("value-0", provider.generate(new SchemaContext(Locale.US, new Random(1L), 0)));
        assertEquals("value-1", provider.generate(new SchemaContext(Locale.US, new Random(1L), 1)));
        assertEquals("string", provider.jsonSchema().get("type"));
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

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                                                   () -> lookup.registerProvider("broken.provider",
                                                                                 cfg -> "not-a-word-generator",
                                                                                 WordGenerator.class,
                                                                                 WordGenerator::generateWord,
                                                                                 ConflictPolicy.REPLACE));
        assertTrue(ex.getMessage().contains("not " + WordGenerator.class.getName()));
    }

    @Test
    @DisplayName("provider-backed registration checks conflicts before creating providers")
    void providerBackedRegistrationChecksConflictsBeforeCreatingProviders() {
        FieldLookup lookup = new FieldLookup(GeneratorConfig.defaults());
        AtomicInteger calls = new AtomicInteger();
        lookup.register("custom.target", ctx -> "target");
        lookup.registerAlias("custom.alias", "custom.target");

        assertThrows(IllegalArgumentException.class,
                     () -> lookup.registerProvider("custom.target",
                                                   cfg -> {
                                                       calls.incrementAndGet();
                                                       return new CountingProvider(cfg);
                                                   },
                                                   CountingProvider.class,
                                                   CountingProvider::next));
        assertThrows(IllegalArgumentException.class,
                     () -> lookup.registerProvider("custom.alias",
                                                   cfg -> {
                                                       calls.incrementAndGet();
                                                       return new CountingProvider(cfg);
                                                   },
                                                   CountingProvider.class,
                                                   CountingProvider::next));
        assertEquals(0, calls.get());

        lookup.registerProvider("custom.target",
                                cfg -> {
                                    calls.incrementAndGet();
                                    return new CountingProvider(cfg);
                                },
                                CountingProvider.class,
                                CountingProvider::next,
                                ConflictPolicy.REPLACE);
        lookup.registerProvider("custom.alias",
                                cfg -> {
                                    calls.incrementAndGet();
                                    return new CountingProvider(cfg);
                                },
                                CountingProvider.class,
                                CountingProvider::next,
                                ConflictPolicy.REPLACE);
        assertEquals(2, calls.get());
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

    private static boolean allowsNull(Map<?, ?> schema) {
        Object type = schema.get("type");
        return type instanceof Iterable<?> types && contains(types, "null");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> safetyMetadata(FieldLookup lookup, String reference) {
        return (Map<String, Object>) lookup.resolve(reference).jsonSchema().get("x-krandom-safety");
    }

    private static boolean contains(Iterable<?> values, Object expected) {
        for (Object value : values) {
            if (expected.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static final class CountingProvider {

        private int next;

        private CountingProvider(GeneratorConfig config) {
            assertNotNull(config);
        }

        private String next() {
            return "value-" + next++;
        }
    }
}
