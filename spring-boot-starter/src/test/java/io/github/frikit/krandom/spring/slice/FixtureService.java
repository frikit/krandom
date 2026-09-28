/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.spring.slice;

import io.github.frikit.krandom.spring.KrandomObjectFakerFactory;
import org.springframework.stereotype.Service;

/** Application service a slice test opts into explicitly with {@code @Import}. */
@Service
public class FixtureService {

    private final KrandomObjectFakerFactory factory;

    public FixtureService(KrandomObjectFakerFactory factory) {
        this.factory = factory;
    }

    public KrandomObjectFakerFactory factory() {
        return factory;
    }
}
