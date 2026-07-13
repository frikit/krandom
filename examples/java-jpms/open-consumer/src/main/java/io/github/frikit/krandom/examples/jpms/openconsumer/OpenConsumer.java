/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.jpms.openconsumer;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.object.ObjectGenerator;

/** Verifies reflective construction from a package opened to the kRandom automatic module. */
public final class OpenConsumer {

    private OpenConsumer() {}

    /** Runs the executable consumer contract. */
    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder()
                                                .objectOverride(String.class, () -> "jpms-value")
                                                .build();

        PrivateFixture fixture = new ObjectGenerator<>(PrivateFixture.class, config).generate();
        if (!fixture.constructorRan || !"jpms-value".equals(fixture.constructorValue)
            || !"jpms-value".equals(fixture.mutableValue)) {
            throw new AssertionError("Qualified opens did not permit safe reflective construction");
        }
    }

    static final class PrivateFixture {

        private final boolean constructorRan;
        private final String  constructorValue;
        private       String  mutableValue;

        private PrivateFixture(String constructorValue) {
            this.constructorRan = true;
            this.constructorValue = constructorValue;
        }
    }
}
