/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.namespace;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.network.*;

/**
 * Fluent namespace for network-related generators.
 *
 * <p>Usage: {@code Generators.network().ipv4().generate()}
 */
public final class NetworkGenerators {

    private final GeneratorConfig config;

    public NetworkGenerators() {
        this(GeneratorConfig.builder().build());
    }

    public NetworkGenerators(GeneratorConfig config) {
        this.config = config;
    }

    public IPv4Generator ipv4() { return new IPv4Generator(config); }

    public IPv6Generator ipv6() { return new IPv6Generator(config); }

    public IPGenerator ip() { return new IPGenerator(config); }

    public MacAddressGenerator macAddress() { return new MacAddressGenerator(config); }

    public DomainGenerator domain() { return new DomainGenerator(config); }

    public HostnameGenerator hostname() { return new HostnameGenerator(config); }

    public URLGenerator url() { return new URLGenerator(config); }

    public UriGenerator uri() { return new UriGenerator(config); }

    public PortGenerator port() { return new PortGenerator(config); }

    public SlugGenerator slug() { return new SlugGenerator(config); }

    public UserAgentGenerator userAgent() { return new UserAgentGenerator(config); }

    public HttpStatusCodeGenerator httpStatusCode() { return new HttpStatusCodeGenerator(config); }

    public HttpMethodGenerator httpMethod() { return new HttpMethodGenerator(config); }
}
