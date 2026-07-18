/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import io.github.frikit.krandom.generator.Generator;
import io.github.frikit.krandom.generator.GeneratorConfig;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Generates coherent HTTP request and response fixture metadata.
 *
 * <p>This generator emits static, syntactically shaped response bodies. It does not make
 * network calls or attempt to model an HTTP server.
 */
public final class HttpFixtureGenerator implements Generator<HttpFixture> {

    private static final Map<Integer, String> REASONS = Map.ofEntries(
        Map.entry(100, "Continue"),
        Map.entry(200, "OK"),
        Map.entry(201, "Created"),
        Map.entry(202, "Accepted"),
        Map.entry(204, "No Content"),
        Map.entry(301, "Moved Permanently"),
        Map.entry(302, "Found"),
        Map.entry(304, "Not Modified"),
        Map.entry(400, "Bad Request"),
        Map.entry(401, "Unauthorized"),
        Map.entry(403, "Forbidden"),
        Map.entry(404, "Not Found"),
        Map.entry(409, "Conflict"),
        Map.entry(422, "Unprocessable Content"),
        Map.entry(429, "Too Many Requests"),
        Map.entry(500, "Internal Server Error"),
        Map.entry(502, "Bad Gateway"),
        Map.entry(503, "Service Unavailable")
    );
    private static final Integer[] STATUS_CODES = {
        100, 200, 201, 202, 204, 301, 302, 304, 400, 401, 403, 404, 409, 422, 429, 500, 502, 503
    };
    private static final String[] METHODS = { "GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS" };
    private static final String[] VERSIONS = { "HTTP/1.0", "HTTP/1.1", "HTTP/2", "HTTP/3" };
    private static final String[] REQUEST_HEADERS = {
        "Accept", "Accept-Language", "Authorization", "Content-Type", "Host", "Origin", "User-Agent"
    };
    private static final String[] RESPONSE_HEADERS = {
        "Cache-Control", "Content-Length", "Content-Type", "ETag", "Location", "Retry-After", "Vary"
    };
    private static final String[] CONTENT_TYPES = {
        "application/json", "application/xml", "text/html", "text/plain", "text/csv",
        "application/javascript", "text/css", "application/graphql", "text/markdown"
    };
    private static final String[] CONTENT_ENCODINGS = { "identity", "gzip", "br", "deflate" };
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4) AppleWebKit/605.1.15 Version/17.4 Safari/605.1.15",
        "Mozilla/5.0 (X11; Linux x86_64; rv:124.0) Gecko/20100101 Firefox/124.0"
    };

    private final Random random;

    /**
     * Creates a generator with the default configuration.
     */
    public HttpFixtureGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a generator with explicit random-source configuration.
     *
     * @param config generator configuration
     */
    public HttpFixtureGenerator(GeneratorConfig config) {
        this.random = Objects.requireNonNull(config, "config must not be null").createRandom();
    }

    @Override
    public HttpFixture generate() {
        String contentType = next(CONTENT_TYPES);
        String version = next(VERSIONS);
        return new HttpFixture(
            next(METHODS),
            version,
            status(),
            next(REQUEST_HEADERS),
            next(RESPONSE_HEADERS),
            contentType,
            next(CONTENT_ENCODINGS),
            next(USER_AGENTS),
            responseBody(contentType));
    }

    /**
     * Generates an HTTP status value.
     *
     * @return status code and reason phrase
     */
    public HttpStatus status() {
        int code = next(STATUS_CODES);
        return new HttpStatus(code, REASONS.get(code));
    }

    /**
     * Generates an HTTP version.
     *
     * @return supported HTTP version
     */
    public String httpVersion() {
        return next(VERSIONS);
    }

    /**
     * Generates a request-header name.
     *
     * @return request-header name
     */
    public String requestHeaderName() {
        return next(REQUEST_HEADERS);
    }

    /**
     * Generates a response-header name.
     *
     * @return response-header name
     */
    public String responseHeaderName() {
        return next(RESPONSE_HEADERS);
    }

    /**
     * Generates a content encoding.
     *
     * @return content encoding
     */
    public String contentEncoding() {
        return next(CONTENT_ENCODINGS);
    }

    /**
     * Generates a syntactically shaped body compatible with the supplied media type.
     *
     * @param contentType media type, optionally with parameters
     * @return fixture body
     */
    public String responseBody(String contentType) {
        Objects.requireNonNull(contentType, "contentType must not be null");
        return bodyFor(normalizeContentType(contentType));
    }

    /**
     * Checks whether a response body has the expected shape for its media type.
     *
     * @param contentType media type, optionally with parameters
     * @param body response body
     * @return whether the body has the expected fixture shape
     */
    public static boolean isBodyCompatible(String contentType, String body) {
        Objects.requireNonNull(contentType, "contentType must not be null");
        Objects.requireNonNull(body, "body must not be null");
        return switch (normalizeContentType(contentType)) {
            case "application/json" -> body.startsWith("{") && body.endsWith("}");
            case "application/xml" -> body.startsWith("<response>") && body.endsWith("</response>");
            case "text/html" -> body.startsWith("<!doctype html>");
            case "text/csv" -> body.startsWith("id,status\n");
            case "application/javascript" -> body.startsWith("export const ");
            case "text/css" -> body.startsWith(":root {");
            case "application/graphql" -> body.startsWith("query ");
            case "text/markdown" -> body.startsWith("# ");
            default -> !body.isBlank();
        };
    }

    private static String normalizeContentType(String contentType) {
        String normalized = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "application/ld+json", "application/problem+json", "application/vnd.api+json" -> "application/json";
            case "text/xml" -> "application/xml";
            default -> normalized;
        };
    }

    private String bodyFor(String contentType) {
        int id = 1 + random.nextInt(9_999);
        return switch (contentType) {
            case "application/json" -> "{\"id\":" + id + ",\"status\":\"ok\"}";
            case "application/xml" -> "<response><id>" + id + "</id><status>ok</status></response>";
            case "text/html" -> "<!doctype html><html><body><p>ok</p></body></html>";
            case "text/csv" -> "id,status\n" + id + ",ok\n";
            case "application/javascript" -> "export const responseId = " + id + ";";
            case "text/css" -> ":root { --response-id: " + id + "; }";
            case "application/graphql" -> "query Response { response(id: " + id + ") { status } }";
            case "text/markdown" -> "# Response\n\nstatus: ok\n";
            default -> "response " + id + " is ok";
        };
    }

    private <T> T next(T[] values) {
        return values[random.nextInt(values.length)];
    }
}
