/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.tech;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Objects;
import java.util.Random;

/**
 * Generates computer-related values such as operating systems, platforms, and device types.
 *
 * <p>{@link #generate()} returns a random operating system, e.g. {@code "Windows 11"}.
 *
 * <pre>{@code
 *   ComputerGenerator computer = new ComputerGenerator();
 *   String os       = computer.operatingSystem(); // e.g. "macOS Sonoma"
 *   String platform = computer.platform();        // e.g. "Linux"
 *   String device   = computer.deviceType();      // e.g. "Laptop"
 * }</pre>
 */
public final class ComputerGenerator implements Generator<String> {

    private static final String[] OPERATING_SYSTEMS = {
        "Windows 11", "Windows 10", "macOS Sonoma", "macOS Ventura", "Ubuntu 22.04 LTS",
        "Fedora 39", "Debian 12", "Android 14", "iOS 17", "ChromeOS"
    };

    private static final String[] PLATFORMS = {
        "Windows", "macOS", "Linux", "Android", "iOS"
    };

    private static final String[] DEVICE_TYPES = {
        "Laptop", "Desktop", "Tablet", "Phone", "Server"
    };

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public ComputerGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public ComputerGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random operating system.
     *
     * @return an operating system such as {@code "Windows 11"}; never {@code null}
     */
    @Override
    public String generate() {
        return operatingSystem();
    }

    /**
     * Generates a uniformly random operating system.
     *
     * @return an operating system such as {@code "macOS Sonoma"}; never {@code null}
     */
    public String operatingSystem() {
        return OPERATING_SYSTEMS[random.nextInt(OPERATING_SYSTEMS.length)];
    }

    /**
     * Generates a uniformly random platform family.
     *
     * @return a platform such as {@code "Linux"}; never {@code null}
     */
    public String platform() {
        return PLATFORMS[random.nextInt(PLATFORMS.length)];
    }

    /**
     * Generates a uniformly random device type.
     *
     * @return a device type such as {@code "Laptop"}; never {@code null}
     */
    public String deviceType() {
        return DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)];
    }
}
