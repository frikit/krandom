package org.github.krandom.user

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.DateUtils.validateBirthDay
import org.github.krandom.testhelper.DateUtils.validateBirthDays
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*
import kotlin.test.assertFailsWith

object BirthdaySpek : Spek({

    run {
        propName = "birthday"
        propNames = "birthdays"
    }

    describe("a user randomizer") {
        val start = Date(2000, 1, 1)
        val end = Date(2020, 1, 1)
        val kRandomUser = BirthDay(start to end)

        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomData()
                it(" $value should be valid $propName") {
                    validateBirthDay(value, start, end)
                }
            }
        }

        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val value = kRandomUser.randomDatas()
                it(" ${value.size} all should be valid $propName") {
                    validateBirthDays(value, start, end)
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
                    validateBirthDays(value, start, end)
                }
            }
        }

        describe("generate user $propNames(start date == end date)") {
            it("should throw exception") {
                assertFailsWith(IllegalArgumentException::class, "should throw illegal argument exception") {
                    BirthDay(Date() to Date())
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
