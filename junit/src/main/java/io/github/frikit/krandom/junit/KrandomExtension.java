/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.junit;

import io.github.frikit.krandom.generator.GenerationRecipe;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Krandom;
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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * JUnit 5 extension that fixes the kRandom seed per test and reports it on failure.
 *
 * <p>Every test runs with a deterministic seed: either pinned via {@link KrandomSeed} (method
 * level wins over class level, enclosing classes are searched for {@code @Nested} tests) or a
 * random per-test seed when unpinned. The seed is injectable as a {@link Krandom} session, a
 * {@link GeneratorConfig} or a {@link GeneratorConfig.Builder} test parameter, all pre-seeded with
 * the test's seed.
 *
 * <p><b>Scopes.</b> Test-method parameters and test-class constructor parameters (under the
 * default {@code PER_METHOD} lifecycle) share the test method's configuration. Parameters of
 * {@code @BeforeAll}/{@code @AfterAll} methods, constructor parameters under
 * {@code @TestInstance(PER_CLASS)}, parameters of {@code @MethodSource} factory methods, and
 * parameters of {@code @BeforeParameterizedClassInvocation}/{@code @AfterParameterizedClassInvocation}
 * methods are resolved against the test class instead: they receive one class-scoped configuration
 * seeded from the class-level {@link KrandomSeed} (or a random seed), shared by those class-level
 * methods and independent of every test method's seed. A {@code @ParameterizedTest} therefore
 * keeps its method-level {@code @KrandomSeed} for its invocations, not for its factory. A
 * class-scoped configuration never leaks into a test method, so a method-level
 * {@code @KrandomSeed} always applies to the test. When a test fails, the class-scoped seeds of its
 * enclosing classes are reported next to the test's own seed, and the printed replay commands
 * restore them.
 *
 * <p><b>Sessions.</b> A {@link Krandom} session is built from the configuration above when first
 * injected. Every {@code Krandom} parameter of one test (its constructor, {@code @BeforeEach}, test
 * method and {@code @AfterEach}) is the same instance, so the streams continue from one method to
 * the next. Each test, repetition and parameterized invocation has a session of its own, so tests
 * running concurrently do not share one. The class-level session of {@code @BeforeAll},
 * {@code @AfterAll}, {@code PER_CLASS} constructors and the factory of a {@code @ParameterizedClass}
 * is a separate instance that tests neither reuse nor advance; a class-level {@link KrandomSeed}
 * gives it the seed of those tests, so their sessions then repeat its values. The factory of a
 * {@code @ParameterizedTest}, and the invocation callbacks of one {@code @ParameterizedClass}
 * invocation, have a session of their own built from the class-scoped configuration: it starts the
 * class-level streams again and leaves the class-level session where it was.
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
 *   <li>{@code krandom.junit.redact-invocation-names=true}
 *       ({@code KRANDOM_JUNIT_REDACT_INVOCATION_NAMES}) names an invocation of a test template, such
 *       as a {@code @ParameterizedTest} or {@code @RepeatedTest}, in the failure line by its
 *       template's display name and invocation index, as in {@code 'rejects(String) [2]'}, rather
 *       than by its own display name, which JUnit formats from the arguments. The default
 *       {@code false} keeps the display name; other values are rejected.</li>
 * </ul>
 *
 * <p>A replay that cannot reproduce the arguments of a {@code @MethodSource} factory says so on
 * {@code System.err}: a {@code krandom.junit.recipe} override applied to a class-scoped
 * configuration that {@code krandom.junit.class-recipe} does not list, and a {@link KrandomSeed}
 * on a {@code @ParameterizedTest} whose factory takes an unpinned class-scoped configuration.
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
    private static final String REDACT_INVOCATION_NAMES_PROPERTY = "krandom.junit.redact-invocation-names";
    private static final String REDACT_INVOCATION_NAMES_ENVIRONMENT = "KRANDOM_JUNIT_REDACT_INVOCATION_NAMES";
    private static final String CLASS_SCOPED_PARAMETERS =
            "(@BeforeAll/@AfterAll, PER_CLASS constructor and @MethodSource factory parameters)";

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

    /**
     * Resolved configuration for one extension context, whether its source is explicit, and whether
     * its failure line names an invocation of a test template without its display name.
     */
    private record SeedInfo(
            GeneratorConfig config, boolean pinned, ClockSource clockSource, boolean redactInvocationNames) {

        private SeedInfo(GeneratorConfig config, boolean pinned, ClockSource clockSource) {
            this(config, pinned, clockSource, false);
        }

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
    private record ContextKey(String uniqueId) {}

    /** Store key of the session of exactly one extension context, for the reason given on {@link ContextKey}. */
    private record SessionKey(String uniqueId) {}

    /** Store key marking that the factory of one parameterized test has been warned about. */
    private record FactoryWarningKey(String uniqueId) {}

    private record ConfiguredValue(String value, String source) {}

    /** The class-scoped configuration of one enclosing test class. */
    private record ClassScope(Class<?> testClass, SeedInfo info) {}

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
        return type == Krandom.class || type == GeneratorConfig.class || type == GeneratorConfig.Builder.class;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        SeedInfo info = seedInfo(extensionContext);
        if (isTestTemplate(extensionContext)) {
            warnIfOnlyTheTestIsPinned(extensionContext, info);
        }
        GeneratorConfig config = info.config();
        Class<?> type = parameterContext.getParameter().getType();
        if (type == Krandom.class) {
            return session(extensionContext, config);
        }
        if (type == GeneratorConfig.class) {
            return config;
        }
        return config.toBuilder();
    }

    /**
     * Warns once per parameterized test when a {@link KrandomSeed} on the test method pins its
     * invocations while its {@code @MethodSource} factory, which JUnit resolves against the context
     * of the test as a whole, takes an unpinned class-scoped configuration: the arguments then change
     * from run to run, and the seed that a failure reports for the test does not reproduce them.
     */
    private static void warnIfOnlyTheTestIsPinned(ExtensionContext factoryContext, SeedInfo classInfo) {
        boolean testPinned = factoryContext
                .getTestMethod()
                .flatMap(method -> AnnotationSupport.findAnnotation(method, KrandomSeed.class))
                .isPresent();
        if (testPinned && !classInfo.pinned()) {
            factoryContext
                    .getStore(NAMESPACE)
                    .computeIfAbsent(
                            new FactoryWarningKey(factoryContext.getUniqueId()),
                            key -> {
                                String testClass = scopeContext(factoryContext)
                                        .getRequiredTestClass()
                                        .getSimpleName();
                                System.err.println("krandom: @KrandomSeed on '" + factoryContext.getDisplayName()
                                        + "' pins its invocations, not the arguments of its @MethodSource factory,"
                                        + " which takes the unpinned class-scoped configuration of '" + testClass
                                        + "'. To reproduce the arguments of a failing run, also annotate '"
                                        + testClass + "' with the class-scoped seed that the failure output"
                                        + " printed, or replay with the printed commands.");
                                return Boolean.TRUE;
                            },
                            Boolean.class);
        }
    }

    /**
     * Returns the one session of this exact context, so that every parameter resolved for it
     * continues the same streams. {@link Krandom#of} refuses only caller-owned randomness, and every
     * configuration this extension creates owns a seed instead, whether from {@link KrandomSeed}, a
     * replay override or a recipe.
     */
    private static Krandom session(ExtensionContext context, GeneratorConfig config) {
        return context.getStore(NAMESPACE)
                .computeIfAbsent(new SessionKey(context.getUniqueId()), key -> Krandom.of(config), Krandom.class);
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

    private static String failureMessage(
            ExtensionContext context, SeedInfo info, GenerationRecipe recipe, String serialized) {
        StringBuilder message = new StringBuilder();
        String testName = info.redactInvocationNames() ? redactedTestName(context) : context.getDisplayName();
        if (info.pinned()) {
            message.append("krandom: test '%s' failed with pinned seed %d.".formatted(testName, info.seed()));
        } else {
            message.append(
                    "krandom: test '%s' failed with seed %d. Annotate it with @KrandomSeed(%dL) to reproduce this run."
                            .formatted(testName, info.seed(), info.seed()));
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
            message.append(CLASS_REPLAY_RECIPE_ENVIRONMENT)
                    .append("='")
                    .append(classRecipes)
                    .append("' ");
        }
        message.append(REPLAY_RECIPE_ENVIRONMENT)
                .append("=base64:")
                .append(encoded)
                .append(" ./gradlew test --tests '")
                .append(testClass)
                .append("' --rerun")
                .append(System.lineSeparator())
                .append("Replay with Maven: mvn test -Dtest='")
                .append(testClass)
                .append("' -D")
                .append(REPLAY_RECIPE_PROPERTY)
                .append("=base64:")
                .append(encoded);
        if (classRecipes.length() > 0) {
            message.append(" -D")
                    .append(CLASS_REPLAY_RECIPE_PROPERTY)
                    .append("='")
                    .append(classRecipes)
                    .append('\'');
        }
        message.append(System.lineSeparator())
                .append("Replay recipe:")
                .append(System.lineSeparator())
                .append(serialized);
        return message.toString();
    }

    /**
     * Names an invocation of a test template, such as a {@code @ParameterizedTest}, by the display
     * name of its template and its invocation index, because JUnit formats the display name of a
     * parameterized invocation from its arguments, which may hold generated data. Any other test
     * keeps its display name. The index is the value of the last unique ID segment, which the Jupiter
     * engine writes as {@code [test-template-invocation:#<index>]}.
     */
    private static String redactedTestName(ExtensionContext context) {
        return context.getParent()
                .filter(KrandomExtension::isTestTemplate)
                .map(template -> {
                    String uniqueId = context.getUniqueId();
                    String index = uniqueId.substring(uniqueId.lastIndexOf(":#") + 2, uniqueId.length() - 1);
                    return template.getDisplayName() + " [" + index + "]";
                })
                .orElseGet(context::getDisplayName);
    }

    private static String encode(String serializedRecipe) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(serializedRecipe.getBytes(StandardCharsets.UTF_8));
    }

    private static String clockLine(ClockSource source, GenerationRecipe recipe) {
        String clock = recipe.getClockInstant() + " (" + recipe.getClockZone().getId() + ")";
        return switch (source) {
            case LIVE ->
                "krandom: recipe clock " + clock + " was read when the failure was reported; the test ran"
                        + " with a live clock, so time-relative data may not replay exactly. Set "
                        + SNAPSHOT_CLOCK_PROPERTY + "=true for time-sensitive tests.";
            case SNAPSHOT ->
                "krandom: recipe clock " + clock + " was captured before parameter injection" + " ("
                        + SNAPSHOT_CLOCK_PROPERTY + "=true); generation used this instant.";
            case RECIPE -> "krandom: recipe clock " + clock + " was fixed by the replay recipe.";
        };
    }

    /**
     * Returns the class-scoped configurations of the enclosing classes, outermost first. A test
     * template or a class template invocation holds none: {@link #seedInfo} keeps its configuration
     * under its test class, so each class is listed once.
     */
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
        String prefix = "krandom: class-scoped configuration of '"
                + scope.testClass().getSimpleName()
                + "' " + CLASS_SCOPED_PARAMETERS;
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
     * Returns the configuration of this context's scope, computing and storing it on first access so
     * that parameter resolution and failure reporting observe the same value.
     */
    private SeedInfo seedInfo(ExtensionContext context) {
        ExtensionContext scope = scopeContext(context);
        return scope.getStore(NAMESPACE)
                .computeIfAbsent(new ContextKey(scope.getUniqueId()), key -> computeSeedInfo(scope), SeedInfo.class);
    }

    /**
     * Returns the context that owns the configuration of this context: the context itself for a test
     * or a test class, and the test class's context for the two contexts JUnit resolves class-level
     * parameters against besides it. A {@code @MethodSource} factory is called with the context of
     * the parameterized test as a whole, and {@code @BeforeParameterizedClassInvocation} and
     * {@code @AfterParameterizedClassInvocation} methods with the context of one invocation of a
     * {@code @ParameterizedClass}. Neither is a test, so both share the class-scoped configuration
     * rather than create a second one, which failure reports would list under the same class name.
     */
    private static ExtensionContext scopeContext(ExtensionContext context) {
        ExtensionContext current = context;
        while (isTestTemplate(current) || isClassTemplateInvocation(current)) {
            current = current.getParent().orElseThrow();
        }
        return current;
    }

    /**
     * Whether this is the context of a test template as a whole, such as a {@code @ParameterizedTest}
     * method, rather than of one of its invocations. It has the test class, test method and parent
     * that the context of a {@code @Test} method would have, so this reads the type of the last unique
     * ID segment, which the Jupiter engine names {@code test-template}
     * ({@code TestTemplateTestDescriptor.SEGMENT_TYPE}). Unique IDs encode {@code /} inside segments,
     * so the last {@code /} starts the last segment.
     */
    private static boolean isTestTemplate(ExtensionContext context) {
        String uniqueId = context.getUniqueId();
        return uniqueId.startsWith("[test-template:", uniqueId.lastIndexOf('/') + 1);
    }

    /**
     * Whether this is the context of one invocation of a class template, such as a
     * {@code @ParameterizedClass}: no other context without a test method has a parent of the same
     * test class.
     */
    private static boolean isClassTemplateInvocation(ExtensionContext context) {
        Optional<Class<?>> parentClass = context.getParent().flatMap(ExtensionContext::getTestClass);
        return context.getTestMethod().isEmpty()
                && parentClass.isPresent()
                && parentClass.equals(context.getTestClass());
    }

    /** Returns the configuration stored for this exact context, never one of an ancestor. */
    private static @Nullable SeedInfo ownSeedInfo(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(new ContextKey(context.getUniqueId()), SeedInfo.class);
    }

    private SeedInfo computeSeedInfo(ExtensionContext context) {
        boolean snapshotClock = booleanSetting(context, SNAPSHOT_CLOCK_PROPERTY, SNAPSHOT_CLOCK_ENVIRONMENT);
        boolean redactInvocationNames =
                booleanSetting(context, REDACT_INVOCATION_NAMES_PROPERTY, REDACT_INVOCATION_NAMES_ENVIRONMENT);
        SeedInfo info = computeConfiguredSeedInfo(context);
        GeneratorConfig config = info.config();
        ClockSource clockSource = info.clockSource();
        if (snapshotClock && clockSource != ClockSource.RECIPE) {
            config = config.snapshotClock();
            clockSource = ClockSource.SNAPSHOT;
        }
        return new SeedInfo(config, info.pinned(), clockSource, redactInvocationNames);
    }

    private boolean booleanSetting(ExtensionContext context, String property, String variable) {
        Optional<ConfiguredValue> configured = configuredValue(context, property, variable);
        if (configured.isEmpty()) {
            return false;
        }
        return switch (configured.get().value()) {
            case "true" -> true;
            case "false" -> false;
            default ->
                throw new ExtensionConfigurationException(property + " must be true or false, got '"
                        + configured.get().value() + "' from "
                        + configured.get().source());
        };
    }

    private SeedInfo computeConfiguredSeedInfo(ExtensionContext context) {
        boolean classScoped =
                context.getTestMethod().isEmpty() && context.getTestClass().isPresent();
        if (classScoped) {
            Optional<SeedInfo> classReplay = classRecipeInfo(context);
            if (classReplay.isPresent()) {
                return classReplay.get();
            }
        }
        Optional<ConfiguredValue> recipeOverride =
                configuredValue(context, REPLAY_RECIPE_PROPERTY, REPLAY_RECIPE_ENVIRONMENT);
        Optional<ConfiguredValue> seedOverride =
                configuredValue(context, REPLAY_SEED_PROPERTY, REPLAY_SEED_ENVIRONMENT);
        if (recipeOverride.isPresent() && seedOverride.isPresent()) {
            throw new ExtensionConfigurationException("Configure only one replay override: "
                    + recipeOverride.get().source() + " or "
                    + seedOverride.get().source());
        }
        if (recipeOverride.isPresent()) {
            SeedInfo info = recipeInfo(recipeOverride.get());
            if (classScoped) {
                warnThatTheRecipeReplaysClassScopedData(context, recipeOverride.get());
            }
            return info;
        }
        if (seedOverride.isPresent()) {
            return numericSeedInfo(seedOverride.get());
        }

        Optional<KrandomSeed> annotation = findSeedAnnotation(context);
        if (annotation.isEmpty()) {
            return new SeedInfo(
                    GeneratorConfig.builder().seed(UNPINNED_SEEDS.nextLong()).build(), false, ClockSource.LIVE);
        }
        KrandomSeed seed = annotation.get();
        boolean hasValue = seed.value() != KrandomSeed.UNSET;
        boolean hasText = !seed.text().isEmpty();
        if (hasValue && hasText) {
            throw new ExtensionConfigurationException("@KrandomSeed must set either value or text, not both");
        }
        if (hasValue) {
            return new SeedInfo(GeneratorConfig.builder().seed(seed.value()).build(), true, ClockSource.LIVE);
        }
        if (!hasText) {
            throw new ExtensionConfigurationException("@KrandomSeed requires a numeric value or a non-blank text seed");
        }
        if (seed.text().isBlank()) {
            throw new ExtensionConfigurationException("@KrandomSeed text seed must not be blank");
        }
        return new SeedInfo(GeneratorConfig.builder().seed(seed.text()).build(), true, ClockSource.LIVE);
    }

    /**
     * Warns that a recipe override, which replays one failing test, also seeds a class-scoped
     * configuration that no class recipe restores. The failure output prints a class recipe for
     * every class-scoped configuration beside the test's recipe, so this replay leaves out a value
     * that it printed, and what {@code @MethodSource} factories and {@code @BeforeAll} methods
     * generate may differ from the failing run. A seed override does not warn, because a build may
     * set one permanently.
     */
    private static void warnThatTheRecipeReplaysClassScopedData(ExtensionContext context, ConfiguredValue recipe) {
        System.err.println("krandom: " + recipe.source() + " replays the class-scoped configuration of '"
                + context.getRequiredTestClass().getSimpleName() + "' " + CLASS_SCOPED_PARAMETERS
                + " as well, so @MethodSource arguments and other data created there come from that recipe and may"
                + " differ from the failing run. To replay them, also set " + CLASS_REPLAY_RECIPE_ENVIRONMENT
                + " (Maven: -D" + CLASS_REPLAY_RECIPE_PROPERTY + ") to the value that the failure output printed.");
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
        Optional<ConfiguredValue> configured =
                configuredValue(context, CLASS_REPLAY_RECIPE_PROPERTY, CLASS_REPLAY_RECIPE_ENVIRONMENT);
        if (configured.isEmpty()) {
            return Optional.empty();
        }
        String recipe = classRecipes(configured.get()).get(classKey(context.getRequiredTestClass()));
        return recipe == null
                ? Optional.empty()
                : Optional.of(
                        recipeInfo(new ConfiguredValue(recipe, configured.get().source())));
    }

    /** Parses {@code <test class>=base64:<recipe>} entries separated by commas. */
    private static Map<String, String> classRecipes(ConfiguredValue configured) {
        Map<String, String> recipes = new HashMap<>();
        String[] entries = configured.value().split(",", -1);
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i];
            int separator = entry.indexOf('=');
            boolean wellFormed = separator > 0 && entry.startsWith("base64:", separator + 1);
            if (!wellFormed
                    || recipes.putIfAbsent(entry.substring(0, separator), entry.substring(separator + 1)) != null) {
                throw new ExtensionConfigurationException("Invalid " + configured.source() + " entry " + (i + 1)
                        + ": expected unique" + " <test class>=base64:<recipe> entries separated by commas");
            }
        }
        return recipes;
    }

    private static SeedInfo recipeInfo(ConfiguredValue override) {
        try {
            GeneratorConfig config =
                    GenerationRecipe.parse(decodeRecipe(override.value())).toGeneratorConfig();
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
            GeneratorConfig config = GeneratorConfig.builder()
                    .seed(Long.parseLong(override.value()))
                    .build();
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
