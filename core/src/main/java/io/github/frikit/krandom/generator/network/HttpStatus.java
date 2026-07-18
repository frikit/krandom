/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An HTTP status code and its reason phrase.
 *
 * @param code         status code in the HTTP range
 * @param reasonPhrase human-readable reason phrase
 */
public record HttpStatus(int code, String reasonPhrase) {

    private static final Pattern HTTP_VERSION = Pattern.compile("HTTP/(1\\.0|1\\.1|2|3)");

    /**
     * Validates a status value.
     */
    public HttpStatus {
        if (code < 100 || code > 599) {
            throw new IllegalArgumentException("code must be in [100,599], got: " + code);
        }
        Objects.requireNonNull(reasonPhrase, "reasonPhrase must not be null");
        if (reasonPhrase.isBlank()) {
            throw new IllegalArgumentException("reasonPhrase must not be blank");
        }
    }

    /**
     * Returns a complete HTTP status line using the supplied HTTP version.
     *
     * @param version a supported HTTP version
     * @return status line
     */
    public String statusLine(String version) {
        Objects.requireNonNull(version, "version must not be null");
        if (!HTTP_VERSION.matcher(version).matches()) {
            throw new IllegalArgumentException("version must be HTTP/1.0, HTTP/1.1, HTTP/2, or HTTP/3, got: " + version);
        }
        return version + ' ' + code + ' ' + reasonPhrase;
    }
}
