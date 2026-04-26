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

    public DateGenerator localDate() { return new DateGenerator(); }

    public LocalDateTimeGenerator localDateTime() { return new LocalDateTimeGenerator(); }

    public InstantGenerator instant() { return new InstantGenerator(); }

    public ZonedDateTimeGenerator zonedDateTime() { return new ZonedDateTimeGenerator(); }

    public DurationGenerator duration() { return new DurationGenerator(); }

    public TimezoneGenerator timezone() { return new TimezoneGenerator(); }
}
