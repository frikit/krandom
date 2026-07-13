/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.jpms.junitconsumer;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.junit.KrandomExtension;

/** Named-module consumer proving the krandom-junit automatic module resolves and works. */
public final class JunitConsumer {

    private JunitConsumer() {
    }

    public static void main(String[] args) {
        KrandomExtension extension = new KrandomExtension();
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).build();
        if (!"krandom.seed".equals(KrandomExtension.REPORT_ENTRY_KEY) || config.getSeed().isEmpty()) {
            throw new IllegalStateException("Unexpected extension contract: " + extension);
        }
        System.out.println("junit named-module consumer OK: " + KrandomExtension.RECIPE_REPORT_ENTRY_KEY);
    }
}
