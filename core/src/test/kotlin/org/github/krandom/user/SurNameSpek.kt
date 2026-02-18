package org.github.krandom.user

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils
import org.github.krandom.testhelper.UserUtils.validateName
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class SurNameSpek : DescribeSpec({

    propName = "surname"
    propNames = "surnames"

    describe("a user randomizer") {
        val kRandomUser = SurName()

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateName(value)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    UserUtils.validateNames(value)
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
                    UserUtils.validateNames(value)
                }
            }
        }

        describe("generate user $propNames(${Constants.overflowUserSizePlus})") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(Constants.overflowUserSizePlus)
                }
            }
        }

        describe("generate user $propNames(${Constants.overflowUserSizeMinus})") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(Constants.overflowUserSizeMinus)
                }
            }
        }
    }

})
