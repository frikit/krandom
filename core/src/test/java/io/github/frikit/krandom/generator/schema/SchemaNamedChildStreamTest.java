/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.text.ProviderTemplateGenerator;
import io.github.frikit.krandom.generator.text.WordGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Schema named child streams")
class SchemaNamedChildStreamTest {

    private static final GeneratorConfig CONFIG = GeneratorConfig.builder()
            .seed(73L)
            .clock(Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
            .build();

    @Test
    @DisplayName("unrelated columns do not perturb seeded values at existing columns")
    void unrelatedColumnsDoNotPerturbExistingColumns() {
        Schema original = new Schema(CONFIG, orderedFields("first", "second"));
        Schema expanded = new Schema(CONFIG, orderedFields("unrelated", "first", "second"));

        List<Map<String, Object>> originalRecords = original.nextRecords(3);
        List<Map<String, Object>> expandedRecords = expanded.nextRecords(3);

        for (int recordIndex = 0; recordIndex < originalRecords.size(); recordIndex++) {
            assertEquals(
                    originalRecords.get(recordIndex).get("first"),
                    expandedRecords.get(recordIndex).get("first"));
            assertEquals(
                    originalRecords.get(recordIndex).get("second"),
                    expandedRecords.get(recordIndex).get("second"));
        }
    }

    @Test
    @DisplayName("a recipe replays every schema column across record indexes")
    void recipeReplaysSchemaColumns() {
        GenerationRecipe recipe = CONFIG.getGenerationRecipe().orElseThrow();
        Schema original = new Schema(CONFIG, orderedFields("first", "second"));
        Schema replay = new Schema(recipe.toGeneratorConfig(), orderedFields("first", "second"));

        assertEquals(original.nextRecords(4), replay.nextRecords(4));
    }

    @Test
    @DisplayName("schema failures print a safe portable replay recipe")
    void schemaFailureCarriesSafeReplayRecipe() {
        Schema schema = new Schema(
                GeneratorConfig.builder().seed("do-not-log-this-text").build(), Map.of("broken", context -> {
                    throw new IllegalStateException("fixture failure");
                }));

        SchemaGenerationException error = assertThrows(SchemaGenerationException.class, schema::nextRecord);

        String recipe = error.getReplayRecipe().orElseThrow();
        assertTrue(error.getMessage().contains("Replay recipe:"));
        assertTrue(recipe.contains("format=krandom-recipe"));
        assertFalse(recipe.contains("do-not-log-this-text"));
    }

    @Test
    @DisplayName("public schema failure constructor has no replay recipe")
    void publicSchemaFailureConstructorHasNoReplayRecipe() {
        SchemaGenerationException error =
                new SchemaGenerationException("broken", 0, new IllegalStateException("fixture failure"));

        assertTrue(error.getReplayRecipe().isEmpty());
    }

    @Test
    @DisplayName("Field references and template placeholders draw from each value's named stream")
    void fieldProvidersDrawFromNamedStreams() {
        // Fresh fields and schemas, with an unrelated column of the same kind first: it used to take the
        // shared generator's first values, and so change the values of the column after it.
        for (String reference : List.of("person.first_name", "custom.word", "template")) {
            List<Map<String, Object>> alone = new Schema(CONFIG, columns(reference, "x")).nextRecords(5);
            List<Map<String, Object>> expanded =
                    new Schema(CONFIG, columns(reference, "unrelated", "x")).nextRecords(5);
            for (int i = 0; i < 5; i++) {
                assertEquals(alone.get(i).get("x"), expanded.get(i).get("x"), reference);
            }
            // Equal columns draw from streams of their own, so they differ, and a recipe replays them.
            long repeated = expanded.stream()
                    .filter(record -> record.get("x").equals(record.get("unrelated")))
                    .count();
            assertTrue(repeated < 2, reference + ": " + expanded);
            GeneratorConfig replay = CONFIG.getGenerationRecipe().orElseThrow().toGeneratorConfig();
            assertEquals(expanded, new Schema(replay, columns(reference, "unrelated", "x")).nextRecords(5));
        }
    }

    @Test
    @DisplayName("a parsed column that its name resolves draws from its own named stream")
    void parsedSemanticColumnsDrawFromNamedStreams() {
        // Both names resolve to person.email, whose generator the two columns used to share.
        Map<String, Object> wider = new LinkedHashMap<>();
        wider.put("emailAddress", Map.of());
        wider.put("email", Map.of());
        List<Map<String, Object>> alone = SchemaParser.fromJsonSchema(
                        Map.of("type", "object", "properties", Map.of("email", Map.of())), CONFIG)
                .nextRecords(5);
        List<Map<String, Object>> expanded = SchemaParser.fromJsonSchema(
                        Map.of("type", "object", "properties", wider), CONFIG)
                .nextRecords(5);
        for (int i = 0; i < 5; i++) {
            assertEquals(alone.get(i).get("email"), expanded.get(i).get("email"));
        }
    }

    @Test
    @DisplayName("schemas without named streams, and fields with a source of their own, keep their values")
    void otherSourcesKeepTheirValues() {
        GeneratorConfig portable = GeneratorConfig.builder().seed(42L).build();
        // Seeded, but with no portable recipe, so the schema's columns share one sequential stream.
        GeneratorConfig sequential =
                portable.toBuilder().objectExcludeField("ghost").build();
        assertEquals(
                List.of(
                        pinnedRecord("Victor", "84-ah", "vaerksmoondmeestsweeld", "fompfoo"),
                        pinnedRecord("Kenneth", "58-ar", "slungproold", "theangrinsturtduit"),
                        pinnedRecord("Mia", "22-dp", "dreerdzump", "trochemptoand")),
                new Schema(sequential, pinned(new Field(sequential))).nextRecords(3));
        // A field whose configuration owns its random source keeps it, even in a schema with named streams.
        Field owned = new Field(GeneratorConfig.builder().random(new Random(5L)).build());
        assertEquals(
                List.of(
                        pinnedRecord("Austin", "65-ak", "thousshondrairk", "baengfuist"),
                        pinnedRecord("Paisley", "75-uo", "spainhaentsienfourt", "pruishcrourt"),
                        pinnedRecord("Mia", "73-dm", "tringkuird", "prarddraer")),
                new Schema(portable, pinned(owned)).nextRecords(3));
        // A factory-made or secure source is the field's own too: with a factory, a schema with named streams
        // gives what one without them gives, and secure values differ from schema to schema, as values seeded
        // from the named streams would not.
        GeneratorConfig factory =
                GeneratorConfig.builder().randomFactory(() -> new Random(5L)).build();
        assertEquals(
                new Schema(sequential, pinned(new Field(factory))).nextRecords(3),
                new Schema(portable, pinned(new Field(factory))).nextRecords(3));
        GeneratorConfig secure = GeneratorConfig.builder().secureRandom().build();
        assertNotEquals(
                new Schema(portable, pinned(new Field(secure))).nextRecords(3),
                new Schema(portable, pinned(new Field(secure))).nextRecords(3));
        // A provider template passes its own context, which is no named stream.
        ProviderTemplateGenerator template = new ProviderTemplateGenerator("{firstname} ##-?? {word}", portable);
        assertEquals("Victor 84-ah vaerksmoondmeestsweeld", template.generate());
        assertEquals("Kenneth 58-ar fompfoo", template.generate());
        // A parsed schema without a portable recipe.
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("firstName", Map.of("type", "string"));
        properties.put("email", Map.of());
        assertEquals(
                List.of(
                        Map.of("firstName", "Victor", "email", "caroline.tran@example.com"),
                        Map.of("firstName", "Kenneth", "email", "steven_mitchell@example.net")),
                SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", properties), sequential)
                        .nextRecords(2));
    }

    @Test
    @DisplayName("Field values from named streams keep the values they first gave")
    void namedStreamValuesArePinned() {
        GeneratorConfig portable = GeneratorConfig.builder().seed(42L).build();
        // A template column gives the same value alone and after another such column.
        Field alone = new Field(portable);
        Map<String, SchemaValueProvider> single = new LinkedHashMap<>();
        single.put("x", alone.template("##"));
        assertEquals("37", new Schema(portable, single).nextRecord().get("x"));
        Field after = new Field(portable);
        Map<String, SchemaValueProvider> pair = new LinkedHashMap<>();
        pair.put("before", after.template("##"));
        pair.put("x", after.template("##"));
        assertEquals("37", new Schema(portable, pair).nextRecord().get("x"));
        // A reference, a template, and a list of references.
        assertEquals(
                List.of(
                        pinnedRecord("Grace", "62-ls", "droushsuldchassiest", "smierdclou"),
                        pinnedRecord("Ivy", "02-us", "chogreasnermean", "glaeldstae")),
                new Schema(portable, pinned(new Field(portable))).nextRecords(2));
        // A reference that registerProvider adds.
        Field provider = new Field(portable)
                .registerProvider("custom.word", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord);
        Map<String, SchemaValueProvider> word = new LinkedHashMap<>();
        word.put("word", provider.bind("custom.word"));
        assertEquals(
                List.of(Map.of("word", "sweempplearknut"), Map.of("word", "duinkootraemp")),
                new Schema(portable, word).nextRecords(2));
        // Parsed columns that their names resolve.
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("firstName", Map.of("type", "string"));
        properties.put("email", Map.of());
        assertEquals(
                List.of(
                        Map.of("firstName", "Stella", "email", "landonhill@example.net"),
                        Map.of("firstName", "Dennis", "email", "savannah_edwards@example.com")),
                SchemaParser.fromJsonSchema(Map.of("type", "object", "properties", properties), portable)
                        .nextRecords(2));
    }

    @Test
    @DisplayName("SchemaOutputBenchmark's schema keeps the values its named streams first gave")
    void benchmarkSchemaValuesArePinned() {
        GeneratorConfig config = GeneratorConfig.builder()
                .locale(Locale.US)
                .seed(7L)
                .clock(Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC))
                .build();
        Field field = new Field(config);
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("orderId", field.bind("code.uuid"));
        fields.put("customer", field.bind("person.full_name"));
        fields.put("email", field.bind("person.email"));
        fields.put("phone", field.bind("address.phone_number"));
        fields.put("currency", field.bind("finance.currency"));
        fields.put("amount", field.bind("finance.money"));
        fields.put("shipTo", field.bind("address.street_address"));
        fields.put("city", field.bind("address.city"));
        fields.put("state", field.bind("address.state"));
        fields.put("postalCode", field.bind("address.postal_code"));
        fields.put("createdAt", field.bind("datetime.timestamp"));
        fields.put("notes", field.bind("text.sentence"));

        assertEquals(
                List.of(
                        orderedRecord(
                                "326c463e-6517-400e-99ea-a1a12914b3ca",
                                "Gary Peterson",
                                "mgriffin@example.com",
                                "618-555-0114",
                                "USD",
                                "$7,134.60",
                                "8731 Forest Aly",
                                "Hayward",
                                "District of Columbia",
                                "20355",
                                2063491200L,
                                "Smeamgraingluisjit roldjaerksiengstiend vieldsaint zaestthelrour comchund"
                                        + " rumppreeroanthoat seasfaird clielcrampgoutnoold chirruint"
                                        + " mourdsnorjierdswerd praerkkaet smotgirkcreetcrurk stailgathwairgaen"
                                        + " foohingprurtfe neaboongglem crildswooshsmaen."),
                        orderedRecord(
                                "7e434675-0b7c-4c67-a1c0-91f547597f84",
                                "Chloe Gonzales",
                                "nkim@example.net",
                                "(346) 555-0111",
                                "USD",
                                "$7,894.54",
                                "1758 River Pike",
                                "Evansville",
                                "Missouri",
                                "64650",
                                1391385600L,
                                "Chuirtninsweald zountprairdrirdshuirk foumootneeshzan puishrush snartdrol"
                                        + " steertploont zerdmithmear wainsiest geensnoastdruingiel"
                                        + " swooldpeangfrotriend heertbotploor craerpes sleentoaldfroonork"
                                        + " brirkruird.")),
                new Schema(config, fields).nextRecords(2));
    }

