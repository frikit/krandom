package org.github.krandom.user

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils.validateAge
import org.github.krandom.testhelper.UserUtils.validateAges
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object AgeSpek : Spek({

    run {
        propName = "age"
        propNames = "ages"
    }

    describe("a user randomizer") {
        val kRandomUser = Age()

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateAge(value)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    validateAges(value)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas(userSize)
                it("should be right size ${value.size} == $userSize") {
                    assert(value.size == userSize) { "${value.size} != $userSize" }
                }
                it(" ${value[0]} should be valid $propName") {
                    validateAges(value)
                }
            }
        }

        describe("generate user $propNames($overflowUserSizePlus)") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomUser.randomDatas(overflowUserSizePlus)
                }
            }
        }

        describe("generate user $propNames($overflowUserSizeMinus)") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomUser.randomDatas(overflowUserSizeMinus)
                }
            }
        }
    }

})
