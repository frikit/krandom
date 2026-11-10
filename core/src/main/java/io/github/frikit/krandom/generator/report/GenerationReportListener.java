/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.report;

/**
 * Receives a value-free {@link GenerationReportEntry} synchronously for each value that object generation
 * produces, when it is set with
 * {@link io.github.frikit.krandom.generator.GeneratorConfig.Builder#generationReportListener(GenerationReportListener)}.
 *
 * <p>The listener never sees a generated value. Reporting draws nothing from any random stream, so a seeded
 * configuration generates the same values with or without a listener. A listener that throws is logged and
 * ignored.
 */
@FunctionalInterface
public interface GenerationReportListener {

    /**
     * Observes how one value was produced.
     *
     * @param entry the value-free entry
     */
    void onEntry(GenerationReportEntry entry);
}