    /** A record of the benchmark's columns, in their order. */
    private static Map<String, Object> orderedRecord(Object... values) {
        List<String> names = List.of(
                "orderId",
                "customer",
                "email",
                "phone",
                "currency",
                "amount",
                "shipTo",
                "city",
                "state",
                "postalCode",
                "createdAt",
                "notes");
        Map<String, Object> record = new LinkedHashMap<>();
        for (int i = 0; i < names.size(); i++) {
            record.put(names.get(i), values[i]);
        }
        return record;
    }

    private static Map<String, SchemaValueProvider> columns(String reference, String... names) {
        Field field = new Field(CONFIG)
                .registerProvider("custom.word", WordGenerator::new, WordGenerator.class, WordGenerator::generateWord);
        Map<String, SchemaValueProvider> columns = new LinkedHashMap<>();
        for (String name : names) {
            columns.put(
                    name, reference.equals("template") ? field.template("##-?? {{text.word}}") : field.bind(reference));
        }
        return columns;
    }

    private static Map<String, SchemaValueProvider> pinned(Field field) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        fields.put("name", field.bind("person.first_name"));
        fields.put("code", field.template("##-??"));
        fields.put("words", field.list("text.word", 2, 2));
        return fields;
    }

    private static Map<String, Object> pinnedRecord(String name, String code, String... words) {
        return Map.of("name", name, "code", code, "words", List.of(words));
    }

    private static Map<String, SchemaValueProvider> orderedFields(String... names) {
        Map<String, SchemaValueProvider> fields = new LinkedHashMap<>();
        for (String name : names) {
            fields.put(name, context -> context.random().nextLong());
        }
        return fields;
    }
}
