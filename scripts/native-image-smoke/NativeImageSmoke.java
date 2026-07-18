/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.smoke;

import io.github.frikit.krandom.generator.base.IntGenerator;

/**
 * Small, dependency-free core fixture used by {@code verify_native_image.sh}.
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
        System.out.println(new IntGenerator(1, 9, 42L).generate());
    }
}
