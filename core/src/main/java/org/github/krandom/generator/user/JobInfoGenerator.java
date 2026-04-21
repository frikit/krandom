/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Objects;

/**
 * Generates structured job payloads with coherent title and profession fields.
 */
public final class JobInfoGenerator implements Generator<JobInfo> {

    private final GeneratorConfig     config;
    private final JobFieldGenerator   jobFieldGenerator;
    private final SeniorityGenerator  seniorityGenerator;
    private final JobTypeGenerator    jobTypeGenerator;
    private final ProfessionGenerator professionGenerator;

    /**
     * Creates a job-info generator using default configuration ({@link Locale#US}).
     */
    public JobInfoGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a job-info generator for the specified locale.
     *
     * @param locale locale to use
     */
    public JobInfoGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(Objects.requireNonNull(locale, "locale must not be null")).build());
    }

    /**
     * Creates a job-info generator using explicit configuration.
     *
     * @param config generator configuration
     */
    public JobInfoGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.jobFieldGenerator = new JobFieldGenerator(config);
        this.seniorityGenerator = new SeniorityGenerator(config);
        this.jobTypeGenerator = new JobTypeGenerator(config);
        this.professionGenerator = new ProfessionGenerator(config);
    }

    @Override
    public JobInfo generate() {
        String descriptor = jobFieldGenerator.generate();
        String level = seniorityGenerator.generate();
        String profession = professionGenerator.generate();
        return new JobInfo(
            descriptor,
            level,
            joinNonBlank(" ", level, profession),
            jobTypeGenerator.generate(),
            profession
        );
    }

    /**
     * Returns the configured locale.
     */
    public Locale getLocale() {
        return config.getLocale();
    }

    private static String joinNonBlank(String delimiter, String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(delimiter);
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
