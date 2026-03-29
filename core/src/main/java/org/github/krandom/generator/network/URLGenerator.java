/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.generator.network;

import org.github.krandom.generator.Generator;
import org.github.krandom.generator.GeneratorConfig;
import org.github.krandom.generator.file.FileExtensionGenerator;
import org.github.krandom.generator.file.FileNameGenerator;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * Generates realistic URLs with customizable protocols, paths, and query parameters.
 *
 * <p>This generator creates complete URLs by combining protocols, domains (via {@link DomainGenerator}),
 * optional paths, and optional query parameters.
 *
 * <p><strong>Supported Protocols:</strong>
 * http, https, ftp, ws, wss
 *
 * <p><strong>Basic Usage:</strong>
 * <pre>{@code
 * // Random URL
 * URLGenerator gen = new URLGenerator();
 * String url = gen.generate();  // "https://techcloud.com"
 *
 * // URL with specific protocol
 * String httpUrl = gen.generate("http");  // "http://datahub.io"
 * }</pre>
 *
 * <p><strong>URLs with Paths:</strong>
 * <pre>{@code
 * // URL with path
 * String withPath = gen.generateWithPath();  // "https://example.com/api/users"
 *
 * // URL with path and query
 * String full = gen.generateWithPathAndQuery();  // "https://example.com/api/users?id=123&amp;page=1"
 * }</pre>
 *
 * <p><strong>Locale-Aware Generation:</strong>
 * <pre>{@code
 * URLGenerator deGen = new URLGenerator(Locale.GERMANY);
 * String deUrl = deGen.generate();  // Might include ".de" domain
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong>
 * This generator is thread-safe and can be shared across threads.
 */
public final class URLGenerator implements Generator<String> {

    private static final String[] PROTOCOLS = {
        "http", "https", "ftp", "ws", "wss"
    };

    private static final String[] PATH_SEGMENTS = {
        "api", "v1", "v2", "admin", "user", "users", "posts", "data",
        "files", "images", "docs", "about", "contact", "services",
        "products", "items", "list", "details", "profile", "settings"
    };

    private static final String[] QUERY_PARAMS = {
        "id", "page", "limit", "offset", "sort", "order", "filter",
        "search", "q", "type", "category", "status", "format"
    };

    private final GeneratorConfig        config;
    private final Random                 random;
    private final DomainGenerator        domainGenerator;
    private final FileExtensionGenerator fileExtensionGenerator;
    private final FileNameGenerator      fileNameGenerator;

    /**
     * Creates a URL generator with default configuration.
     */
    public URLGenerator() {
        this(GeneratorConfig.defaults());
    }

    /**
     * Creates a URL generator for the specified locale.
     *
     * @param locale the locale for domain generation
     */
    public URLGenerator(Locale locale) {
        this(GeneratorConfig.builder().locale(locale).build());
    }

    /**
     * Creates a URL generator with the specified configuration.
     *
     * @param config the generator configuration; must not be {@code null}
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public URLGenerator(GeneratorConfig config) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.random = config.createRandom();
        this.domainGenerator = new DomainGenerator(config);
        this.fileExtensionGenerator = new FileExtensionGenerator(config);
        this.fileNameGenerator = new FileNameGenerator(config);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    private static String normalizeOptional(String value, String field) {
        if (value == null) {
            return null;
        }
        return requireNonBlank(value, field);
    }

    private static String normalizePath(String path) {
        String normalized = path.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private static String appendPathSegment(String url, String segment) {
        if (url.endsWith("/")) {
            return url + segment;
        }
        return url + "/" + segment;
    }

    private static String encodeUrlComponent(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates a URL with random protocol and domain (no path or query).
     *
     * @return a URL; never {@code null}
     */
    @Override
    public String generate() {
        String protocol = getRandomProtocol();
        String domain = domainGenerator.generate();
        return protocol + "://" + domain;
    }

    /**
     * Generates a URL with the specified protocol.
     *
     * @param protocol the protocol (e.g., "http", "https"); must not be {@code null}
     * @return a URL with the specified protocol; never {@code null}
     * @throws NullPointerException if {@code protocol} is {@code null}
     */
    public String generate(String protocol) {
        Objects.requireNonNull(protocol, "protocol must not be null");
        String domain = domainGenerator.generate();
        return protocol + "://" + domain;
    }

    /**
     * Generates a URL with a random path (1-3 segments).
     *
     * @return a URL with path; never {@code null}
     */
    public String generateWithPath() {
        String base = generate();
        String path = generatePath();
        return base + path;
    }

    /**
     * Generates a URL with a path and query parameters.
     *
     * @return a URL with path and query; never {@code null}
     */
    public String generateWithPathAndQuery() {
        String base = generateWithPath();
        String query = generateQueryString();
        return base + query;
    }

    /**
     * Generates a URL.
     * Bogus-style alias: {@code url()}.
     *
     * @return URL string
     */
    public String generateUrl() {
        return generate();
    }

    /**
     * Generates a URL with path.
     * Bogus-style alias: {@code urlWithPath()}.
     *
     * @return URL with path
     */
    public String generateUrlWithPath() {
        return generateWithPath();
    }

    /**
     * Generates a URL using a fixed domain.
     *
     * @param domain domain (e.g. {@code "example.com"}); must not be null/blank
     * @return URL with the given domain
     */
    public String generateWithDomain(String domain) {
        return generateWithOptions(new URLOptions(null, requireNonBlank(domain, "domain"), null, null, null, false));
    }

    /**
     * Generates a URL with a domain prefix/subdomain.
     *
     * @param domainPrefix prefix (e.g. {@code "api"}); must not be null/blank
     * @return URL containing the generated domain prefixed as {@code <prefix>.<domain>}
     */
    public String generateWithDomainPrefix(String domainPrefix) {
        return generateWithOptions(new URLOptions(null, null, requireNonBlank(domainPrefix, "domainPrefix"), null, null, false));
    }

    /**
     * Generates a URL with a fixed path.
     *
     * @param path path (e.g. {@code "/api/v1"} or {@code "api/v1"}); must not be null/blank
     * @return URL containing the fixed path
     */
    public String generateWithFixedPath(String path) {
        return generateWithOptions(new URLOptions(null, null, null, requireNonBlank(path, "path"), null, false));
    }

    /**
     * Generates a URL with a random file extension chosen from the given list.
     *
     * @param extensions non-null, non-empty extension list (e.g. {@code "gif"}, {@code "jpg"})
     * @return URL ending with one of the provided file extensions
     */
    public String generateWithExtensions(String... extensions) {
        return generateWithOptions(new URLOptions(null, null, null, null, extensions, false));
    }

    /**
     * Generates a URL with composable options similar to Chance.js {@code url({...})}.
     *
     * @param options option bag; must not be {@code null}
     * @return generated URL
     */
    public String generateWithOptions(URLOptions options) {
        Objects.requireNonNull(options, "options must not be null");

        String protocol = options.protocol() == null ? getRandomProtocol() : requireNonBlank(options.protocol(), "protocol");
        String domain = options.domain() == null ? domainGenerator.generate() : requireNonBlank(options.domain(), "domain");
        String prefix = normalizeOptional(options.domainPrefix(), "domainPrefix");
        String effectiveDomain = prefix == null ? domain : prefix + "." + domain;

        String url = protocol + "://" + effectiveDomain;

        String path = options.path() == null ? null : normalizePath(requireNonBlank(options.path(), "path"));
        if (path != null) {
            url += path;
        } else if (options.withQuery()) {
            // query generation requires a path, keep previous URL style
            url += generatePath();
        }

        String[] exts = options.extensions();
        if (exts != null) {
            String extension = fileExtensionGenerator.generateFrom(exts);
            String fileName = fileNameGenerator.generateWithExtension(extension);
            String encodedFileName = encodeUrlComponent(fileName);
            url = appendPathSegment(url, encodedFileName);
        }

        if (options.withQuery()) {
            url += generateQueryString();
        }
        return url;
    }

    /**
     * Generates just a protocol.
     *
     * @return a protocol (e.g., "https"); never {@code null}
     */
    public String getProtocol() {
        return getRandomProtocol();
    }

    /**
     * Generates a protocol token.
     * Bogus-style alias: {@code protocol()}.
     *
     * @return protocol string
     */
    public String generateProtocol() {
        return getProtocol();
    }

    /**
     * Generates just a path (without leading domain).
     *
     * @return a path starting with "/" (e.g., "/api/users"); never {@code null}
     */
    public String getPath() {
        return generatePath();
    }

    /**
     * Generates just a query string (without leading "?").
     *
     * @return a query string (e.g., "id=123&amp;page=1"); never {@code null}
     */
    public String getQueryString() {
        return generateQueryString().substring(1); // Remove leading "?"
    }

    /**
     * Generates a query-string fragment.
     * Mimesis-style alias for {@link #getQueryString()}.
     *
     * @return query string without leading {@code ?}
     */
    public String generateQuery() {
        return getQueryString();
    }

    /**
     * Generates a TLD token using the underlying domain generator.
     *
     * @return top-level domain token (for example {@code "com"})
     */
    public String generateTld() {
        return domainGenerator.getTLD();
    }

    /**
     * Returns a random protocol.
     *
     * @return a protocol
     */
    private String getRandomProtocol() {
        return PROTOCOLS[random.nextInt(PROTOCOLS.length)];
    }

    /**
     * Generates a path with 1-3 segments.
     *
     * @return a path starting with "/"
     */
    private String generatePath() {
        int segments = 1 + random.nextInt(3); // 1-3 segments
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < segments; i++) {
            path.append("/").append(getRandomPathSegment());
        }
        return path.toString();
    }

    /**
     * Generates a query string with 1-3 parameters.
     *
     * @return a query string starting with "?"
     */
    private String generateQueryString() {
        int params = 1 + random.nextInt(3); // 1-3 parameters
        StringBuilder query = new StringBuilder("?");
        for (int i = 0; i < params; i++) {
            if (i > 0) {
                query.append("&");
            }
            query.append(getRandomQueryParam())
                 .append("=")
                 .append(random.nextInt(1000));
        }
        return query.toString();
    }

    /**
     * Returns a random path segment.
     *
     * @return a path segment
     */
    private String getRandomPathSegment() {
        return PATH_SEGMENTS[random.nextInt(PATH_SEGMENTS.length)];
    }

    /**
     * Returns a random query parameter name.
     *
     * @return a query parameter name
     */
    private String getRandomQueryParam() {
        return QUERY_PARAMS[random.nextInt(QUERY_PARAMS.length)];
    }

    /**
     * Option bag for {@link #generateWithOptions(URLOptions)}.
     *
     * @param protocol     optional protocol
     * @param domain       optional fixed domain
     * @param domainPrefix optional subdomain prefix
     * @param path         optional fixed path
     * @param extensions   optional extension list for random file extension selection
     * @param withQuery    whether query parameters should be appended
     */
    public record URLOptions(
        String protocol,
        String domain,
        String domainPrefix,
        String path,
        String[] extensions,
        boolean withQuery
    ) {

        /**
         * Convenience options with all optional fields unset and query disabled.
         */
        public URLOptions() {
            this(null, null, null, null, null, false);
        }

        /**
         * Returns default extension candidates used by URL generation helpers.
         *
         * @return extension candidates
         */
        public static String[] defaultExtensions() {
            return FileExtensionGenerator.defaultExtensions();
        }
    }
}
