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

class UsernameSpek : DescribeSpec({

    propName = "username"
    propNames = "usernames"

    describe("a user randomizer without numbers") {
        val kRandomUser = Username(false)

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

    describe("a user randomizer with numbers") {
        val kRandomUser = Username(true)

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateName(value, true)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    UserUtils.validateNames(value, true)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas(userSize)
                assert(value.size == userSize) { "${value.size} != $userSize" }
                it(" ${value[0]} should be valid $propName") {
                    UserUtils.validateNames(value, true)
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
