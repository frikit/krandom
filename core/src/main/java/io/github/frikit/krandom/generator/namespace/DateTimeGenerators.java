/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.datetime.*;

/**
 * Fluent namespace for date/time-related generators.
 *
 * <p>Usage: {@code Generators.datetime().localDate().generate()}
 */
public final class DateTimeGenerators {

    private final GeneratorConfig config;

    public DateTimeGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public DateTimeGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public DateGenerator localDate() { return new DateGenerator(config); }

    public LocalDateTimeGenerator localDateTime() { return new LocalDateTimeGenerator(config); }

    public InstantGenerator instant() { return new InstantGenerator(config); }

    public ZonedDateTimeGenerator zonedDateTime() { return new ZonedDateTimeGenerator(config); }

    public DurationGenerator duration() { return new DurationGenerator(config); }

    public TimezoneGenerator timezone() { return new TimezoneGenerator(config); }
}
