/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.datetime;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Generates timezone identifiers, preferring locale-country zones when available.
 */
public final class TimezoneGenerator implements Generator<String> {

    private static final Map<String, List<String>> COUNTRY_TO_ZONES = Map.of(
            "US", List.of("America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles"),
            "GB", List.of("Europe/London"),
            "AU", List.of("Australia/Sydney", "Australia/Melbourne", "Australia/Perth"),
            "DE", List.of("Europe/Berlin"),
            "FR", List.of("Europe/Paris"),
            "ES", List.of("Europe/Madrid"),
            "IT", List.of("Europe/Rome"),
            "BR", List.of("America/Sao_Paulo", "America/Manaus"),
            "JP", List.of("Asia/Tokyo"),
            "CN", List.of("Asia/Shanghai")
    );

    private static final List<String> ALL_ZONES = List.copyOf(ZoneId.getAvailableZoneIds().stream().sorted().toList());

    private final Locale locale;
    private final Random random;

    public TimezoneGenerator() {
        this(GeneratorConfig.defaults());
    }

    public TimezoneGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    public TimezoneGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.locale = effective.getLocale();
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public String generate() {
        List<String> localeZones = COUNTRY_TO_ZONES.get(locale.getCountry());
        if (localeZones != null) {
            return localeZones.get(random.nextInt(localeZones.size()));
        }
        return ALL_ZONES.get(random.nextInt(ALL_ZONES.size()));
    }

    /**
     * Generates a timezone identifier.
     * Alias for {@link #generate()}.
     *
     * @return timezone id
     */
    public String generateTimezone() {
        return generate();
    }

    /**
     * Generates the current UTC offset for a generated timezone.
     *
     * @return offset id such as {@code +01:00}
     */
    public String generateOffset() {
        ZoneId zoneId = ZoneId.of(generate());
        return zoneId.getRules().getOffset(Instant.now()).getId();
    }

    /**
     * Generates a UTC offset string.
     * Alias for {@link #generateOffset()}.
     *
     * @return offset id
     */
    public String generateUtcOffset() {
        return generateOffset();
    }

    /**
     * Returns the set of locale-preferred zones for the configured locale country.
     *
     * @return locale zones, or an empty set when no mapping exists
     */
    public Set<String> localeZones() {
        List<String> zones = COUNTRY_TO_ZONES.get(locale.getCountry());
        return zones == null ? Set.of() : Set.copyOf(zones);
    }
}
