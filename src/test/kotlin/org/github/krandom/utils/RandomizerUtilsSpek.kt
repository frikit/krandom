package org.github.krandom.utils

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object RandomizerUtilsSpek : Spek({

    describe("a loop string generator function") {
        it("should be empty") {
            assert(generateRandomString(function = { "" }, numbers = true) == "")
        }
    }
})
