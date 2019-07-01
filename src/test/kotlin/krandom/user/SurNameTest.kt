package krandom.user

import krandom.KRandomUser
import krandom.exceptions.SizeLimitExceedException
import krandom.user.BaseUserGenerator.propName
import krandom.user.BaseUserGenerator.propNames
import krandom.utils.Constants
import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import krandom.utils.UserUtils
import krandom.utils.Constants.userSize
import krandom.utils.TestLifecycle.onTestStart
import krandom.utils.UserUtils.validateName
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SurNameTest : Spek({
    val logger: KLogger = KLogging().logger(SurNameTest::class.java.simpleName)

    run {
        propName = "surname"
        propNames = "surnames"
    }

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = SurName()

        onTestStart("generate user $propName")
        describe("generate user $propName") {
            (1..generateValues).forEach {
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
            (1..generateValues).forEach {
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
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(userSize)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == userSize)
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
            } catch (see: SizeLimitExceedException) {
                assert(true) { "Exception should throw" }
            }
        }
        TestLifecycle.onTestFinish("generate user $propNames(${Constants.overflowUserSizeMinus})")
    }

})
