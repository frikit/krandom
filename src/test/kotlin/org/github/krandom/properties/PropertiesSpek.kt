package org.github.krandom.properties

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.reflect.full.declaredMemberProperties

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
    ).sortedWith(compareBy { it.first })


    describe("get fields from object") {
        val fields = Properties::class.declaredMemberProperties
                .map { it.name to it.get(Properties) }
                .sortedWith(compareBy { it.first })

        it("should be ${expectedFields.size} props") {
            assert(fields.size == expectedFields.size) { "Should be [${expectedFields.size}] != [${fields.size}]" }
        }

        repeat(expectedFields.size) {
            val expectedValue = expectedFields[it]
            val currentValue = fields[it]
            it("key should be the same [${expectedValue.first}]") {
                assert(expectedValue.first == currentValue.first) { "${expectedValue.first} == ${currentValue.first}" }
            }
            it("value should be the same [${expectedValue.second}]") {
                assert(expectedValue.second == currentValue.second) { "${expectedValue.second} == ${currentValue.second}" }
            }
        }
    }
})
