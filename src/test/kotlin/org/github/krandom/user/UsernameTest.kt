package org.github.krandom.user

import org.github.krandom.exceptions.NegativeSizeException
import org.github.krandom.exceptions.SizeLimitExceedException
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.github.krandom.testhelper.Constants
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.TestLifecycle
import org.github.krandom.testhelper.TestLifecycle.onTestStart
import org.github.krandom.testhelper.UserUtils
import org.github.krandom.testhelper.UserUtils.validateName
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object UsernameTest : Spek({
    val logger: KLogger = KLogging().logger(UsernameTest::class.java.simpleName)

    run {
        propName = "username"
        propNames = "usernames"
    }

    describe("a user randomizer without numbers") {
        val kRandomUser: KRandomUser<String> = Username(false)

        onTestStart("generate user $propName")
        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                it(" $name should be valid $propName") {
                    validateName(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propName")

        onTestStart("generate user $propNames")
        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user $propNames")
                it(" ${name.size} all should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames")

        onTestStart("generate user $propNames($userSize)")
        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas(userSize)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == userSize) { "${name.size} != $userSize" }
                it(" ${name[0]} should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames($userSize)")

        onTestStart("generate user $propNames(${Constants.overflowUserSizePlus})")
        describe("generate user $propNames(${Constants.overflowUserSizePlus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizePlus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames(${Constants.overflowUserSizePlus})")

        onTestStart("generate user $propNames(${Constants.overflowUserSizeMinus})")
        describe("generate user $propNames(${Constants.overflowUserSizeMinus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizeMinus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: NegativeSizeException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames(${Constants.overflowUserSizeMinus})")
    }

    describe("a user randomizer with numbers") {
        val kRandomUser: KRandomUser<String> = Username(true)

        onTestStart("generate user $propName")
        describe("generate user $propName") {
            (1..generateValues).forEach { _ ->
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                it(" $name should be valid $propName") {
                    validateName(name, true)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propName")

        onTestStart("generate user $propNames")
        describe("generate user $propNames") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user $propNames")
                it(" ${name.size} all should be valid name") {
                    UserUtils.validateNames(name, true)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames")

        onTestStart("generate user $propNames($userSize)")
        describe("generate user $propNames($userSize)") {
            (1..generateValues).forEach { _ ->
                val name: List<String> = kRandomUser.randomDatas(userSize)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == userSize) { "${name.size} != $userSize" }
                it(" ${name[0]} should be valid name") {
                    UserUtils.validateNames(name, true)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames($userSize)")

        onTestStart("generate user $propNames(${Constants.overflowUserSizePlus})")
        describe("generate user $propNames(${Constants.overflowUserSizePlus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizePlus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames(${Constants.overflowUserSizePlus})")

        onTestStart("generate user $propNames(${Constants.overflowUserSizeMinus})")
        describe("generate user $propNames(${Constants.overflowUserSizeMinus})") {
            try {
                kRandomUser.randomDatas(Constants.overflowUserSizeMinus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: NegativeSizeException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames(${Constants.overflowUserSizeMinus})")
    }

})
