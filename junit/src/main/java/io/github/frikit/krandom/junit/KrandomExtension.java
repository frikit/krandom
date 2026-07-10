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
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.support.AnnotationSupport;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.Random;

/**
 * JUnit 5 extension that fixes the kRandom seed per test and reports it on failure.
 *
 * <p>Every test runs with a deterministic seed: either pinned via {@link KrandomSeed} (method
 * level wins over class level, enclosing classes are searched for {@code @Nested} tests) or a
 * random per-test seed when unpinned. The seed is injectable as a {@link GeneratorConfig} or
 * {@link GeneratorConfig.Builder} test parameter, both pre-seeded with the test's seed.
 *
 * <p>When a test fails, the seed is published as a JUnit report entry under the
 * {@value #REPORT_ENTRY_KEY} key and a safe portable recipe under {@value #RECIPE_REPORT_ENTRY_KEY}.
 * Both are printed to {@code System.err} with a reproduction hint, so a failing unpinned run can
 * be replayed by annotating the test with
 * {@code @KrandomSeed(<reported seed>L)} — no more unreproducible random-data failures.
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
    private static final String SEED_KEY = "seedInfo";
    private static final Random UNPINNED_SEEDS = new Random();

    /** Resolved seed for one test: the value plus whether it was pinned via the annotation. */
    private record SeedInfo(long seed, boolean pinned) {
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
        GeneratorConfig.Builder builder = GeneratorConfig.builder().seed(seedInfo(extensionContext).seed());
        if (parameterContext.getParameter().getType() == GeneratorConfig.class) {
            return builder.build();
        }
        return builder;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        SeedInfo info = context.getStore(NAMESPACE).get(SEED_KEY, SeedInfo.class);
        if (info == null) {
            // The failure happened before a seed was established (e.g. a @KrandomSeed
            // configuration error); there is nothing reproducible to report.
            return;
        }
        context.publishReportEntry(REPORT_ENTRY_KEY, Long.toString(info.seed()));
        String recipe = GenerationRecipe.builder().seed(info.seed()).build().serializeForDiagnostics();
        context.publishReportEntry(RECIPE_REPORT_ENTRY_KEY, recipe);
        System.err.println(failureMessage(context.getDisplayName(), info, recipe));
    }

    private static String failureMessage(String displayName, SeedInfo info, String recipe) {
        if (info.pinned()) {
            return ("krandom: test '%s' failed with pinned seed %d. Replay recipe:%n%s")
                .formatted(displayName, info.seed(), recipe);
        }
        return ("krandom: test '%s' failed with seed %d. "
                + "Annotate it with @KrandomSeed(%dL) to reproduce this run. Replay recipe:%n%s")
                .formatted(displayName, info.seed(), info.seed(), recipe);
    }

    /**
     * Returns the seed for the current test, computing and storing it on first access so that
     * parameter resolution and failure reporting observe the same value.
     */
    private static SeedInfo seedInfo(ExtensionContext context) {
        Store store = context.getStore(NAMESPACE);
        return store.computeIfAbsent(SEED_KEY, key -> computeSeedInfo(context), SeedInfo.class);
    }

    private static SeedInfo computeSeedInfo(ExtensionContext context) {
        Optional<KrandomSeed> annotation = findSeedAnnotation(context);
        if (annotation.isEmpty()) {
            return new SeedInfo(UNPINNED_SEEDS.nextLong(), false);
        }
        KrandomSeed seed = annotation.get();
        boolean hasValue = seed.value() != KrandomSeed.UNSET;
        boolean hasText = !seed.text().isEmpty();
        if (hasValue && hasText) {
            throw new ExtensionConfigurationException(
                    "@KrandomSeed must set either value or text, not both");
        }
        if (hasValue) {
            return new SeedInfo(seed.value(), true);
        }
        if (!hasText) {
            throw new ExtensionConfigurationException(
                    "@KrandomSeed requires a numeric value or a non-blank text seed");
        }
        if (seed.text().isBlank()) {
            throw new ExtensionConfigurationException("@KrandomSeed text seed must not be blank");
        }
        return new SeedInfo(GeneratorConfig.deriveSeed(seed.text()), true);
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
