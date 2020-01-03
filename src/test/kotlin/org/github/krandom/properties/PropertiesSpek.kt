package org.github.krandom.properties

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object PropertiesSpek : Spek({

    val expectedFields = listOf(
            Pair("MAX_DOUBLE", Double.MAX_VALUE),
            Pair("MIN_DOUBLE", Double.MIN_VALUE),
            Pair("MAX_FLOAT", Float.MAX_VALUE),
            Pair("MIN_FLOAT", Float.MIN_VALUE),
            Pair("MAX_LONG", Long.MAX_VALUE),
            Pair("MIN_LONG", Long.MIN_VALUE),
            Pair("MAX_INT", Int.MAX_VALUE),
            Pair("MIN_INT", Int.MIN_VALUE)
    )

    val currentFields = listOf(
            Pair("MAX_DOUBLE", Properties.MAX_DOUBLE),
            Pair("MIN_DOUBLE", Properties.MIN_DOUBLE),
            Pair("MAX_FLOAT", Properties.MAX_FLOAT),
            Pair("MIN_FLOAT", Properties.MIN_FLOAT),
            Pair("MAX_LONG", Properties.MAX_LONG),
            Pair("MIN_LONG", Properties.MIN_LONG),
            Pair("MAX_INT", Properties.MAX_INT),
            Pair("MIN_INT", Properties.MIN_INT)
    )

    describe("a properties") {
        it("should be ${expectedFields.size} props") {
            assert(currentFields.size == expectedFields.size) { "Should be [${expectedFields.size}] != [${currentFields.size}]" }
        }

        repeat(expectedFields.size) {
            val expectedValue = expectedFields[it]
            val currentValue = currentFields[it]
            it("key should be the same [${expectedValue.first}]") {
                assert(expectedValue.first == currentValue.first) { "${expectedValue.first} == ${currentValue.first}" }
            }
            it("value should be the same [${expectedValue.second}]") {
                assert(expectedValue.second == currentValue.second) { "${expectedValue.second} == ${currentValue.second}" }
            }
        }
    }
})
