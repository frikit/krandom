package org.github.krandom.user

import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.UserUtils
import org.github.krandom.testhelper.UserUtils.validateName
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertFailsWith

object SurNameTest : Spek({

    run {
        propName = "surname"
        propNames = "surnames"
    }

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = SurName()

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val name: String = kRandomUser.randomData()
                it(" $name should be valid $propName") {
                    validateName(name)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas()
                it(" ${name.size} all should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }

        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas(userSize)
                it("should be right size ${name.size} == $userSize") {
                    assert(name.size == userSize) { "${name.size} != $userSize" }
                }
                it(" ${name[0]} should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }

        describe("generate user $propNames(${Constants.overflowUserSizePlus})") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomUser.randomDatas(Constants.overflowUserSizePlus)
                }
            }
        }

        describe("generate user $propNames(${Constants.overflowUserSizeMinus})") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    kRandomUser.randomDatas(Constants.overflowUserSizeMinus)
                }
            }
        }
    }

})
