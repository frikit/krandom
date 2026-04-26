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

    public IPv4Generator ipv4() { return new IPv4Generator(); }

    public IPv6Generator ipv6() { return new IPv6Generator(); }

    public IPGenerator ip() { return new IPGenerator(); }

    public MacAddressGenerator macAddress() { return new MacAddressGenerator(); }

    public DomainGenerator domain() { return new DomainGenerator(); }

    public HostnameGenerator hostname() { return new HostnameGenerator(); }

    public URLGenerator url() { return new URLGenerator(); }

    public UriGenerator uri() { return new UriGenerator(); }

    public PortGenerator port() { return new PortGenerator(); }

    public SlugGenerator slug() { return new SlugGenerator(); }

    public UserAgentGenerator userAgent() { return new UserAgentGenerator(); }

    public HttpStatusCodeGenerator httpStatusCode() { return new HttpStatusCodeGenerator(); }

    public HttpMethodGenerator httpMethod() { return new HttpMethodGenerator(); }
}
