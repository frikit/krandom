/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates standard HTTP status codes.
 */
public final class HttpStatusCodeGenerator implements Generator<Integer> {

    private static final int[]                CODES_1XX           = { 100, 101, 102, 103 };
    private static final int[]                CODES_2XX           = { 200, 201, 202, 203, 204, 205, 206, 207, 208, 226 };
    private static final int[]                CODES_3XX           = { 300, 301, 302, 303, 304, 305, 307, 308 };
    private static final int[]                CODES_4XX           = { 400, 401, 402, 403, 404, 405, 406, 407, 408, 409, 410, 411, 412, 413,
                                                                      414, 415, 416, 417, 418, 421, 422, 423, 424, 425, 426, 428, 429, 431, 451 };
    private static final int[]                CODES_5XX           = { 500, 501, 502, 503, 504, 505, 506, 507, 508, 510, 511 };
    private static final String[]             COMMON_HEADER_NAMES = {
        "Accept", "Accept-Language", "Authorization", "Cache-Control", "Connection",
        "Content-Length", "Content-Type", "Host", "Origin", "Referer", "User-Agent", "X-Request-ID"
    };
    private static final Map<Integer, String> REASON_BY_CODE      = Map.ofEntries(
        Map.entry(100, "Continue"),
        Map.entry(101, "Switching Protocols"),
        Map.entry(102, "Processing"),
        Map.entry(103, "Early Hints"),
        Map.entry(200, "OK"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(203, "Non-Authoritative Information"),
        Map.entry(204, "No Content"),
        Map.entry(205, "Reset Content"),
        Map.entry(206, "Partial Content"),
        Map.entry(207, "Multi-Status"),
        Map.entry(208, "Already Reported"),
        Map.entry(226, "IM Used"),
        Map.entry(300, "Multiple Choices"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Found"),
        Map.entry(303, "See Other"),
        Map.entry(304, "Not Modified"),
        Map.entry(305, "Use Proxy"),
        Map.entry(307, "Temporary Redirect"),
        Map.entry(308, "Permanent Redirect"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(402, "Payment Required"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(405, "Method Not Allowed"),
        Map.entry(406, "Not Acceptable"),
        Map.entry(407, "Proxy Authentication Required"),
        Map.entry(408, "Request Timeout"),
        Map.entry(409, "Conflict"),
        Map.entry(410, "Gone"),
        Map.entry(411, "Length Required"),
        Map.entry(412, "Precondition Failed"),
        Map.entry(413, "Payload Too Large"),
        Map.entry(414, "URI Too Long"),
        Map.entry(415, "Unsupported Media Type"),
        Map.entry(416, "Range Not Satisfiable"),
        Map.entry(417, "Expectation Failed"),
        Map.entry(418, "I'm a teapot"),
        Map.entry(421, "Misdirected Request"),
        Map.entry(422, "Unprocessable Content"),
        Map.entry(423, "Locked"),
        Map.entry(424, "Failed Dependency"),
        Map.entry(425, "Too Early"),
        Map.entry(426, "Upgrade Required"),
        Map.entry(428, "Precondition Required"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(431, "Request Header Fields Too Large"),
        Map.entry(451, "Unavailable For Legal Reasons"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(501, "Not Implemented"),
        Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable"),
        Map.entry(504, "Gateway Timeout"),
        Map.entry(505, "HTTP Version Not Supported"),
        Map.entry(506, "Variant Also Negotiates"),
        Map.entry(507, "Insufficient Storage"),
        Map.entry(508, "Loop Detected"),
        Map.entry(510, "Not Extended"),
        Map.entry(511, "Network Authentication Required")
    );
    private static final int[]                ALL                 = concatAll();

    private final Random random;

    public HttpStatusCodeGenerator() {
        this(GeneratorConfig.defaults());
    }

    public HttpStatusCodeGenerator(GeneratorConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
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

    /**
     * Returns the reason phrase for a status code.
     *
     * @param code HTTP status code
     * @return reason phrase, or {@code "Unknown Status"} when code is unmapped
     */
    public String reasonPhrase(int code) {
        return REASON_BY_CODE.getOrDefault(code, "Unknown Status");
    }

    /**
     * Generates a reason phrase for a random status code.
     *
     * @return reason phrase
     */
    public String generateReasonPhrase() {
        return reasonPhrase(generate());
    }

    /**
     * Generates an HTTP status line (for example, {@code HTTP/1.1 404 Not Found}).
     *
     * @return status line
     */
    public String generateStatusLine() {
        int code = generate();
        return "HTTP/1.1 " + code + " " + reasonPhrase(code);
    }

    /**
     * Generates a commonly used HTTP header name.
     *
     * @return header name
     */
    public String generateHeaderName() {
        return COMMON_HEADER_NAMES[random.nextInt(COMMON_HEADER_NAMES.length)];
    }
}
