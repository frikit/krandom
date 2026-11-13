/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.frikit.krandom.generator.ContextualGenerator;
import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.report.GenerationDecision;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import io.github.frikit.krandom.generator.report.GenerationStep;
import io.github.frikit.krandom.generator.report.SeedSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

@DisplayName("generation report")
class GenerationReportTest {

    // ── What an entry says ───────────────────────────────────────────────────

    @Test
    @DisplayName("each generated path is reported with its step, generator, settings, policies and seed source")
    void entriesDescribeEachPath() {
        List<GenerationReportEntry> entries = generate(Order.class, listening(42L));

        GenerationReportEntry email = entry(entries, "Order.customer.email");
        assertEquals(GenerationStep.SEMANTIC, email.step());
        assertEquals("email", email.generator());
        assertEquals(SeedSource.DERIVED_STREAM, email.seedSource());
        assertEquals(Optional.of("krandom-recipe/v1"), email.recipeFormat());
        assertEquals(List.of("object.semantic-mode", "email.domain-policy", "network.safety-policy"), email.settings());
        assertEquals("TEST_SAFE_RESERVED", email.safetyPolicies().get("network.safety-policy"));
        assertEquals("TEST_SAFE_RESERVED_DOMAINS", email.safetyPolicies().get("email.domain-policy"));
        assertEquals(Optional.empty(), email.matchedRule());
        assertEquals(List.of(), email.decisions());
        GenerationReportEntry note = entry(entries, "Order.note");
        assertEquals(GenerationStep.BUILT_IN, note.step());
        assertEquals("java.lang.String", note.generator());
        assertEquals(List.of("string.min", "string.max", "charset"), note.settings());
        assertEquals(GenerationStep.COLLECTION, entry(entries, "Order.lines").step());
        assertEquals(
                List.of("collection.min", "collection.max"),
                entry(entries, "Order.lines").settings());
        assertEquals(GenerationStep.OBJECT, entry(entries, "Order.lines[]").step());
        assertEquals(Line.class.getName(), entry(entries, "Order.lines[]").generator());
        assertEquals(GenerationStep.ENUM, entry(entries, "Order.phase").step());
        assertEquals(GenerationStep.CONSTRAINT, entry(entries, "Order.code").step());
        assertEquals(List.of("Size"), entry(entries, "Order.code").constraints());
        assertEquals(GenerationStep.OBJECT, entry(entries, "Order").step());
        assertEquals(Order.class.getName(), entry(entries, "Order").generator());
    }

    @Test
    @DisplayName("semantic providers name the safety policies that bind them")
    void semanticProvidersNameTheirPolicies() {
        List<GenerationReportEntry> entries = generate(Contact.class, listening(7L));

        assertEquals(
                List.of("email.domain-policy", "network.safety-policy"),
                List.copyOf(
                        entry(entries, "Contact.companyEmail").safetyPolicies().keySet()));
        assertEquals(
                List.of("network.safety-policy"),
                List.copyOf(entry(entries, "Contact.url").safetyPolicies().keySet()));
        assertEquals(
                List.of("network.safety-policy"),
                List.copyOf(
                        entry(entries, "Contact.companyUrl").safetyPolicies().keySet()));
        assertEquals(
                List.of("network.safety-policy"),
                List.copyOf(entry(entries, "Contact.domain").safetyPolicies().keySet()));
        assertEquals(
                Map.of("phone-number.safety-policy", "TEST_SAFE_WHERE_AVAILABLE"),
                entry(entries, "Contact.phone").safetyPolicies());
        assertEquals(Map.of(), entry(entries, "Contact.firstName").safetyPolicies());
        GenerationReportEntry business = entry(entries, "Contact.businessEmail");
        assertEquals(GenerationStep.CONSTRAINT, business.step());
        assertEquals(List.of("Email"), business.constraints());
        assertEquals(List.of(GenerationDecision.SEMANTIC_DEFERRED), business.decisions());
        assertTrue(business.safetyPolicies().containsKey("email.domain-policy"));
    }

    @Test
    @DisplayName("each kind of override is named as the matched rule, with ObjectFaker rules by registration")
    void overridesAreNamedAsMatchedRules() {
        Predicate<Field> named = field -> field.getName().equals("note");
        Predicate<Field> fixed = field -> field.getName().equals("tag");
        List<GenerationReportEntry> entries = generate(
                Overridden.class,
                listening(3L)
                        .objectOverride(Overridden.class, "label", () -> "plain")
                        .objectOverride(Overridden.class, "code", (ContextualGenerator<String>) context -> "context")
                        .objectOverride(fixed, () -> "predicate")
                        .objectOverride(named, (ContextualGenerator<String>) context -> "context predicate")
                        .objectOverride(Line.class, () -> new Line("sku", 1))
                        .objectOverride(Status.class, (ContextualGenerator<Status>) context -> Status.OPEN));

        assertRule(entry(entries, "Overridden.label"), FixtureRuleKind.FIELD_OVERRIDE, "label", -1);
        assertRule(entry(entries, "Overridden.code"), FixtureRuleKind.CONTEXT_FIELD_OVERRIDE, "code", -1);
        assertRule(entry(entries, "Overridden.tag"), FixtureRuleKind.PREDICATE_OVERRIDE, "tag", -1);
        assertRule(entry(entries, "Overridden.note"), FixtureRuleKind.CONTEXT_PREDICATE_OVERRIDE, "note", -1);
        assertRule(entry(entries, "Overridden.line"), FixtureRuleKind.TYPE_OVERRIDE, "line", -1);
        assertRule(entry(entries, "Overridden.status"), FixtureRuleKind.CONTEXT_TYPE_OVERRIDE, "status", -1);
        assertEquals("override", entry(entries, "Overridden.label").generator());
        assertEquals(
                SeedSource.EXPLICIT_SEED, entry(entries, "Overridden.label").seedSource());
        assertEquals(Optional.empty(), entry(entries, "Overridden.label").recipeFormat());
    }

