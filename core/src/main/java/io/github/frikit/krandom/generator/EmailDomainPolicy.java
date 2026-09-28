/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.generator;

/**
 * Safety policy for the domains of generated email addresses.
 *
 * <p>Realistic names combined with popular mailbox-provider domains can form deliverable addresses
 * of real people. The default therefore uses only domains that are reserved for documentation and
 * testing and can never reach a real mailbox. The policy governs domains that a generator chooses on
 * its own; a domain passed explicitly by the caller is always used verbatim.
 */
public enum EmailDomainPolicy {

    /**
     * Uses only reserved domains: the RFC 2606 second-level domains {@code example.com},
     * {@code example.net}, and {@code example.org} for personal addresses, and the RFC 2606 /
     * RFC 6761 {@code .test} top-level domain for company addresses. Methods whose purpose is a
     * real free-mail provider domain fail closed under this policy.
     */
    TEST_SAFE_RESERVED_DOMAINS,

    /**
     * Restores the prior popular mailbox-provider and top-level domains (for example
     * {@code gmail.com}) without a safety claim. Select it only for isolated fixtures that never
     * send mail.
     */
    REALISTIC_UNCLASSIFIED
}
