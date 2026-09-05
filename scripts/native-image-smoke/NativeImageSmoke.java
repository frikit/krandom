/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.smoke;

import io.github.frikit.krandom.generator.GeneratorConfig;
import io.github.frikit.krandom.generator.Generators;
import io.github.frikit.krandom.generator.extension.KRandomModule;
import io.github.frikit.krandom.generator.extension.KRandomModuleContext;
import io.github.frikit.krandom.generator.object.ObjectGenerator;
import io.github.frikit.krandom.generator.provider.ProviderDescriptor;

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
        GeneratorConfig config = GeneratorConfig.builder().seed(42L).locale(Locale.US).install(new SmokeModule()).build();
        String name = Generators.ofFullName(config).generate();
        Person person = new ObjectGenerator<>(Person.class, config).generate();
        String extensionValue = Generators.ofProviderHub(config).get("smoke.provider", String.class);
        if (name.isBlank() || person.name().isBlank() || person.age() == 0 || !"extension".equals(extensionValue)) {
            throw new IllegalStateException("Core provider or record generation failed");
        }
        System.out.println("native-image-smoke-passed");
    }

    private record Person(String name, int age) {}

    private static final class SmokeModule implements KRandomModule {
        @Override public String id() { return "native-image-smoke"; }
        @Override public void configure(KRandomModuleContext context) {
            context.registerProvider(ProviderDescriptor.builder("smoke.provider", String.class, config -> "extension").build());
        }
    }
}
