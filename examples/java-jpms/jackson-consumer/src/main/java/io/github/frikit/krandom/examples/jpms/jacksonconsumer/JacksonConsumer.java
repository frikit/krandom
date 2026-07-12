/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples.jpms.jacksonconsumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.frikit.krandom.jackson.KrandomJackson;

/** Named-module consumer proving the krandom-jackson automatic module resolves and works. */
public final class JacksonConsumer {

    private JacksonConsumer() {
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = KrandomJackson.newObjectMapper();
        String json = mapper.writeValueAsString(java.util.Map.of("module", "krandom-jackson"));
        if (!json.contains("krandom-jackson")) {
            throw new IllegalStateException("Unexpected serialization output: " + json);
        }
        System.out.println("jackson named-module consumer OK: " + json);
    }
}
