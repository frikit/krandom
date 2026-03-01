/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.user;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

/**
 * Generates avatar image URLs for user-profile fixtures.
 */
public final class AvatarUrlGenerator implements Generator<String> {

    private static final String[] PROVIDERS = {
            "https://api.dicebear.com/8.x/identicon/svg?seed=%s",
            "https://robohash.org/%s.png?size=%dx%d",
            "https://ui-avatars.com/api/?name=%s&size=%d"
    };

    private final Random random;
    private final UsernameGenerator usernameGenerator;

    public AvatarUrlGenerator() {
        this(GeneratorConfig.defaults());
    }

    public AvatarUrlGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.getSeed().isPresent()
                ? new Random(effective.getSeed().getAsLong())
                : new SecureRandom();
        this.usernameGenerator = new UsernameGenerator(effective);
    }

    @Override
    public String generate() {
        return generate(usernameGenerator.generate(), 256);
    }

    public String generate(int size) {
        return generate(usernameGenerator.generate(), size);
    }

    public String generate(String username) {
        return generate(username, 256);
    }

    public String generate(String username, int size) {
        Objects.requireNonNull(username, "username must not be null");
        if (username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (size < 16 || size > 1024) {
            throw new IllegalArgumentException("size must be in [16, 1024], got: " + size);
        }

        String seed = encode(username);
        int provider = random.nextInt(PROVIDERS.length);
        return switch (provider) {
            case 0 -> String.format(PROVIDERS[0], seed);
            case 1 -> String.format(PROVIDERS[1], seed, size, size);
            default -> String.format(PROVIDERS[2], seed, size);
        };
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
