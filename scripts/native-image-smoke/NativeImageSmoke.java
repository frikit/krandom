/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.smoke;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.object.ObjectGenerator;

import java.util.Locale;

/**
 * Representative core fixture used by {@code verify_native_image.sh}.
 */
public final class NativeImageSmoke {

    private NativeImageSmoke() {
    }

    /**
     * Runs a deterministic core generator.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).locale(Locale.US).build();
        String name = Generators.ofFullName(config).generate();
        Person person = new ObjectGenerator<>(Person.class, config).generate();
        if (name.isBlank() || person.name().isBlank() || person.age() == 0) {
            throw new IllegalStateException("Core provider or record generation failed");
        }
        System.out.println("native-image-smoke-passed");
    }

    private record Person(String name, int age) {}
}
