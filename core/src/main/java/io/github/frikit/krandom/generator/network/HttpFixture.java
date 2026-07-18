/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import java.util.Objects;

/**
 * A coherent set of HTTP fixture values suitable for tests and examples.
 *
 * @param method             request method
 * @param version            HTTP protocol version
 * @param status             response status
 * @param requestHeaderName  request header name
 * @param responseHeaderName response header name
 * @param contentType        response content type
 * @param contentEncoding    response content encoding
 * @param userAgent          request user agent
 * @param responseBody       response body compatible with {@code contentType}
 */
public record HttpFixture(String method,
                          String version,
                          HttpStatus status,
                          String requestHeaderName,
                          String responseHeaderName,
                          String contentType,
                          String contentEncoding,
                          String userAgent,
                          String responseBody) {

    /**
     * Validates that every fixture component is usable text.
     */
    public HttpFixture {
        requireText(method, "method");
        requireText(version, "version");
        Objects.requireNonNull(status, "status must not be null");
        requireText(requestHeaderName, "requestHeaderName");
        requireText(responseHeaderName, "responseHeaderName");
        requireText(contentType, "contentType");
        requireText(contentEncoding, "contentEncoding");
        requireText(userAgent, "userAgent");
        requireText(responseBody, "responseBody");
    }

    /**
     * Returns the status formatted with this fixture's HTTP version.
     *
     * @return HTTP status line
     */
    public String statusLine() {
        return status.statusLine(version);
    }

    private static void requireText(String value, String name) {
        Objects.requireNonNull(value, name + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
