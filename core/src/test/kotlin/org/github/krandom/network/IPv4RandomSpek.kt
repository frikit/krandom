/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package org.github.krandom.network

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isValidIP
import io.kotest.core.spec.style.DescribeSpec

class IPv4RandomSpek : DescribeSpec({

    describe("a random ip v4") {
        (1..Constants.generateValues).forEach {
            val ips = listOf(IPv4Random.random())
            it("$ips should be valid") {
                isValidIP(ips)
            }
        }
    }

    describe("a multiple random ip v4") {
        (1..Constants.generateValues).forEach {
            val ips = IPv4Random.random(10)
            it("$ips should be valid") {
                isValidIP(ips)
            }
        }
    }

})
