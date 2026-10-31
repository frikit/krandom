/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.user;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.network.NetworkSafetyPolicy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates avatar image URLs for user-profile fixtures.
 *
 * <p>{@link NetworkSafetyPolicy#REALISTIC_UNCLASSIFIED} uses real avatar services' hosts, and
 * {@link NetworkSafetyPolicy#TEST_SAFE_RESERVED} the same URL shapes under the reserved {@code .test}
 * top-level domain. A supplied username or size does not change the host.
 */
public final class AvatarUrlGenerator implements Generator<String> {

    private static final String[] PROVIDERS = {
        "https://api.dicebear.com/8.x/identicon/svg?seed=%s",
        "https://robohash.org/%s.png?size=%dx%d",
        "https://ui-avatars.com/api/?name=%s&size=%d"
    };

    /** The same providers' URL shapes, on hosts under the reserved {@code .test} top-level domain. */
    private static final String[] RESERVED_PROVIDERS = {
        "https://api.dicebear.test/8.x/identicon/svg?seed=%s",
        "https://robohash.test/%s.png?size=%dx%d",
        "https://ui-avatars.test/api/?name=%s&size=%d"
    };

    private final Random random;
    private final UsernameGenerator usernameGenerator;
    private final String[] providers;

    public AvatarUrlGenerator() {
        this(GeneratorConfig.defaults());
    }

    public AvatarUrlGenerator(GeneratorConfig config) {
        GeneratorConfig effective = Objects.requireNonNull(config, "config must not be null");
        this.random = effective.createRandom();
        this.usernameGenerator = new UsernameGenerator(effective.forChildStream("username"));
        this.providers = effective.getNetworkSafetyPolicy() == NetworkSafetyPolicy.REALISTIC_UNCLASSIFIED
                ? PROVIDERS
                : RESERVED_PROVIDERS;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
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
        int provider = random.nextInt(providers.length);
        return switch (provider) {
            case 0 -> String.format(Locale.ROOT, providers[0], seed);
            case 1 -> String.format(Locale.ROOT, providers[1], seed, size, size);
            default -> String.format(Locale.ROOT, providers[2], seed, size);
        };
    }
}
