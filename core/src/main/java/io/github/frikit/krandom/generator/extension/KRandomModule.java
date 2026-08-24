/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.extension;

/**
 * Explicit, configuration-scoped extension module.
 *
 * <p>Modules are installed with {@code GeneratorConfig.builder().install(module)}. kRandom never
 * scans the classpath for modules, and contributions are visible only to APIs using the resulting
 * configuration. Implementations should be immutable and keep {@link #configure(KRandomModuleContext)}
 * free of global side effects because it is evaluated whenever a derived configuration is built.
 */
public interface KRandomModule {

    /**
     * Returns the stable module identifier used for conflict detection and diagnostics.
     *
     * @return non-blank, single-line identifier
     */
    String id();

    /**
     * Contributes providers and semantic aliases to one configuration.
     *
     * @param context scoped contribution context
     */
    void configure(KRandomModuleContext context);
}