    @Test
    @DisplayName("ObjectFaker rules are reported by kind, path and registration, where and when they run")
    void fakerRulesAreReported() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        new ObjectFaker<>(
                        Shop.class,
                        listening(5L).generationReportListener(entries::add).build())
                .ruleFor("title", () -> "First")
                .ruleFor("title", () -> "Main")
                .ruleForContext("motto", context -> "Always")
                .include("client")
                .ruleFor("client.name", () -> "Ada")
                .ruleForContext("client.email", context -> "ada@example.test")
                .ruleFor("client.city", shop -> "Paris")
                .ignore("client.street")
                .ignore("client.street")
                .include("client.name")
                .generate();

        assertRule(entry(entries, "Shop.title"), FixtureRuleKind.RULE, "title", 2);
        assertEquals("ObjectFaker", entry(entries, "Shop.title").generator());
        assertRule(entry(entries, "Shop.motto"), FixtureRuleKind.CONTEXT_RULE, "motto", 3);
        GenerationReportEntry name = last(entries, "Shop.client.name");
        assertEquals(GenerationStep.RULE, name.step());
        assertRule(name, FixtureRuleKind.RULE, "client.name", 5);
        assertRule(last(entries, "Shop.client.email"), FixtureRuleKind.CONTEXT_RULE, "client.email", 6);
        assertRule(last(entries, "Shop.client.city"), FixtureRuleKind.DEPENDENT_RULE, "client.city", 7);
        GenerationReportEntry street = last(entries, "Shop.client.street");
        assertEquals(GenerationStep.IGNORE, street.step());
        assertRule(street, FixtureRuleKind.IGNORE, "client.street", 8);
        assertEquals(SeedSource.EXPLICIT_SEED, street.seedSource());
    }

    @Test
    @DisplayName("fallback decisions are reported: depth, cycles, absence and lenient failures")
    void fallbackDecisionsAreReported() {
        List<GenerationReportEntry> cut = generate(Deep.class, listening(9L).objectMaxDepth(1));
        List<GenerationReportEntry> cycle = generate(Deep.class, listening(9L));
        List<GenerationReportEntry> absent =
                generate(Absent.class, listening(9L).objectNullProbability(1.0).objectOptionalEmptyProbability(1.0));
        List<GenerationReportEntry> lenient =
                generate(Broken.class, listening(9L).objectIgnoreErrors(true));

        GenerationReportEntry depth = entry(cut, "Deep.middle.leaf");
        assertEquals(GenerationStep.DEFAULT, depth.step());
        assertEquals(List.of(GenerationDecision.DEPTH_CUT), depth.decisions());
        assertEquals(List.of("object.max-depth"), depth.settings());
        assertEquals(
                List.of(GenerationDecision.CYCLE_CUT),
                entry(cycle, "Deep.self.self").decisions());
        assertEquals(
                List.of(GenerationDecision.NULL_PROBABILITY),
                entry(absent, "Absent.text").decisions());
        assertEquals(GenerationStep.NULL, entry(absent, "Absent.text").step());
        assertEquals(
                List.of(GenerationDecision.OPTIONAL_EMPTY),
                entry(absent, "Absent.maybe").decisions());
        assertEquals(GenerationStep.NULL, entry(absent, "Absent.always").step());
        assertEquals(
                List.of(GenerationDecision.UNSUPPORTED_TYPE),
                entry(lenient, "Broken.task").decisions());
        assertEquals(
                List.of(GenerationDecision.CONSTRAINT_CONFLICT),
                entry(lenient, "Broken.code").decisions());
        assertEquals(
                List.of(GenerationDecision.UNSUPPORTED_TYPE),
                entry(lenient, "Broken.raw").decisions());
    }

    @Test
    @DisplayName("annotations, root factories and dated built-ins are reported")
    void annotationsFactoriesAndDatesAreReported() {
        List<GenerationReportEntry> annotated = generate(Annotated.class, listening(11L));
        List<GenerationReportEntry> dated = generate(
                Dated.class, listening(11L).objectDateRange(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 12, 31)));
        List<GenerationReportEntry> factory =
                generate(Line.class, listening(11L).objectOverride(Line.class, () -> new Line("a", 1)));
        List<GenerationReportEntry> contextFactory =
                generate(Line.class, listening(11L).objectOverride(Line.class, (ContextualGenerator<Line>)
                        context -> new Line("a", 1)));

        assertEquals(
                GenerationStep.RANDOMIZER, entry(annotated, "Annotated.fixed").step());
        assertEquals(Fixed.class.getName(), entry(annotated, "Annotated.fixed").generator());
        assertEquals(GenerationStep.FAKE, entry(annotated, "Annotated.city").step());
        assertEquals("city", entry(annotated, "Annotated.city").generator());
        assertEquals(
                GenerationStep.FAKE_RANGE, entry(annotated, "Annotated.count").step());
        assertEquals(
                List.of("object.date-min", "object.date-max"),
                entry(dated, "Dated.day").settings());
        assertEquals(
                List.of("object.date-min", "object.date-max"),
                entry(dated, "Dated.legacy").settings());
        assertEquals(
                List.of("object.date-min", "object.date-max"),
                entry(dated, "Dated.calendar").settings());
        assertEquals(List.of(), entry(dated, "Dated.count").settings());
        assertRule(entry(factory, "Line"), FixtureRuleKind.TYPE_OVERRIDE, "Line", -1);
        assertRule(entry(contextFactory, "Line"), FixtureRuleKind.CONTEXT_TYPE_OVERRIDE, "Line", -1);
    }

    @Test
    @DisplayName("constraints are named as generation reads them, from a getter or an initialized field too")
    void effectiveConstraintsAreNamed() {
        List<GenerationReportEntry> constructed = generate(Coded.class, listening(41L));
        List<GenerationReportEntry> getter = generate(GetterCoded.class, listening(41L));

        assertEquals(List.of("Size"), entry(constructed, "Coded.code").constraints());
        assertEquals(GenerationStep.CONSTRAINT, entry(constructed, "Coded.code").step());
        assertEquals(List.of("Email"), entry(constructed, "Coded.contact").constraints());
        assertTrue(entry(constructed, "Coded.contact").safetyPolicies().containsKey("email.domain-policy"));
        assertEquals(List.of("Size"), entry(getter, "GetterCoded.code").constraints());
    }

    @Test
    @DisplayName("failures that objectIgnoreErrors(true) passes over during generation are reported")
    void passedOverFailuresAreReported() {
        List<GenerationReportEntry> entries =
                generate(Failing.class, listening(43L).objectIgnoreErrors(true));
        List<GenerationReportEntry> unique = new ArrayList<>();
        new ObjectGenerator<>(
                        Flagged.class,
                        listening(43L)
                                .objectUniqueField("flag")
                                .objectIgnoreErrors(true)
                                .generationReportListener(unique::add)
                                .build())
                .generateList(3);

        assertEquals(
                List.of(GenerationDecision.CONSTRAINT_CONFLICT),
                entry(entries, "Failing.contact").decisions());
        assertEquals(GenerationStep.DEFAULT, entry(entries, "Failing.contact").step());
        assertEquals(
                List.of(GenerationDecision.CONSTRAINT_CONFLICT),
                entry(entries, "Failing.flags").decisions());
        assertEquals(
                List.of(GenerationDecision.FAILURE_FALLBACK),
                entry(entries, "Failing.exploding").decisions());
        assertEquals(
                List.of(GenerationDecision.FAILURE_FALLBACK),
                entry(entries, "Failing.fixed").decisions());
        assertEquals(
                List.of(GenerationDecision.FAILURE_FALLBACK),
                entry(entries, "Failing.nested").decisions());
        assertTrue(
                unique.stream().anyMatch(entry -> entry.decisions().contains(GenerationDecision.FAILURE_FALLBACK)),
                unique.toString());
    }

    @Test
    @DisplayName("a @Fake provider names the safety policies of the semantic provider it uses")
    void fakeProvidersNameTheirPolicies() {
        List<GenerationReportEntry> entries = generate(Annotated.class, listening(47L));

        assertEquals(
                List.of("email.domain-policy", "network.safety-policy"),
                List.copyOf(entry(entries, "Annotated.mail").safetyPolicies().keySet()));
        assertEquals(
                List.of("phone-number.safety-policy"),
                List.copyOf(entry(entries, "Annotated.number").safetyPolicies().keySet()));
        assertEquals(
                List.of("network.safety-policy"),
                List.copyOf(entry(entries, "Annotated.link").safetyPolicies().keySet()));
        assertEquals(Map.of(), entry(entries, "Annotated.city").safetyPolicies());
    }

    @Test
    @DisplayName("without a listener a root builds no report metadata, such as the recipe")
    void rootBuildsNoMetadataWithoutAListener() {
        CountingClock clock = new CountingClock();

        new ObjectGenerator<>(Signal.class, listening(59L).clock(clock).build()).generate();

        assertEquals(0, clock.reads, "building a recipe reads the clock");
    }

    @Test
    @DisplayName("a repeated @Pattern is named, and a root factory's passed-over failure is reported")
    void repeatedPatternsAndFailedFactoriesAreReported() {
        List<GenerationReportEntry> patterned = generate(Patterned.class, listening(61L));
        List<GenerationReportEntry> failed =
                generate(Line.class, listening(61L).objectIgnoreErrors(true).objectOverride(Line.class, () -> null));

        assertEquals(List.of("Pattern"), entry(patterned, "Patterned.code").constraints());
        GenerationReportEntry root = entry(failed, "Line");
        assertEquals(GenerationStep.DEFAULT, root.step());
        assertEquals(List.of(GenerationDecision.FAILURE_FALLBACK), root.decisions());
        assertRule(root, FixtureRuleKind.TYPE_OVERRIDE, "Line", -1);
    }

    @Test
    @DisplayName("a value that cannot be assigned is reported after the entry that made it, as the value kept")
    void failedAssignmentIsReported() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig config = listening(67L)
                .objectIgnoreErrors(true)
                .objectOverride(Counted.class, "count", () -> null)
                .generationReportListener(entries::add)
                .build();

        Counted generated = new ObjectGenerator<>(Counted.class, config).generate();

        assertEquals(7, generated.count);
        assertEquals(GenerationStep.OVERRIDE, entry(entries, "Counted.count").step());
        GenerationReportEntry outcome = last(entries, "Counted.count");
        assertEquals(GenerationStep.KEPT, outcome.step());
        assertEquals("int", outcome.generator());
        assertEquals(List.of(GenerationDecision.FAILURE_FALLBACK), outcome.decisions());

        entries.clear();
        Counted existing = new Counted();
        existing.count = 5;
        new ObjectFaker<>(Counted.class, config).populate(existing);

        assertEquals(5, existing.count);
        assertEquals(GenerationStep.KEPT, last(entries, "Counted.count").step());
    }

    @Test
    @DisplayName("the email domain policy is named only where it chooses the domain of the address")
    void emailDomainPolicyIsNamedOnlyWhereItChoosesTheDomain() {
        List<GenerationReportEntry> entries =
                generate(Mailboxes.class, listening(71L).objectIgnoreErrors(true));

        assertEquals(List.of("Email"), entry(entries, "Mailboxes.fixed").constraints());
        assertEquals(
                GenerationStep.CONSTRAINT, entry(entries, "Mailboxes.fixed").step());
        assertEquals(Map.of(), entry(entries, "Mailboxes.fixed").safetyPolicies());
        assertEquals(Map.of(), entry(entries, "Mailboxes.patterned").safetyPolicies());
        assertEquals(
                Map.of("email.domain-policy", "TEST_SAFE_RESERVED_DOMAINS"),
                entry(entries, "Mailboxes.filtered").safetyPolicies());
        assertEquals(Map.of(), entry(entries, "Mailboxes.digits").safetyPolicies());
        assertEquals(
                GenerationStep.CONSTRAINT, entry(entries, "Mailboxes.boxes").step());
        assertEquals(Map.of(), entry(entries, "Mailboxes.boxes").safetyPolicies());
        GenerationReportEntry tiny = entry(entries, "Mailboxes.tiny");
        assertEquals(GenerationStep.DEFAULT, tiny.step());
        assertEquals(List.of(GenerationDecision.CONSTRAINT_CONFLICT), tiny.decisions());
        assertEquals(Map.of(), tiny.safetyPolicies());
    }

    @Test
    @DisplayName("an array element that cannot be stored is reported after its entry, at its default")
    void rejectedArrayElementIsReportedAtItsDefault() {
        List<GenerationReportEntry> entries = generate(
                Readings.class,
                listening(73L).collectionSize(1, 1).objectIgnoreErrors(true).objectOverride(int.class, () -> null));

        GenerationReportEntry readings = entry(entries, "Readings.readings");
        assertEquals(GenerationStep.COLLECTION, readings.step());
        assertEquals("[I", readings.generator());
        assertEquals(List.of(), readings.decisions());
        assertEquals(
                GenerationStep.OVERRIDE, entry(entries, "Readings.readings[]").step());
        GenerationReportEntry element = last(entries, "Readings.readings[]");
        assertEquals(GenerationStep.DEFAULT, element.step());
        assertEquals("int", element.generator());
        assertEquals(List.of(GenerationDecision.FAILURE_FALLBACK), element.decisions());
        assertTrue(entries.indexOf(element) < entries.indexOf(readings), entries.toString());

        List<GenerationReportEntry> strict = new ArrayList<>();
        GeneratorConfig throwing = listening(73L)
                .collectionSize(1, 1)
                .objectOverride(int.class, () -> null)
                .generationReportListener(strict::add)
                .build();
        assertThrows(
                io.github.frikit.krandom.generator.object.exception.ObjectGenerationException.class,
                () -> new ObjectGenerator<>(Readings.class, throwing).generate());
        assertTrue(strict.stream().noneMatch(entry -> entry.step() == GenerationStep.DEFAULT), strict.toString());
    }

    @Test
    @DisplayName("a parent that an ObjectFaker rule builds is reported at its path in the fixture")
    void materializedParentIsReportedAtItsFixturePath() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        Household household = fakeHousehold(listening(83L).generationReportListener(entries::add));

        assertEquals("Rex", household.pet.name);
        assertTrue(entries.stream().allMatch(entry -> entry.path().startsWith("Household")), entries.toString());
        assertEquals(GenerationStep.NULL, entry(entries, "Household.pet").step());
        assertEquals(GenerationStep.OBJECT, last(entries, "Household.pet").step());
        assertRule(entry(entries, "Household.pet.age"), FixtureRuleKind.FIELD_OVERRIDE, "pet.age", -1);
        assertEquals(
                GenerationStep.BUILT_IN, entry(entries, "Household.pet.legs").step());
        assertEquals(GenerationStep.RULE, last(entries, "Household.pet.name").step());
        assertEquals(
                fakeHousehold(listening(83L)).describe(),
                fakeHousehold(listening(83L).generationReportListener(entry -> {}))
                        .describe());
    }

    @Test
    @DisplayName("a rule whose values on a requested name run out is reported as a fallback to the default")
    void exhaustedRuleIsReportedAsAFallback() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        ObjectFaker<Household> faker = new ObjectFaker<>(
                        Household.class,
                        listening(89L)
                                .objectUniqueField("name")
                                .objectIgnoreErrors(true)
                                .generationReportListener(entries::add)
                                .build())
                .ruleFor("pet.name", () -> "Rex");

        faker.generate();
        GenerationReportEntry first = last(entries, "Household.pet.name");
        entries.clear();
        faker.generate();
        GenerationReportEntry repeated = last(entries, "Household.pet.name");

        assertEquals(GenerationStep.RULE, first.step());
        assertEquals(List.of(), first.decisions());
        assertEquals(GenerationStep.DEFAULT, repeated.step());
        assertEquals(List.of(GenerationDecision.FAILURE_FALLBACK), repeated.decisions());
        assertRule(repeated, FixtureRuleKind.RULE, "pet.name", 1);
    }

    @Test
    @DisplayName("a required member that objectMaxDepth leaves null is reported as an unmet constraint")
    void requiredMemberCutAtMaxDepthIsAnUnmetConstraint() {
        List<GenerationReportEntry> entries =
                generate(Anchored.class, listening(79L).objectMaxDepth(1).objectIgnoreErrors(true));

        GenerationReportEntry leaf = entry(entries, "Anchored.held.leaf");
        assertEquals(GenerationStep.DEFAULT, leaf.step());
        assertEquals(List.of(GenerationDecision.DEPTH_CUT, GenerationDecision.CONSTRAINT_CONFLICT), leaf.decisions());
        assertEquals(List.of("object.max-depth"), leaf.settings());
    }

    @Test
    @DisplayName("decisions are listed in the order they were taken, each once")
    void decisionsKeepTheOrderTheyWereTaken() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GenerationReporter reporter = reporter(entries);
        GenerationReporter.Trace trace = reporter.trace("Order.email");

        GenerationReporter.decision(trace, GenerationDecision.SEMANTIC_DEFERRED, "object.semantic-mode");
        GenerationReporter.failed(trace, GenerationDecision.CONSTRAINT_CONFLICT);
        GenerationReporter.decision(trace, GenerationDecision.SEMANTIC_DEFERRED, null);
        reporter.emit(trace);

        assertEquals(
                List.of(GenerationDecision.SEMANTIC_DEFERRED, GenerationDecision.CONSTRAINT_CONFLICT),
                entries.get(0).decisions());
    }

    @Test
    @DisplayName("a sealed root that picks an enum is reported as an enum")
    void sealedRootPickingAnEnumIsReported() {
        List<GenerationReportEntry> entries = generate(Signal.class, listening(53L));

        assertEquals(GenerationStep.ENUM, entry(entries, "Signal").step());
        assertEquals(Light.class.getName(), entry(entries, "Signal").generator());
    }

    @Test
    @DisplayName("the coherence pass reports what it moves and, in RELAXED mode, what it keeps")
    void coherenceMovesAndKeepsAreReported() {
        List<GenerationReportEntry> entries = generate(Profile.class, listening(13L));

        assertTrue(
                entries.stream()
                        .anyMatch(entry -> entry.path().equals("Profile.email")
                                && entry.decisions().equals(List.of(GenerationDecision.COHERENCE_MOVE))),
                entries.toString());
        assertTrue(
                entries.stream()
                        .anyMatch(entry -> entry.path().equals("Profile.fullName")
                                && entry.decisions().equals(List.of(GenerationDecision.CONSTRAINT_KEPT))),
                entries.toString());
    }

    @Test
    @DisplayName("a reporting slot reports a change once per move and a kept member once")
    void reportingSlotReportsChangesOnly() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GenerationReporter reporter = reporter(entries);
        Object[] values = {"same"};
        SemanticCoherenceAdjuster.ReportingSlot slot =
                new SemanticCoherenceAdjuster.ReportingSlot(new ArraySlot(values), reporter, "Root.value");

        slot.setValue("same");
        slot.setValue("other");
        slot.kept();
        slot.kept();

        assertEquals(2, entries.size(), entries.toString());
        assertEquals(List.of(GenerationDecision.COHERENCE_MOVE), entries.get(0).decisions());
        assertEquals(List.of(GenerationDecision.CONSTRAINT_KEPT), entries.get(1).decisions());
        assertEquals("other", values[0]);
    }

    @Test
    @DisplayName("the seed source is the recipe's for a restored configuration, and none without a seed")
    void seedSourcesAreReported() {
        GeneratorConfig seeded = GeneratorConfig.builder().seed(17L).build();
        GeneratorConfig restored = seeded.getGenerationRecipe().orElseThrow().toGeneratorConfig();
        List<GenerationReportEntry> replay = new ArrayList<>();
        List<GenerationReportEntry> unseeded = new ArrayList<>();

        new ObjectGenerator<>(
                        Line.class,
                        restored.toBuilder()
                                .generationReportListener(replay::add)
                                .build())
                .generate();
        new ObjectGenerator<>(
                        Line.class,
                        GeneratorConfig.builder()
                                .generationReportListener(unseeded::add)
                                .build())
                .generate();

        assertTrue(restored.isRestoredFromRecipe());
        assertFalse(seeded.isRestoredFromRecipe());
        assertFalse(restored.toBuilder().seed(3L).build().isRestoredFromRecipe());
        assertFalse(restored.toBuilder().seed("text").build().isRestoredFromRecipe());
        assertEquals(SeedSource.RECIPE, entry(replay, "Line.sku").seedSource());
        assertEquals(SeedSource.UNSEEDED, entry(unseeded, "Line.sku").seedSource());
    }

    // ── What the report never does ───────────────────────────────────────────

    @Test
    @DisplayName("seeded output is the same with and without a listener")
    void seededOutputIsUnchanged() {
        assertSameWithAndWithoutListener(config -> new ObjectGenerator<>(Order.class, config).generateList(30));
        assertSameWithAndWithoutListener(config -> new ObjectGenerator<>(Contact.class, config).generateList(30));
        assertSameWithAndWithoutListener(config -> new ObjectGenerator<>(Profile.class, config).generateList(30));
        assertSameWithAndWithoutListener(config -> new ObjectGenerator<>(Graph.class, config).generateList(30));
        assertSameWithAndWithoutListener(config -> new ObjectFaker<>(Shop.class, config)
                        .ruleFor("title", () -> "Main")
                        .ruleForContext("motto", context -> context.getFieldName())
                        .ruleFor("client.city", shop -> shop.title.toUpperCase(java.util.Locale.ROOT))
                        .ignore("client.street")
                        .generateList(30)
                        .stream()
                        .map(Shop::describe)
                        .toList());
    }

    @Test
    @DisplayName("no generated value appears in an entry or its toString")
    void entriesHoldNoValue() {
        List<GenerationReportEntry> entries = new ArrayList<>();
        GeneratorConfig config =
                listening(23L).generationReportListener(entries::add).build();
        List<Object> generated = new ArrayList<>();
        generated.addAll(new ObjectGenerator<>(Order.class, config).generateList(10));
        generated.addAll(new ObjectGenerator<>(Contact.class, config).generateList(10));
        generated.addAll(new ObjectGenerator<>(Profile.class, config).generateList(10));
        generated.addAll(new ObjectGenerator<>(Graph.class, config).generateList(10));

        List<String> values = new ArrayList<>();
        for (Object object : generated) {
            collectStrings(object, values);
        }
        assertFalse(values.isEmpty());
        for (GenerationReportEntry entry : entries) {
            String text = entry.toString();
            for (String value : values) {
                assertFalse(value.length() >= 4 && text.contains(value), "an entry holds a generated value");
            }
        }
    }

    @Test
    @DisplayName("a listener that throws is logged with its class names only, and generation goes on")
    void throwingListenerIsLoggedAndIgnored() {
        Logger logger = (Logger) LoggerFactory.getLogger(GenerationReporter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            Order order = new ObjectGenerator<>(
                            Order.class,
                            listening(29L)
                                    .generationReportListener(entry -> {
                                        throw new IllegalStateException("SECRET-" + entry.path());
                                    })
                                    .build())
                    .generate();

            assertEquals(new ObjectGenerator<>(Order.class, listening(29L).build()).generate(), order);
            assertFalse(appender.list.isEmpty());
            String message = appender.list.get(0).getFormattedMessage();
            assertTrue(message.contains("java.lang.IllegalStateException"), message);
            assertFalse(message.contains("SECRET"), message);
        } finally {
            logger.detachAppender(appender);
        }
    }

    @Test
    @DisplayName("without a listener nothing is reported or built")
    void nothingIsBuiltWithoutAListener() {
        com.sun.management.ThreadMXBean threads = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        AtomicInteger count = new AtomicInteger();
        ObjectGenerator<Order> quiet =
                new ObjectGenerator<>(Order.class, listening(31L).build());
        ObjectGenerator<Order> reported = new ObjectGenerator<>(
                Order.class,
                listening(31L)
                        .generationReportListener(entry -> count.incrementAndGet())
                        .build());
        for (int i = 0; i < 300; i++) {
            quiet.generate();
            reported.generate();
        }
        count.set(0);

        long start = threads.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < 300; i++) {
            quiet.generate();
        }
        long quietBytes = threads.getCurrentThreadAllocatedBytes() - start;
        start = threads.getCurrentThreadAllocatedBytes();
        for (int i = 0; i < 300; i++) {
            reported.generate();
        }
        long reportedBytes = threads.getCurrentThreadAllocatedBytes() - start;

        assertNull(
                GenerationReporter.of(ObjectGeneratorConfig.from(listening(31L).build()), true));
        assertTrue(count.get() > 0);
        // Each entry, with the trace, lists and map it is built from, takes well over 64 bytes, and without a
        // listener none is built, so the difference covers every entry the listening run reported.
        assertTrue(
                reportedBytes - quietBytes >= 64L * count.get(),
                "without a listener: " + quietBytes + " bytes; with one: " + reportedBytes + " bytes for " + count.get()
                        + " entries");
    }

    @Test
    @DisplayName("an entry rejects a blank path or generator and keeps unmodifiable copies")
    void entryValidatesItsFields() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new GenerationReportEntry(
                        " ",
                        SeedSource.UNSEEDED,
                        Optional.empty(),
                        List.of(),
                        GenerationStep.ENUM,
                        "g",
                        Optional.empty(),
                        List.of(),
                        Map.of(),
                        List.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> new GenerationReportEntry(
                        "p",
                        SeedSource.UNSEEDED,
                        Optional.empty(),
                        List.of(),
                        GenerationStep.ENUM,
                        " ",
                        Optional.empty(),
                        List.of(),
                        Map.of(),
                        List.of()));
        GenerationReportEntry entry = new GenerationReportEntry(
                "p",
                SeedSource.UNSEEDED,
                Optional.empty(),
                new ArrayList<>(List.of("s")),
                GenerationStep.ENUM,
                "g",
                Optional.empty(),
                new ArrayList<>(),
                new java.util.HashMap<>(Map.of("k", "v")),
                new ArrayList<>());
        assertThrows(UnsupportedOperationException.class, () -> entry.settings().add("x"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> entry.safetyPolicies().put("x", "y"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static GeneratorConfig.Builder listening(long seed) {
        return GeneratorConfig.builder().seed(seed);
    }

    // Every nullable member of the household is null, so the rule on its pet builds the pet.
    private static Household fakeHousehold(GeneratorConfig.Builder config) {
        return new ObjectFaker<>(
                        Household.class,
                        config.objectNullProbability(1.0)
                                .objectOverride(Pet.class, "age", () -> 3)
                                .build())
                .ruleFor("pet.name", () -> "Rex")
                .generate();
    }

    private static List<GenerationReportEntry> generate(Class<?> type, GeneratorConfig.Builder config) {
        List<GenerationReportEntry> entries = new ArrayList<>();
        new ObjectGenerator<>(
                        type, config.generationReportListener(entries::add).build())
                .generate();
        return entries;
    }

    private static GenerationReporter reporter(List<GenerationReportEntry> entries) {
        return java.util.Objects.requireNonNull(GenerationReporter.of(
                ObjectGeneratorConfig.from(GeneratorConfig.builder()
                        .generationReportListener(entries::add)
                        .build()),
                false));
    }

    private static <T> void assertSameWithAndWithoutListener(Function<GeneratorConfig, T> generation) {
        AtomicInteger count = new AtomicInteger();
        T plain = generation.apply(listening(37L).build());
        T reported = generation.apply(listening(37L)
                .generationReportListener(entry -> count.incrementAndGet())
                .build());
        assertEquals(plain, reported);
        assertTrue(count.get() > 0);
    }

    private static GenerationReportEntry entry(List<GenerationReportEntry> entries, String path) {
        return entries.stream()
                .filter(entry -> entry.path().equals(path))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no entry for " + path + " in " + entries));
    }

    private static GenerationReportEntry last(List<GenerationReportEntry> entries, String path) {
        GenerationReportEntry found = null;
        for (GenerationReportEntry entry : entries) {
            if (entry.path().equals(path)) {
                found = entry;
            }
        }
        if (found == null) {
            throw new AssertionError("no entry for " + path + " in " + entries);
        }
        return found;
    }

    private static void assertRule(GenerationReportEntry entry, FixtureRuleKind kind, String path, int registration) {
        FixtureRule rule = entry.matchedRule().orElseThrow(() -> new AssertionError("no rule in " + entry));
        assertEquals(kind, rule.kind(), entry.toString());
        assertEquals(path, rule.path(), entry.toString());
        assertEquals(registration, rule.registration(), entry.toString());
    }

    private static void collectStrings(Object value, List<String> strings) {
        if (value instanceof String text) {
            strings.add(text);
        } else if (value instanceof Collection<?> collection) {
            collection.forEach(element -> collectStrings(element, strings));
        } else if (value instanceof Map<?, ?> map) {
            map.forEach((key, element) -> {
                collectStrings(key, strings);
                collectStrings(element, strings);
            });
        } else if (value instanceof Optional<?> optional) {
            optional.ifPresent(element -> collectStrings(element, strings));
        } else if (value != null && value.getClass().isRecord()) {
            for (RecordComponent component : value.getClass().getRecordComponents()) {
                try {
                    component.getAccessor().setAccessible(true);
                    collectStrings(component.getAccessor().invoke(value), strings);
                } catch (ReflectiveOperationException e) {
                    throw new AssertionError(e);
                }
            }
        }
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    enum Status {
        OPEN,
        CLOSED
    }

    record Customer(String name, String email) {}

    record Line(String sku, int quantity) {}

    record Order(
            Customer customer,
            String note,
            List<Line> lines,
            Status phase,
            @Size(min = 3, max = 3) String code) {}

    record Contact(
            String firstName,
            String email,
            String companyEmail,
            String url,
            String companyUrl,
            String domain,
            String phone,
            @Email String businessEmail) {}

    record Overridden(String label, String code, String tag, String note, Line line, Status status) {}

    static final class Client {
        String name;
        String email;
        String city;
        String street;
    }

    static final class Shop {
        String title;
        String motto;
        Client client;

        String describe() {
            return title + "|" + motto + "|" + client.name + "|" + client.email + "|" + client.city + "|"
                    + client.street;
        }
    }

    record Leaf(String value) {}

    record Middle(Leaf leaf) {}

    static final class Deep {
        Middle middle;
        Deep self;
    }

    static final class Absent {
        String text;
        Optional<String> maybe;

        @Null String always;
    }

    static final class Broken {
        Runnable task;

        @Size(min = 3, max = 1) String code;

        @SuppressWarnings("rawtypes")
        List raw;
    }

    static final class Fixed implements Generator<String> {
        @Override
        public String generate() {
            return "fixed";
        }
    }

    record Annotated(
            @Randomizer(Fixed.class) String fixed,
            @Fake("city") String city,
            @FakeRange(min = 1, max = 5) int count,
            @Fake("email") String mail,
            @Fake("phone") String number,
            @Fake("url") String link) {}

    static final class Coded {
        @Size(min = 3, max = 3) private final String code;

        @Email private final String contact;

        Coded(String code, String contact) {
            this.code = code;
            this.contact = contact;
        }
    }

    static final class GetterCoded {
        String code;

        @Size(min = 2, max = 2) public String getCode() {
            return code;
        }
    }

    static final class ExplodingList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;

        ExplodingList() {
            throw new IllegalStateException("no list");
        }
    }

    static final class FixedList extends ArrayList<String> {
        private static final long serialVersionUID = 1L;

        FixedList(int capacity) {
            super(capacity);
        }
    }

    static final class ExplodingObject {
        String value;

        ExplodingObject() {
            throw new IllegalStateException("no object");
        }
    }

    static final class Failing {
        @Email @jakarta.validation.constraints.Pattern(regexp = "x") String contact;

        @Size(min = 3) java.util.Set<Boolean> flags;

        ExplodingList exploding;
        FixedList fixed;
        ExplodingObject nested;
    }

    record Flagged(boolean flag) {}

    static final class Counted {
        int count = 7;
    }

    static final class Mailboxes {
        @Email(regexp = "[a-z]{4}@corp\\.com") String fixed;

        @Email @jakarta.validation.constraints.Pattern(regexp = "[a-z]{4}@corp\\.com") String patterned;

        // The regex generator does not support a lookahead, so the policy's addresses are only filtered.
        @Email(regexp = "(?=.*@example\\.).*")
        String filtered;

        @jakarta.validation.constraints.Pattern(regexp = "[0-9]{4}") String digits;

        @jakarta.validation.constraints.Min(1) int boxes;

        @Email @Size(max = 3) String tiny;
    }

    static final class Readings {
        int[] readings;
    }

    static final class Held {
        @jakarta.validation.constraints.NotNull Leaf leaf;
    }

    static final class Household {
        Pet pet;

        String describe() {
            return pet.name + "|" + pet.age + "|" + pet.legs;
        }
    }

    static final class Pet {
        String name;
        int age;
        int legs;
    }

    static final class Anchored {
        Held held;
    }

    static final class Patterned {
        @jakarta.validation.constraints.Pattern(regexp = "[a-c]{3}") @jakarta.validation.constraints.Pattern(regexp = "a.*") String code;
    }

    /** A fixed clock that counts its reads. */
    static final class CountingClock extends java.time.Clock {

        private final java.time.Clock fixed =
                java.time.Clock.fixed(java.time.Instant.parse("2026-01-01T00:00:00Z"), java.time.ZoneOffset.UTC);
        int reads;

        @Override
        public java.time.ZoneId getZone() {
            return fixed.getZone();
        }

        @Override
        public java.time.Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public java.time.Instant instant() {
            reads++;
            return fixed.instant();
        }
    }

    sealed interface Signal permits Light {}

    enum Light implements Signal {
        RED,
        GREEN
    }

    record Dated(LocalDate day, Date legacy, Calendar calendar, int count) {}

    record Profile(
            String firstName,
            String lastName,
            String email,
            @Size(min = 2, max = 40) String fullName) {}

    record Graph(Map<String, Line> lines, Optional<Customer> customer, Line[] history, Status[] statuses) {
        @Override
        public boolean equals(Object other) {
            return other instanceof Graph graph
                    && lines.equals(graph.lines)
                    && customer.equals(graph.customer)
                    && java.util.Arrays.equals(history, graph.history)
                    && java.util.Arrays.equals(statuses, graph.statuses);
        }

        @Override
        public int hashCode() {
            return lines.hashCode();
        }
    }

    /** A coherence slot over one array element, for the reporting slot's own test. */
    private record ArraySlot(Object[] values) implements SemanticCoherenceAdjuster.Slot {

        @Override
        public Class<?> ownerType() {
            return Object.class;
        }

        @Override
        public String fieldName() {
            return "value";
        }

        @Override
        public Class<?> rawType() {
            return String.class;
        }

        @Override
        public Field annotatedElement() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue() {
            return values[0];
        }

        @Override
        public void setValue(Object value) {
            values[0] = value;
        }
    }
}
