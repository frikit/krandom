/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.GenerationRecipe;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.StringJoiner;
import java.util.function.Function;

/**
 * JUnit 5 extension that fixes the kRandom seed per test and reports it on failure.
 *
 * <p>Every test runs with a deterministic seed: either pinned via {@link KrandomSeed} (method
 * level wins over class level, enclosing classes are searched for {@code @Nested} tests) or a
 * random per-test seed when unpinned. The seed is injectable as a {@link GeneratorConfig} or
 * {@link GeneratorConfig.Builder} test parameter, both pre-seeded with the test's seed.
 *
 * <p><b>Scopes.</b> Test-method parameters and test-class constructor parameters (under the
 * default {@code PER_METHOD} lifecycle) share the test method's configuration. Parameters of
 * {@code @BeforeAll}/{@code @AfterAll} methods, and constructor parameters under
 * {@code @TestInstance(PER_CLASS)}, are resolved against the test class instead: they receive one
 * class-scoped configuration seeded from the class-level {@link KrandomSeed} (or a random seed),
 * shared by those class-level callbacks and independent of every test method's seed. A
 * class-scoped configuration never leaks into a test method, so a method-level
 * {@code @KrandomSeed} always applies. When a test fails, the class-scoped seeds of its enclosing
 * classes are reported next to the test's own seed, and the printed replay commands restore them.
 *
 * <p><b>Configuration.</b> The {@code krandom.junit.*} settings below are read as JUnit Platform
 * configuration parameters, so a launcher configuration parameter, a JVM system property of the
 * test JVM, or an entry in {@code junit-platform.properties} all work; the matching
 * {@code KRANDOM_JUNIT_*} environment variable is consulted only when no configuration parameter
 * is present. Environment variables are the portable way to reach a forked test JVM from Gradle,
 * because {@code ./gradlew test -Dname=value} sets a property on the Gradle JVM only.
 *
 * <ul>
 *   <li>{@code krandom.junit.snapshot-clock=true} ({@code KRANDOM_JUNIT_SNAPSHOT_CLOCK}) captures
 *       one fixed clock per configuration before parameter injection, so generation and failure
 *       diagnostics use the same instant. The default remains a live clock; the failure recipe then
 *       records the instant at which the failure was reported, which the failure output states.</li>
 *   <li>{@code krandom.junit.recipe=base64:<serialized recipe>} ({@code KRANDOM_JUNIT_RECIPE}) or
 *       {@code krandom.junit.seed=<numeric seed>} ({@code KRANDOM_JUNIT_SEED}) replay a run without
 *       editing its source. A recipe takes the same form as {@link GenerationRecipe#serialize()},
 *       encoded with URL-safe Base64 without padding; literal recipes with {@code \n} line
 *       separators are also accepted. Replay overrides take precedence over {@link KrandomSeed} and
 *       apply to test-method and class-scoped configurations alike; configuring both a recipe and a
 *       seed is an error.</li>
 *   <li>{@code krandom.junit.class-recipe=<test class>=base64:<recipe>,...}
 *       ({@code KRANDOM_JUNIT_CLASS_RECIPE}) replays class-scoped configurations: each entry names a
 *       test class by its canonical name and gives the recipe of that class's class-scoped
 *       configuration. A listed class ignores the other overrides and {@link KrandomSeed} for its
 *       class-scoped configuration; test methods and unlisted classes are unaffected.</li>
 * </ul>
 *
 * <p>Customizations to an injected builder are not tracked by the extension; report the actual
 * customized configuration's recipe yourself.
 *
 * <p>When a test fails, the seed is published as a JUnit report entry under the
 * {@value #REPORT_ENTRY_KEY} key and a safe portable recipe under {@value #RECIPE_REPORT_ENTRY_KEY}.
 * Both are printed to {@code System.err} with a reproduction hint, the source of the recorded
 * clock instant, and copyable Gradle and Maven replay commands, so a failing unpinned run can be
 * replayed either by one of those commands or by annotating the test with
 * {@code @KrandomSeed(<reported seed>L)}.
 *
 * <p><b>Usage</b>
 * <pre>{@code
 *   @ExtendWith(KrandomExtension.class)
 *   class OrderServiceTest {
 *
 *       @Test
 *       void totalsAreNonNegative(GeneratorConfig config) {
 *           Order order = Generators.ofObject(Order.class, config).generate();
 *           assertTrue(service.total(order).signum() >= 0);
 *           // on failure: "krandom: test 'totalsAreNonNegative(GeneratorConfig)' failed with
 *           //              seed 1234567890. Annotate it with @KrandomSeed(1234567890L) ..."
 *       }
 *   }
 * }</pre>
 *
 * <p>The extension composes with the Spring Boot starter's {@code @KrandomTest} slice: JUnit
 * extensions stack, so a Spring test can keep its auto-configured beans and still receive a
 * seeded {@code GeneratorConfig} parameter from this extension.
 *
 * @see KrandomSeed
 */
