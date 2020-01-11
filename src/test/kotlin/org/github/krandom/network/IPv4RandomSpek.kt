package org.github.krandom.network

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.isValidIP
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object IPv4RandomSpek : Spek({

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
