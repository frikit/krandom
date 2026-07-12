/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
module io.github.frikit.krandom.examples.jpms.jackson {
    // Automatic modules do not declare their dependencies; Gradle needs the explicit
    // core requirement to place krandom-core on the module path.
    requires io.github.frikit.krandom.jackson;
    requires io.github.frikit.krandom;
    requires com.fasterxml.jackson.databind;
}
