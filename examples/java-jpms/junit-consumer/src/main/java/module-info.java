/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
module io.github.frikit.krandom.examples.jpms.junit {
    // Automatic modules do not declare their dependencies; the consumer states the
    // core and JUnit API requirements explicitly.
    requires io.github.frikit.krandom.junit;
    requires io.github.frikit.krandom;
    requires org.junit.jupiter.api;
}
