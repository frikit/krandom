/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator.network;

/**
 * Safety policy for generated network destinations: IP addresses, CIDRs, domains, hostnames, URLs,
 * URIs, company URLs, avatar URLs, and social links.
 *
 * <p>A random public address or an ordinary domain can belong to a real host. The policy governs
 * destinations that a generator chooses on its own; a destination passed explicitly by the caller,
 * such as the TLD in {@code generate("com")}, is always used verbatim. A supplied username, subdomain,
 * scheme, or size does not choose a destination.
 */
public enum NetworkSafetyPolicy {

    /**
     * Uses only reserved fixture destinations: the RFC 5737 documentation blocks for IPv4, the RFC 3849
     * documentation prefix for IPv6, and names under the RFC 2606 / RFC 6761 {@code .test} top-level
     * domain. MAC addresses are locally administered and unicast. Methods whose purpose is a public or
     * private address fail closed. This reserves the destination; it does not isolate the transport,
     * since a network may still resolve {@code .test} privately.
     */
    TEST_SAFE_RESERVED,

    /**
     * Restores random public addresses and realistic domains without a safety claim. Select it only for
     * isolated fixtures that never connect to what they generate.
     */
    REALISTIC_UNCLASSIFIED
}
