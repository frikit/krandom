package org.github.krandom.user

import org.github.krandom.exceptions.NegativeSizeException
import org.github.krandom.exceptions.SizeLimitExceedException
import org.github.krandom.user.BaseUserGenerator.propName
import org.github.krandom.user.BaseUserGenerator.propNames
import org.github.krandom.testhelper.Constants.generateValues
import org.github.krandom.testhelper.Constants.overflowUserSizeMinus
import org.github.krandom.testhelper.Constants.overflowUserSizePlus
import org.github.krandom.testhelper.Constants.userSize
import org.github.krandom.testhelper.TestLifecycle
import org.github.krandom.testhelper.TestLifecycle.onTestStart
import org.github.krandom.testhelper.UserUtils.validateName
import org.github.krandom.testhelper.UserUtils.validateNames
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object FirstNameTest : Spek({
    val logger: KLogger = KLogging().logger(FirstNameTest::class.java.simpleName)

    run {
        propName = "name"
        propNames = "names"
    }

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = FirstName()

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
                    validateNames(name)
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
                    validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames($userSize)")

        onTestStart("generate user $propNames($overflowUserSizePlus)")
        describe("generate user $propNames($overflowUserSizePlus)") {
            try {
                kRandomUser.randomDatas(overflowUserSizePlus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames($overflowUserSizePlus)")

        onTestStart("generate user $propNames($overflowUserSizeMinus)")
        describe("generate user $propNames($overflowUserSizeMinus)") {
            try {
                kRandomUser.randomDatas(overflowUserSizeMinus)
                assert(false) { "Should be runtime exception on line above" }
            } catch (see: NegativeSizeException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames($overflowUserSizeMinus)")
    }

})
