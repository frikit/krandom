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
 * Generates standard HTTP status codes.
 */
public final class HttpStatusCodeGenerator implements Generator<Integer> {

    private static final int[] CODES_1XX = {100, 101, 102, 103};
    private static final int[] CODES_2XX = {200, 201, 202, 203, 204, 205, 206, 207, 208, 226};
    private static final int[] CODES_3XX = {300, 301, 302, 303, 304, 305, 307, 308};
    private static final int[] CODES_4XX = {400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413,
            414, 415, 416, 417, 418, 421, 422, 423, 424, 425, 426, 428, 429, 431, 451};
    private static final int[] CODES_5XX = {500, 501, 502, 503, 504, 505, 506, 507, 508, 510, 511};
    private static final int[] ALL = concatAll();

    private final Random random;

    public HttpStatusCodeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public HttpStatusCodeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.getSeed().isPresent()
                ? new Random(config.getSeed().getAsLong())
                : new SecureRandom();
    }

    @Override
    public Integer generate() {
        return ALL[random.nextInt(ALL.length)];
    }

    /**
     * Generates a status code from a specific category (1..5).
     */
    public int generateByCategory(int category) {
        int[] source = switch (category) {
            case 1 -> CODES_1XX;
            case 2 -> CODES_2XX;
            case 3 -> CODES_3XX;
            case 4 -> CODES_4XX;
            case 5 -> CODES_5XX;
            default -> throw new IllegalArgumentException("category must be in [1,5], got: " + category);
        };
        return source[random.nextInt(source.length)];
    }

    private static int[] concatAll() {
        int total = CODES_1XX.length + CODES_2XX.length + CODES_3XX.length + CODES_4XX.length + CODES_5XX.length;
        int[] all = new int[total];
        int i = 0;
        for (int code : CODES_1XX) all[i++] = code;
        for (int code : CODES_2XX) all[i++] = code;
        for (int code : CODES_3XX) all[i++] = code;
        for (int code : CODES_4XX) all[i++] = code;
        for (int code : CODES_5XX) all[i++] = code;
        return all;
    }
}
