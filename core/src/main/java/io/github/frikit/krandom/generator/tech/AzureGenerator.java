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
 * Generates Microsoft Azure resource identifiers such as regions and resource group names.
 *
 * <p>{@link #generate()} returns a random Azure region, e.g. {@code "eastus"}.
 *
 * <pre>{@code
 *   AzureGenerator azure = new AzureGenerator();
 *   String region = azure.region();        // e.g. "westeurope"
 *   String group  = azure.resourceGroup(); // e.g. "rg-a1b2c3"
 * }</pre>
 */
public final class AzureGenerator implements Generator<String> {

    private static final String[] REGIONS = {
        "eastus", "westus2", "westeurope", "northeurope",
        "uksouth", "southeastasia", "japaneast", "australiaeast"
    };

    private static final String GROUP_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";

    private final Random random;

    /**
     * Creates a generator using the default configuration.
     */
    public AzureGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator from explicit configuration (optional seed).
     *
     * @param config the generator configuration; must not be {@code null}
     */
    public AzureGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
    }

    /**
     * Generates a uniformly random Azure region.
     *
     * @return a region such as {@code "eastus"}; never {@code null}
     */
    @Override
    public String generate() {
        return region();
    }

    /**
     * Generates a uniformly random Azure region.
     *
     * @return a region such as {@code "westeurope"}; never {@code null}
     */
    public String region() {
        return REGIONS[random.nextInt(REGIONS.length)];
    }

    /**
     * Generates a resource group name: the prefix {@code "rg-"} followed by 4 to 12 lowercase
     * alphanumeric characters (matching {@code rg-[a-z0-9]{4,12}}).
     *
     * @return a resource group name such as {@code "rg-a1b2c3"}; never {@code null}
     */
    public String resourceGroup() {
        int length = 4 + random.nextInt(9); // 4..12 inclusive
        StringBuilder sb = new StringBuilder(3 + length);
        sb.append("rg-");
        for (int i = 0; i < length; i++) {
            sb.append(GROUP_CHARS.charAt(random.nextInt(GROUP_CHARS.length())));
        }
        return sb.toString();
    }
}