public final class KrandomExtension implements BeforeEachCallback, ParameterResolver, TestWatcher {

    /** Key under which the failing test's seed is published as a JUnit report entry. */
    public static final String REPORT_ENTRY_KEY = "krandom.seed";

    /** Key under which the failing test's safe portable replay recipe is published. */
    public static final String RECIPE_REPORT_ENTRY_KEY = "krandom.recipe";

    private static final Namespace NAMESPACE = Namespace.create(KrandomExtension.class);
    private static final Random UNPINNED_SEEDS = new Random();
    private static final String REPLAY_RECIPE_PROPERTY = "krandom.junit.recipe";
    private static final String REPLAY_RECIPE_ENVIRONMENT = "KRANDOM_JUNIT_RECIPE";
    private static final String REPLAY_SEED_PROPERTY = "krandom.junit.seed";
    private static final String REPLAY_SEED_ENVIRONMENT = "KRANDOM_JUNIT_SEED";
    private static final String CLASS_REPLAY_RECIPE_PROPERTY = "krandom.junit.class-recipe";
    private static final String CLASS_REPLAY_RECIPE_ENVIRONMENT = "KRANDOM_JUNIT_CLASS_RECIPE";
    private static final String SNAPSHOT_CLOCK_PROPERTY = "krandom.junit.snapshot-clock";
    private static final String SNAPSHOT_CLOCK_ENVIRONMENT = "KRANDOM_JUNIT_SNAPSHOT_CLOCK";

    private final Function<String, @Nullable String> environment;

    /** Creates the extension. */
    public KrandomExtension() {
        this(System::getenv);
    }

    /** Creates the extension with an explicit environment-variable lookup (for tests). */
    KrandomExtension(Function<String, @Nullable String> environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
    }

    /** Where the clock instant recorded in a failure recipe comes from. */
    private enum ClockSource {
        /** A live clock: the recipe records the instant at which the failure was reported. */
        LIVE,
        /** {@code krandom.junit.snapshot-clock=true}: generation used the recorded instant. */
        SNAPSHOT,
        /** A replay recipe: the clock was fixed by the recipe. */
        RECIPE
    }

    /** Resolved configuration for one extension context and whether its source is explicit. */
    private record SeedInfo(GeneratorConfig config, boolean pinned, ClockSource clockSource) {

        private long seed() {
            return config.getSeed().orElseThrow();
        }

        private GenerationRecipe recipe() {
            return config.getGenerationRecipe()
                         .orElseThrow(() -> new IllegalStateException("test replay configuration has no recipe"));
        }
    }

    /**
     * Store key bound to exactly one extension context. Stores are hierarchical, so a key shared
     * by every context would let a test method silently reuse a configuration that an enclosing
     * class context created for {@code @BeforeAll} or constructor injection.
     */
    private record ContextKey(String uniqueId) {
    }

    private record ConfiguredValue(String value, String source) {
    }

    /** The class-scoped configuration of one enclosing test class. */
    private record ClassScope(Class<?> testClass, SeedInfo info) {
    }

