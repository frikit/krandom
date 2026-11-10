/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.object;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.report.GenerationDecision;
import io.github.frikit.krandom.generator.report.GenerationReportEntry;
import io.github.frikit.krandom.generator.report.GenerationReportListener;
import io.github.frikit.krandom.generator.report.GenerationStep;
import io.github.frikit.krandom.generator.report.SeedSource;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds and delivers the value-free entries of a {@link GenerationReportListener}. Object generation
 * creates one only when a listener is configured, so without one it allocates nothing and records
 * nothing. It never receives a generated value and draws nothing from a random stream.
 */
final class GenerationReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerationReporter.class);

    private static final String RECIPE_FORMAT = GenerationRecipe.FORMAT + "/" + GenerationRecipe.RECIPE_VERSION;

    private final GenerationReportListener listener;
    private final GeneratorConfig config;
    private final SeedSource seedSource;
    private final Optional<String> recipeFormat;

    private GenerationReporter(GenerationReportListener listener, ObjectGeneratorConfig config, boolean namedStreams) {
        this.listener = listener;
        this.config = config.getGeneratorConfig();
        this.seedSource = seedSource(this.config, namedStreams);
        this.recipeFormat = config.hasPortableRecipe() ? Optional.of(RECIPE_FORMAT) : Optional.empty();
    }

    /**
     * The reporter for a configuration, or {@code null} when it has no listener.
     *
     * @param namedStreams whether the members draw from streams derived by name from the seed
     */
    static @Nullable GenerationReporter of(ObjectGeneratorConfig config, boolean namedStreams) {
        GenerationReportListener listener =
                config.getGeneratorConfig().getGenerationReportListener().orElse(null);
        return listener == null ? null : new GenerationReporter(listener, config, namedStreams);
    }

    private static SeedSource seedSource(GeneratorConfig config, boolean namedStreams) {
        if (config.getSeed().isEmpty()) {
            return SeedSource.UNSEEDED;
        }
        if (config.isRestoredFromRecipe()) {
            return SeedSource.RECIPE;
        }
        return namedStreams ? SeedSource.DERIVED_STREAM : SeedSource.EXPLICIT_SEED;
    }

    Trace trace(String path) {
        return new Trace(path);
    }

    // ── Hooks: each does nothing without a trace, and takes only values that already exist ──

    static void step(@Nullable Trace trace, GenerationStep step, Object generator) {
        if (trace != null) {
            trace.step(step, generator instanceof Class<?> type ? type.getName() : generator.toString());
        }
    }

    static void setting(@Nullable Trace trace, String setting) {
        if (trace != null) {
            trace.setting(setting);
        }
    }

    static void decision(@Nullable Trace trace, GenerationDecision decision, @Nullable String setting) {
        if (trace != null) {
            trace.decision(decision);
            if (setting != null) {
                trace.setting(setting);
            }
        }
    }

    /** A fallback that leaves the type's default, unless the failure policy throws first. */
    static void fallback(@Nullable Trace trace, Class<?> rawType, GenerationDecision decision) {
        if (trace != null) {
            trace.step(GenerationStep.DEFAULT, rawType.getName()).decision(decision);
        }
    }

    static void constraints(@Nullable Trace trace, @Nullable AnnotatedElement element) {
        if (trace != null) {
            trace.constraints(element);
        }
    }

    /** The override that supplies a member: an {@code ObjectFaker} rule, or the configuration's entry. */
    static void override(@Nullable Trace trace, FixtureRuleKind kind, Object generator) {
        if (trace != null) {
            FixtureRule rule;
            if (generator instanceof FixtureRuleGenerator<?> tagged) {
                rule = tagged.rule();
            } else if (generator instanceof FixtureRuleContextualGenerator<?> contextual) {
                rule = contextual.rule();
            } else {
                rule = FixtureRule.configured(kind, trace.relativePath());
            }
            trace.step(GenerationStep.OVERRIDE, rule.kind().isRegisteredOnObjectFaker() ? "ObjectFaker" : "override")
                    .rule(rule);
        }
    }

    static void semantic(@Nullable Trace trace, String semanticKey, GeneratorConfig config) {
        if (trace != null) {
            trace.step(GenerationStep.SEMANTIC, semanticKey).setting("object.semantic-mode");
            policies(trace, semanticKey, config);
        }
    }

    /** A {@code @Fake} provider, bound by the safety policies of the semantic provider it names. */
    static void fake(@Nullable Trace trace, String provider, SemanticFieldRegistry registry, GeneratorConfig config) {
        if (trace != null) {
            trace.step(GenerationStep.FAKE, provider);
            // A provider that @Fake can use is one of the registry's names, so it always has a key.
            policies(trace, Objects.requireNonNullElse(registry.semanticKeyForFieldName(provider), provider), config);
        }
    }

    /**
     * A failure that {@code objectIgnoreErrors(true)} passes over; when it throws instead, the trace is
     * never delivered.
     */
    static void failed(@Nullable Trace trace, GenerationDecision decision) {
        if (trace != null) {
            trace.decision(decision);
            trace.step = GenerationStep.DEFAULT;
        }
    }

    private static void policies(Trace trace, String semanticKey, GeneratorConfig config) {
        if (semanticKey.equals("email") || semanticKey.equals("companyemail")) {
            trace.safetyPolicy("email.domain-policy", config.getEmailDomainPolicy())
                    .safetyPolicy("network.safety-policy", config.getNetworkSafetyPolicy());
        } else if (semanticKey.equals("url") || semanticKey.equals("companyurl") || semanticKey.equals("domain")) {
            trace.safetyPolicy("network.safety-policy", config.getNetworkSafetyPolicy());
        } else if (semanticKey.equals("phone")) {
            trace.safetyPolicy("phone-number.safety-policy", config.getPhoneNumberSafetyPolicy());
        }
    }

    static void constraint(@Nullable Trace trace, Class<?> rawType, AnnotatedElement element, GeneratorConfig config) {
        if (trace != null) {
            trace.step(GenerationStep.CONSTRAINT, rawType.getName());
            if (BeanValidationSupport.constraintNames(element).contains("Email")) {
                trace.safetyPolicy("email.domain-policy", config.getEmailDomainPolicy());
            }
        }
    }

    static void builtIn(@Nullable Trace trace, Class<?> rawType, ObjectGeneratorConfig config) {
        if (trace != null) {
            trace.step(GenerationStep.BUILT_IN, rawType.getName());
            if (rawType == String.class) {
                trace.setting("string.min").setting("string.max").setting("charset");
            } else if (config.getDateMin() != null && isTemporal(rawType)) {
                trace.setting("object.date-min").setting("object.date-max");
            }
        }
    }

    static void collection(@Nullable Trace trace, Class<?> rawType) {
        if (trace != null) {
            trace.step(GenerationStep.COLLECTION, rawType.getName())
                    .setting("collection.min")
                    .setting("collection.max");
        }
    }

    private static boolean isTemporal(Class<?> type) {
        return java.time.temporal.Temporal.class.isAssignableFrom(type)
                || java.util.Date.class.isAssignableFrom(type)
                || java.util.Calendar.class.isAssignableFrom(type);
    }

    /** Reports the root object, which the engine builds, or a type override does. */
    void root(String path, Class<?> type, @Nullable FixtureRuleKind factory, @Nullable GenerationDecision decision) {
        Trace trace = new Trace(path);
        if (factory == null) {
            trace.step(type.isEnum() ? GenerationStep.ENUM : GenerationStep.OBJECT, type.getName());
        } else {
            trace.step(GenerationStep.OVERRIDE, "override").rule(FixtureRule.configured(factory, path));
        }
        if (decision != null) {
            failed(trace, decision);
        }
        emit(trace);
    }

    /** Reports a value that could not be assigned, which leaves the member at its default. */
    void failedAssignment(String path, Class<?> type) {
        Trace trace = new Trace(path);
        trace.step(GenerationStep.DEFAULT, type.getName()).decision(GenerationDecision.FAILURE_FALLBACK);
        emit(trace);
    }

    /** Reports an {@code ObjectFaker} rule or ignore that runs after generation, at its own path. */
    void applied(String path, FixtureRule rule) {
        Trace trace = new Trace(path);
        trace.step(rule.kind() == FixtureRuleKind.IGNORE ? GenerationStep.IGNORE : GenerationStep.RULE, "ObjectFaker")
                .rule(rule);
        emit(trace);
    }

    /** Reports a member that the coherence pass changed, or kept in {@code RELAXED} mode. */
    void coherence(String path, GenerationDecision decision) {
        Trace trace = new Trace(path);
        trace.step(GenerationStep.COHERENCE, "semantic coherence")
                .setting("object.semantic-mode")
                .decision(decision);
        emit(trace);
    }

    void emit(Trace trace) {
        GenerationReportEntry entry = new GenerationReportEntry(
                trace.path,
                seedSource,
                recipeFormat,
                new ArrayList<>(trace.settings),
                trace.step,
                trace.generator,
                Optional.ofNullable(trace.rule),
                trace.constraints,
                trace.safetyPolicies,
                new ArrayList<>(trace.decisions));
        try {
            listener.onEntry(entry);
        } catch (RuntimeException e) {
            LOGGER.warn("Generation report listener callback was ignored; listener="
                    + listener.getClass().getName() + ", cause=" + e.getClass().getName());
        }
    }

    /** What one resolution decided, filled in as it decides, and delivered once it is done. */
    final class Trace {

        private final String path;
        GenerationStep step = GenerationStep.DEFAULT;
        private String generator = "default";
        private @Nullable FixtureRule rule;
        private List<String> constraints = List.of();
        private final Set<String> settings = new LinkedHashSet<>();
        private final Map<String, String> safetyPolicies = new LinkedHashMap<>();
        private final Set<GenerationDecision> decisions = EnumSet.noneOf(GenerationDecision.class);

        private Trace(String path) {
            this.path = path;
        }

        /** The path without the root type's name, as an {@code ObjectFaker} path reads. */
        String relativePath() {
            return path.substring(path.indexOf('.') + 1);
        }

        Trace step(GenerationStep step, String generator) {
            this.step = step;
            this.generator = generator;
            return this;
        }

        Trace rule(FixtureRule rule) {
            this.rule = rule;
            return this;
        }

        Trace setting(String name) {
            settings.add(name);
            return this;
        }

        Trace decision(GenerationDecision decision) {
            decisions.add(decision);
            return this;
        }

        Trace constraints(@Nullable AnnotatedElement element) {
            constraints = BeanValidationSupport.constraintNames(element);
            return this;
        }

        Trace safetyPolicy(String setting, Enum<?> policy) {
            settings.add(setting);
            safetyPolicies.put(setting, policy.name());
            return this;
        }
    }
}
