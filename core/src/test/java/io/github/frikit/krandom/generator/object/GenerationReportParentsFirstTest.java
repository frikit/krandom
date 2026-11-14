/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.SHIPMENT_REPORT;
import static io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.shipmentReport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.Address;
import io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.Member;
import io.github.frikit.krandom.generator.object.ObjectEngineDefaultsPinTest.Shipment;
import io.github.frikit.krandom.generator.object.exception.ObjectGenerationException;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GeneratorConfig.Builder.generationReportParentsFirst lists each object before its members")
class GenerationReportParentsFirstTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig.Builder seeded() {
        return GeneratorConfig.builder().seed(42L).clock(CLOCK);
    }

    private static GeneratorConfig.Builder parentsFirst() {
        return seeded().generationReportParentsFirst(true);
    }

    private static List<String> lines(List<GenerationReportEntry> entries) {
        return entries.stream().map(entry -> entry.path() + " " + entry.step()).toList();
    }

    private static List<GenerationReportEntry> report(GeneratorConfig.Builder builder, Consumer<GeneratorConfig> run) {
        List<GenerationReportEntry> entries = new ArrayList<>();
        run.accept(builder.generationReportListener(entries::add).build());
        return entries;
    }

    public static class Broken {
        public Address address;
        public Runnable zjob;
    }

    public static class Pair {
        public Address first;
        public Address second;
    }

    @Test
    @DisplayName("an object, a collection, an element and a record come before their members")
    void parentsComeFirst() {
        assertEquals(
                List.of(
                        "Shipment OBJECT",
                        "Shipment.id SEMANTIC",
                        "Shipment.lines COLLECTION",
                        "Shipment.lines[] OBJECT",
                        "Shipment.lines[].quantity BUILT_IN",
                        "Shipment.lines[].sku BUILT_IN",
                        "Shipment.lines[] OBJECT",
                        "Shipment.lines[].quantity BUILT_IN",
                        "Shipment.lines[].sku BUILT_IN",
                        "Shipment.member OBJECT",
                        "Shipment.member.address OBJECT",
                        "Shipment.member.address.city SEMANTIC",
                        "Shipment.member.address.street SEMANTIC",
                        "Shipment.member.address.zip BUILT_IN",
                        "Shipment.member.address.street COHERENCE",
                        "Shipment.member.address.city COHERENCE",
                        "Shipment.member.age SEMANTIC",
                        "Shipment.member.email SEMANTIC",
                        "Shipment.member.name BUILT_IN",
                        "Shipment.member.nickname BUILT_IN",
                        "Shipment.origin OBJECT",
                        "Shipment.origin.x BUILT_IN",
                        "Shipment.origin.y BUILT_IN"),
                shipmentReport(parentsFirst()));
        assertEquals(SHIPMENT_REPORT, shipmentReport(seeded().generationReportParentsFirst(false)));
    }

    @Test
    @DisplayName("the entries, the values and the recipe are the same as without it")
    void entriesValuesAndRecipeAreTheSame() {
        Comparator<GenerationReportEntry> byText = Comparator.comparing(GenerationReportEntry::toString);
        List<Shipment> values = new ArrayList<>();
        List<GenerationReportEntry> on = report(
                parentsFirst().collectionSize(2, 2),
                config -> values.add(new ObjectGenerator<>(Shipment.class, config).generate()));
        List<GenerationReportEntry> off = report(
                seeded().collectionSize(2, 2),
                config -> values.add(new ObjectGenerator<>(Shipment.class, config).generate()));

        assertEquals(
                off.stream().sorted(byText).toList(), on.stream().sorted(byText).toList());
        assertEquals(
                ObjectEngineDefaultsPinTest.show(values.get(1).member),
                ObjectEngineDefaultsPinTest.show(values.get(0).member));
        assertEquals(
                seeded().build().getGenerationRecipe().map(GenerationRecipe::serializeForDiagnostics),
                parentsFirst().build().getGenerationRecipe().map(GenerationRecipe::serializeForDiagnostics));
        assertTrue(parentsFirst().build().isGenerationReportParentsFirst());
        assertFalse(seeded().build().isGenerationReportParentsFirst());
        assertTrue(parentsFirst().build().forChildStream("child").isGenerationReportParentsFirst());
        assertTrue(parentsFirst().build().toBuilder().build().isGenerationReportParentsFirst());
    }

    @Test
    @DisplayName("a generation that fails still delivers the entries it made, parents first")
    void failedGenerationDeliversItsEntries() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig config =
                parentsFirst().generationReportListener(entries::add).build();

        assertThrows(ObjectGenerationException.class, () -> new ObjectGenerator<>(Broken.class, config).generate());

        assertEquals(
                List.of(
                        "Broken OBJECT",
                        "Broken.address OBJECT",
                        "Broken.address.city SEMANTIC",
                        "Broken.address.street SEMANTIC",
                        "Broken.address.zip BUILT_IN",
                        "Broken.address.street COHERENCE",
                        "Broken.address.city COHERENCE"),
                lines(entries));
    }

    @Test
    @DisplayName("ObjectFaker rules, populate(existing) and generations inside a generation keep the order")
    void fakerPopulateAndInnerGenerations() {
        List<String> faker = lines(report(
                parentsFirst(),
                config -> new ObjectFaker<>(Member.class, config)
                        .ruleFor("address.city", () -> "C")
                        .generate()));
        assertEquals("Member OBJECT", faker.get(0));
        assertEquals("Member.address OBJECT", faker.get(1));
        assertEquals("Member.address.city RULE", faker.get(faker.size() - 1));

        List<String> populated =
                lines(report(parentsFirst(), config -> new ObjectGenerator<>(Pair.class, config).populate(new Pair())));
        assertEquals("Pair.first OBJECT", populated.get(0));
        assertEquals("Pair.second OBJECT", populated.get(populated.size() / 2));

        // A field override that generates another object with the same listener reports it inside its member.
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig.Builder builder = parentsFirst().generationReportListener(entries::add);
        GeneratorConfig inner = builder.build();
        GeneratorConfig outer = builder.objectOverride(
                        Pair.class, "first", () -> new ObjectGenerator<>(Address.class, inner).generate())
                .build();
        new ObjectGenerator<>(Pair.class, outer).generate();
        List<String> nested = lines(entries);
        assertEquals("Pair OBJECT", nested.get(0));
        assertEquals("Pair.first OVERRIDE", nested.get(1));
        assertEquals("Address OBJECT", nested.get(2));
        assertTrue(nested.indexOf("Pair.second OBJECT") > nested.indexOf("Address.zip BUILT_IN"), nested.toString());
    }

    @Test
    @DisplayName("without a listener nothing is reported and the values are the same")
    void withoutAListener() {
        Member on = new ObjectGenerator<>(Member.class, parentsFirst().build()).generate();
        Member off = new ObjectGenerator<>(Member.class, seeded().build()).generate();

        assertEquals(ObjectEngineDefaultsPinTest.show(off), ObjectEngineDefaultsPinTest.show(on));
    }
}