    /**
     * Resolves constructor parameters against the test method, so constructor-injected
     * configurations share the test method's seed. Under {@code PER_CLASS} JUnit still supplies the
     * class context, which then receives the class-scoped configuration.
     */
    @Override
    public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
        return ExtensionContextScope.TEST_METHOD;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        seedInfo(context);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == GeneratorConfig.class || type == GeneratorConfig.Builder.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        GeneratorConfig config = seedInfo(extensionContext).config();
        if (parameterContext.getParameter().getType() == GeneratorConfig.class) {
            return config;
        }
        return config.toBuilder();
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        SeedInfo info = ownSeedInfo(context);
        if (info == null) {
            // The failure happened before a seed was established (e.g. a @KrandomSeed
            // configuration error); there is nothing reproducible to report.
            return;
        }
        GenerationRecipe recipe = info.recipe();
        String serialized = recipe.serializeForDiagnostics();
        context.publishReportEntry(REPORT_ENTRY_KEY, Long.toString(info.seed()));
        context.publishReportEntry(RECIPE_REPORT_ENTRY_KEY, serialized);
        System.err.println(failureMessage(context, info, recipe, serialized));
    }

    private static String failureMessage(ExtensionContext context,
                                         SeedInfo info,
                                         GenerationRecipe recipe,
                                         String serialized) {
        StringBuilder message = new StringBuilder();
        if (info.pinned()) {
            message.append("krandom: test '%s' failed with pinned seed %d."
                .formatted(context.getDisplayName(), info.seed()));
        } else {
            message.append("krandom: test '%s' failed with seed %d. Annotate it with @KrandomSeed(%dL) to reproduce this run."
                .formatted(context.getDisplayName(), info.seed(), info.seed()));
        }
        message.append(System.lineSeparator()).append(clockLine(info.clockSource(), recipe));
        StringJoiner classRecipes = new StringJoiner(",");
        for (ClassScope scope : classScopes(context)) {
            message.append(System.lineSeparator()).append(classScopedLine(scope));
            classRecipes.add(classKey(scope.testClass()) + "=base64:"
                             + encode(scope.info().recipe().serializeForDiagnostics()));
        }
        String encoded = encode(serialized);
        String testClass = replayTestClassName(context);
        message.append(System.lineSeparator()).append("Replay with Gradle: ");
        if (classRecipes.length() > 0) {
            message.append(CLASS_REPLAY_RECIPE_ENVIRONMENT).append("='").append(classRecipes).append("' ");
        }
        message.append(REPLAY_RECIPE_ENVIRONMENT).append("=base64:").append(encoded)
               .append(" ./gradlew test --tests '").append(testClass).append("' --rerun")
               .append(System.lineSeparator())
               .append("Replay with Maven: mvn test -Dtest='").append(testClass).append("' -D")
               .append(REPLAY_RECIPE_PROPERTY).append("=base64:").append(encoded);
        if (classRecipes.length() > 0) {
            message.append(" -D").append(CLASS_REPLAY_RECIPE_PROPERTY).append("='").append(classRecipes).append('\'');
        }
        message.append(System.lineSeparator())
               .append("Replay recipe:")
               .append(System.lineSeparator())
               .append(serialized);
        return message.toString();
    }

    private static String encode(String serializedRecipe) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(serializedRecipe.getBytes(StandardCharsets.UTF_8));
    }

    private static String clockLine(ClockSource source, GenerationRecipe recipe) {
        String clock = recipe.getClockInstant() + " (" + recipe.getClockZone().getId() + ")";
        return switch (source) {
            case LIVE -> "krandom: recipe clock " + clock + " was read when the failure was reported; the test ran"
                         + " with a live clock, so time-relative data may not replay exactly. Set "
                         + SNAPSHOT_CLOCK_PROPERTY + "=true for time-sensitive tests.";
            case SNAPSHOT -> "krandom: recipe clock " + clock + " was captured before parameter injection"
                             + " (" + SNAPSHOT_CLOCK_PROPERTY + "=true); generation used this instant.";
            case RECIPE -> "krandom: recipe clock " + clock + " was fixed by the replay recipe.";
        };
    }

    /** Returns the class-scoped configurations of the enclosing classes, outermost first. */
    private static Deque<ClassScope> classScopes(ExtensionContext context) {
        Deque<ClassScope> scopes = new ArrayDeque<>();
        for (@Nullable ExtensionContext current = context.getParent().orElse(null);
                current != null;
                current = current.getParent().orElse(null)) {
            SeedInfo classInfo = ownSeedInfo(current);
            if (classInfo == null || current.getTestClass().isEmpty()) {
                continue;
            }
            scopes.addFirst(new ClassScope(current.getRequiredTestClass(), classInfo));
        }
        return scopes;
    }

    private static String classScopedLine(ClassScope scope) {
        SeedInfo classInfo = scope.info();
        String prefix = "krandom: class-scoped configuration of '" + scope.testClass().getSimpleName()
                        + "' (@BeforeAll/@AfterAll or PER_CLASS constructor parameters)";
        return classInfo.pinned()
            ? prefix + " used pinned seed " + classInfo.seed() + "."
            : prefix + " used seed " + classInfo.seed() + "; the replay commands restore it. To pin it in source,"
              + " annotate that class with @KrandomSeed(" + classInfo.seed() + "L) and keep the test's own"
              + " @KrandomSeed.";
    }

    /** Names a test class in class replay recipes; canonical names contain no shell-sensitive {@code $}. */
    private static String classKey(Class<?> testClass) {
        String canonicalName = testClass.getCanonicalName();
        return canonicalName != null ? canonicalName : testClass.getName();
    }

    /**
     * Returns the binary name of the top-level class JUnit discovered for this test, which both
     * Gradle's {@code --tests} filter and Surefire's {@code -Dtest} accept; {@code @Nested} classes
     * run through their top-level class.
     */
    private static String replayTestClassName(ExtensionContext context) {
        ExtensionContext current = context;
        Optional<ExtensionContext> parent = current.getParent();
        while (parent.isPresent() && parent.get().getParent().isPresent()) {
            current = parent.get();
            parent = current.getParent();
        }
        return current.getTestClass()
                      .or(context::getTestClass)
                      .map(Class::getName)
                      .orElse("<test class>");
    }

    /**
     * Returns the configuration of this exact context, computing and storing it on first access so
     * that parameter resolution and failure reporting observe the same value.
     */
    private SeedInfo seedInfo(ExtensionContext context) {
        return context.getStore(NAMESPACE)
                      .computeIfAbsent(new ContextKey(context.getUniqueId()),
                                       key -> computeSeedInfo(context),
                                       SeedInfo.class);
    }

    /** Returns the configuration stored for this exact context, never one of an ancestor. */
    private static @Nullable SeedInfo ownSeedInfo(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(new ContextKey(context.getUniqueId()), SeedInfo.class);
    }

    private SeedInfo computeSeedInfo(ExtensionContext context) {
        boolean snapshotClock = snapshotClock(context);
        SeedInfo info = computeConfiguredSeedInfo(context);
        if (!snapshotClock || info.clockSource() == ClockSource.RECIPE) {
            return info;
        }
        return new SeedInfo(info.config().snapshotClock(), info.pinned(), ClockSource.SNAPSHOT);
    }

    private boolean snapshotClock(ExtensionContext context) {
        Optional<ConfiguredValue> configured = configuredValue(
            context, SNAPSHOT_CLOCK_PROPERTY, SNAPSHOT_CLOCK_ENVIRONMENT);
        if (configured.isEmpty()) {
            return false;
        }
        return switch (configured.get().value()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new ExtensionConfigurationException(
                SNAPSHOT_CLOCK_PROPERTY + " must be true or false, got '" + configured.get().value()
                    + "' from " + configured.get().source());
        };
    }

    private SeedInfo computeConfiguredSeedInfo(ExtensionContext context) {
        if (context.getTestMethod().isEmpty() && context.getTestClass().isPresent()) {
            Optional<SeedInfo> classReplay = classRecipeInfo(context);
            if (classReplay.isPresent()) {
                return classReplay.get();
            }
        }
        Optional<ConfiguredValue> recipeOverride = configuredValue(
            context, REPLAY_RECIPE_PROPERTY, REPLAY_RECIPE_ENVIRONMENT);
        Optional<ConfiguredValue> seedOverride = configuredValue(
            context, REPLAY_SEED_PROPERTY, REPLAY_SEED_ENVIRONMENT);
        if (recipeOverride.isPresent() && seedOverride.isPresent()) {
            throw new ExtensionConfigurationException(
                "Configure only one replay override: " + recipeOverride.get().source() + " or "
                    + seedOverride.get().source());
        }
        if (recipeOverride.isPresent()) {
            return recipeInfo(recipeOverride.get());
        }
        if (seedOverride.isPresent()) {
            return numericSeedInfo(seedOverride.get());
        }

        Optional<KrandomSeed> annotation = findSeedAnnotation(context);
        if (annotation.isEmpty()) {
            return new SeedInfo(GeneratorConfig.builder().seed(UNPINNED_SEEDS.nextLong()).build(), false,
                                ClockSource.LIVE);
        }
        KrandomSeed seed = annotation.get();
        boolean hasValue = seed.value() != KrandomSeed.UNSET;
        boolean hasText = !seed.text().isEmpty();
        if (hasValue && hasText) {
            throw new ExtensionConfigurationException(
                    "@KrandomSeed must set either value or text, not both");
        }
        if (hasValue) {
            return new SeedInfo(GeneratorConfig.builder().seed(seed.value()).build(), true, ClockSource.LIVE);
        }
        if (!hasText) {
            throw new ExtensionConfigurationException(
                    "@KrandomSeed requires a numeric value or a non-blank text seed");
        }
        if (seed.text().isBlank()) {
            throw new ExtensionConfigurationException("@KrandomSeed text seed must not be blank");
        }
        return new SeedInfo(GeneratorConfig.builder().seed(seed.text()).build(), true, ClockSource.LIVE);
    }

    /**
     * Looks a setting up as a JUnit configuration parameter (launcher parameter, JVM system
     * property, or {@code junit-platform.properties}), then as a JVM system property for launchers
     * that disable implicit configuration parameters, then as an environment variable.
     */
    private Optional<ConfiguredValue> configuredValue(ExtensionContext context, String property, String variable) {
        Optional<String> parameter = context.getConfigurationParameter(property);
        if (parameter.isPresent()) {
            return Optional.of(new ConfiguredValue(parameter.get(), property));
        }
        String systemProperty = System.getProperty(property);
        if (systemProperty != null) {
            return Optional.of(new ConfiguredValue(systemProperty, property));
        }
        String environmentValue = environment.apply(variable);
        if (environmentValue != null) {
            return Optional.of(new ConfiguredValue(environmentValue, variable));
        }
        return Optional.empty();
    }

    /** Returns the class replay recipe listed for the test class of this class context, if any. */
    private Optional<SeedInfo> classRecipeInfo(ExtensionContext context) {
        Optional<ConfiguredValue> configured = configuredValue(
            context, CLASS_REPLAY_RECIPE_PROPERTY, CLASS_REPLAY_RECIPE_ENVIRONMENT);
        if (configured.isEmpty()) {
            return Optional.empty();
        }
        String recipe = classRecipes(configured.get()).get(classKey(context.getRequiredTestClass()));
        return recipe == null
               ? Optional.empty()
               : Optional.of(recipeInfo(new ConfiguredValue(recipe, configured.get().source())));
    }

    /** Parses {@code <test class>=base64:<recipe>} entries separated by commas. */
    private static Map<String, String> classRecipes(ConfiguredValue configured) {
        Map<String, String> recipes = new HashMap<>();
        String[] entries = configured.value().split(",", -1);
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            int separator = entry.indexOf('=');
            boolean wellFormed = separator > 0 && entry.startsWith("base64:", separator + 1);
            if (!wellFormed || recipes.putIfAbsent(entry.substring(0, separator), entry.substring(separator + 1)) != null) {
                throw new ExtensionConfigurationException(
                    "Invalid " + configured.source() + " entry " + (i + 1) + ": expected unique"
                    + " <test class>=base64:<recipe> entries separated by commas");
            }
        }
        return recipes;
    }

    private static SeedInfo recipeInfo(ConfiguredValue override) {
        try {
            GeneratorConfig config = GenerationRecipe.parse(decodeRecipe(override.value())).toGeneratorConfig();
            return new SeedInfo(config, true, ClockSource.RECIPE);
        } catch (IllegalArgumentException exception) {
            throw new ExtensionConfigurationException("Invalid " + override.source() + " replay recipe", exception);
        }
    }

    private static String decodeRecipe(String value) {
        if (value.startsWith("base64:")) {
            byte[] decoded = Base64.getUrlDecoder().decode(value.substring("base64:".length()));
            return new String(decoded, StandardCharsets.UTF_8);
        }
        return value.replace("\\n", "\n");
    }

    private static SeedInfo numericSeedInfo(ConfiguredValue override) {
        try {
            GeneratorConfig config = GeneratorConfig.builder().seed(Long.parseLong(override.value())).build();
            return new SeedInfo(config, true, ClockSource.LIVE);
        } catch (NumberFormatException exception) {
            throw new ExtensionConfigurationException("Invalid " + override.source() + " numeric seed", exception);
        }
    }

    /**
     * Finds the nearest {@link KrandomSeed} by walking the context hierarchy: test method
     * first, then test class, then enclosing classes of {@code @Nested} tests.
     */
    private static Optional<KrandomSeed> findSeedAnnotation(ExtensionContext context) {
        for (@Nullable ExtensionContext current = context;
                current != null;
                current = current.getParent().orElse(null)) {
            Optional<KrandomSeed> found = current.getElement()
                    .flatMap(element -> AnnotationSupport.findAnnotation(element, KrandomSeed.class));
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }
}
