/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Seeded values and reports of {@code ObjectFaker} and object generation that the 3.2 object-engine opt-ins
 * must leave as they are, recorded on {@code main} before the opt-ins existed: a root rule or root ignore drops
 * the recipe and moves the other members off their named streams, and the report lists a nested object after
 * its members.
 */
@DisplayName("with no object-engine opt-in, seeded values and reports stay as 3.1 gave them")
class ObjectEngineDefaultsPinTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-07-10T12:00:00Z"), ZoneOffset.UTC);

    private static GeneratorConfig.Builder seeded() {
        return GeneratorConfig.builder().seed(42L).clock(CLOCK);
    }

    public static class Address {
        public String street;
        public String city;
        public int zip;
    }

    public static class Member {
        public String name;
        public String nickname;
        public int age;
        public Address address;
        public String email;
    }

    public record Point(int x, int y) {}

    public static class Line {
        public String sku;
        public int quantity;
    }

    public static class Shipment {
        public String id;
        public Member member;
        public List<Line> lines;
        public Point origin;
    }

    static String show(Member member) {
        return member.name + "|" + member.nickname + "|" + member.age + "|" + member.email + "|" + member.address.street
                + "/" + member.address.city + "/" + member.address.zip;
    }

    private static List<String> twice(ObjectFaker<Member> faker) {
        return List.of(show(faker.generate()), show(faker.generate()));
    }

    private static List<Optional<String>> recipeFormats(Function<GeneratorConfig, ObjectFaker<Member>> faker) {
        List<GenerationReportEntry> entries = new ArrayList<>();
        faker.apply(seeded().generationReportListener(entries::add).build()).generate();
        return entries.stream()
                .map(GenerationReportEntry::recipeFormat)
                .distinct()
                .toList();
    }

    @Test
    @DisplayName("a root rule and a root ignore move the other members off their seeded values")
    void rootRulesMoveUnrelatedValues() {
        GeneratorConfig config = seeded().build();
        Function<Member, String> dependent = member -> "X";

        List<String> values = new ArrayList<>();
        values.addAll(twice(new ObjectFaker<>(Member.class, config)));
        values.addAll(twice(new ObjectFaker<>(Member.class, config).ruleFor("nickname", () -> "X")));
        values.addAll(twice(new ObjectFaker<>(Member.class, config).ignore("nickname")));
        values.addAll(twice(new ObjectFaker<>(Member.class, config).ruleFor("nickname", dependent)));
        values.addAll(twice(new ObjectFaker<>(Member.class, config).ruleFor("address.city", () -> "C")));

        assertEquals(
                List.of(
                        // No rule: each member draws from its named stream.
                        PLAIN_FIRST,
                        PLAIN_SECOND,
                        // A root rule: every member moves.
                        "fVRdTDHcuejsrNmYtVmF|X|61|ewhite@example.com|2490 Pine St/Fairfield/-2144308780",
                        "eDULnViQLH|X|52|abigail_adams@example.org|8533 Grove Mnr/Cape Coral/-1634390306",
                        // A root ignore: every member moves, as with a root rule.
                        "fVRdTDHcuejsrNmYtVmF|null|61|ewhite@example.com|2490 Pine St/Fairfield/-2144308780",
                        "eDULnViQLH|null|52|abigail_adams@example.org|8533 Grove Mnr/Cape Coral/-1634390306",
                        // A dependent rule: only its member changes.
                        "rjyXYwkzybng|X|51|angelwalker@example.net|4315 Brookside Cres/Indianapolis/766875267",
                        "oAERm|X|72|adams.scarlett@example.com|8731 Forest Pike/Orange/-1958311597",
                        // A nested rule: only its member changes.
                        "rjyXYwkzybng|aQvyppOmBjDTPRIitB|51|angelwalker@example.net|4315 Brookside Cres/C/766875267",
                        "oAERm|sDANx|72|adams.scarlett@example.com|8731 Forest Pike/C/-1958311597"),
                values);
    }

    /** The first two members that seed 42 gives with no rule. */
    static final String PLAIN_FIRST =
            "rjyXYwkzybng|aQvyppOmBjDTPRIitB|51|angelwalker@example.net|4315 Brookside Cres/Indianapolis/766875267";

    static final String PLAIN_SECOND = "oAERm|sDANx|72|adams.scarlett@example.com|8731 Forest Pike/Orange/-1958311597";

    /** The report of a {@link Shipment} for seed 42, as 3.1 orders it: each object after its members. */
    static final List<String> SHIPMENT_REPORT = List.of(
            "Shipment OBJECT",
            "Shipment.id SEMANTIC",
            "Shipment.lines[].quantity BUILT_IN",
            "Shipment.lines[].sku BUILT_IN",
            "Shipment.lines[] OBJECT",
            "Shipment.lines[].quantity BUILT_IN",
            "Shipment.lines[].sku BUILT_IN",
            "Shipment.lines[] OBJECT",
            "Shipment.lines COLLECTION",
            "Shipment.member.address.city SEMANTIC",
            "Shipment.member.address.street SEMANTIC",
            "Shipment.member.address.zip BUILT_IN",
            "Shipment.member.address.street COHERENCE",
            "Shipment.member.address.city COHERENCE",
            "Shipment.member.address OBJECT",
            "Shipment.member.age SEMANTIC",
            "Shipment.member.email SEMANTIC",
            "Shipment.member.name BUILT_IN",
            "Shipment.member.nickname BUILT_IN",
            "Shipment.member OBJECT",
            "Shipment.origin.x BUILT_IN",
            "Shipment.origin.y BUILT_IN",
            "Shipment.origin OBJECT");

    /** The report of one seeded {@link Shipment}, as {@code path step} lines. */
    static List<String> shipmentReport(GeneratorConfig.Builder builder) {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig config = builder.collectionSize(2, 2)
                .generationReportListener(entries::add)
                .build();
        new ObjectGenerator<>(Shipment.class, config).generate();
        return entries.stream().map(entry -> entry.path() + " " + entry.step()).toList();
    }

    @Test
    @DisplayName("a root rule and a root ignore drop the recipe from the report, other rules keep it")
    void rootRulesDropTheRecipe() {
        Optional<String> v1 = Optional.of("krandom-recipe/v1");
        Function<Member, String> dependent = member -> "X";

        assertEquals(List.of(v1), recipeFormats(config -> new ObjectFaker<>(Member.class, config)));
        assertEquals(
                List.of(Optional.empty()),
                recipeFormats(config -> new ObjectFaker<>(Member.class, config).ruleFor("nickname", () -> "X")));
        assertEquals(
                List.of(Optional.empty()),
                recipeFormats(config -> new ObjectFaker<>(Member.class, config).ignore("nickname")));
        assertEquals(
                List.of(v1),
                recipeFormats(config -> new ObjectFaker<>(Member.class, config).ruleFor("nickname", dependent)));
        assertEquals(
                List.of(v1),
                recipeFormats(config -> new ObjectFaker<>(Member.class, config).ruleFor("address.city", () -> "C")));
    }

    @Test
    @DisplayName("the report lists a nested object, an element and a record after their members")
    void reportListsNestedObjectsAfterTheirMembers() {
        assertEquals(SHIPMENT_REPORT, shipmentReport(seeded()));
    }
}
