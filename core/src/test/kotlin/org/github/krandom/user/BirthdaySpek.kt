package org.github.krandom.user

import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.DateUtils.newDate
import org.github.krandom.testhelper.DateUtils.validateBirthDay
import org.github.krandom.testhelper.DateUtils.validateBirthDays
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.github.krandom.user.enum.AgeGroup
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.assertions.throwables.shouldThrow

class BirthdaySpek : DescribeSpec({

    propName = "birthday"
    propNames = "birthdays"

    val customRangeOfYears = listOf(
            Pair(2000, 2100),
            Pair(1900, 1800)
    )

    customRangeOfYears.forEach { yearPair ->
        describe("a user randomizer custom range $yearPair") {
            val start = newDate(yearPair.first, 1, 1)
            val end = newDate(yearPair.second, 1, 1)
            val expectedStart = if (start.before(end)) start else end
            val expectedEnd = if (start.before(end)) end else start

            val kRandomUser = BirthDay(start to end)

            describe("generate user $propName") {
                (1..generateValues).forEach { _ ->
                    val value = kRandomUser.randomData()
                    it(" $value should be valid $propName") {
                        validateBirthDay(value, expectedStart, expectedEnd)
                    }
                }
            }

            describe("generate user $propNames") {
                (1..generateValues).forEach { _ ->
                    val value = kRandomUser.randomDatas()
                    it(" ${value.size} all should be valid $propName") {
                        validateBirthDays(value, expectedStart, expectedEnd)
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
                        validateBirthDays(value, expectedStart, expectedEnd)
                    }
                }
            }
        }
    }

    describe("a user randomizer age groups") {
        AgeGroup.values().forEach {
            val start = it.getBirthdayRange().first
            val end = it.getBirthdayRange().second
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
        }
    }

    describe("a user randomizer exceptions") {
        val start = newDate(2000, 1, 1)
        val end = newDate(2020, 1, 1)
        val kRandomUser = BirthDay(start to end)

        describe("generate user $propNames(start date == end date)") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    BirthDay(newDate() to newDate())
                }
            }
        }

        describe("generate user $propNames($overflowUserSizePlus)") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(overflowUserSizePlus)
                }
            }
        }

        describe("generate user $propNames($overflowUserSizeMinus)") {
            it("should throw exception") {
                shouldThrow<IllegalArgumentException> {
                    kRandomUser.randomDatas(overflowUserSizeMinus)
                }
            }
        }
    }

})
