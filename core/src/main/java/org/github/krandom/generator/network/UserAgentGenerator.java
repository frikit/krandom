/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates browser and bot user-agent strings.
 */
public final class UserAgentGenerator implements Generator<String> {

    private static final String[] OS = {
            "Windows NT 10.0; Win64; x64",
            "Macintosh; Intel Mac OS X 13_5",
            "X11; Linux x86_64",
            "Linux; Android 14; Pixel 8",
            "iPhone; CPU iPhone OS 17_2 like Mac OS X"
    };

    private static final String[] ENGINES = {"AppleWebKit/537.36", "Gecko/20100101"};
    private static final String[] BROWSERS = {
            "Chrome/123.0.0.0 Safari/537.36",
            "Firefox/123.0",
            "Version/17.2 Mobile/15E148 Safari/604.1",
            "Edg/122.0.0.0"
    };

    private static final String[] BOTS = {
            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",
            "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)",
            "DuckDuckBot/1.0; (+http://duckduckgo.com/duckduckbot.html)",
            "Twitterbot/1.0"
    };

    private final Random random;

    /** Creates a user-agent generator with default configuration. */
    public UserAgentGenerator() {
        this(GeneratorConfig.defaults());
    }

    /** Creates a user-agent generator with the specified configuration. */
    public UserAgentGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    /** Generates a browser user-agent string. */
    @Override
    public String generate() {
        String os = OS[random.nextInt(OS.length)];
        String engine = ENGINES[random.nextInt(ENGINES.length)];
        String browser = BROWSERS[random.nextInt(BROWSERS.length)];
        return "Mozilla/5.0 (" + os + ") " + engine + " (KHTML, like Gecko) " + browser;
    }

    /** Generates a crawler/bot user-agent string. */
    public String generateBot() {
        return BOTS[random.nextInt(BOTS.length)];
    }
}
