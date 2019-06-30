package krandom.user

import krandom.KRandomUser
import krandom.utils.Constants.generateValues
import krandom.utils.Constants.userSize
import krandom.utils.TestLifecycle
import krandom.utils.UserUtils.validateName
import krandom.utils.UserUtils.validateNames
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe


object FirstNameTest : Spek({
    val logger: KLogger = KLogging().logger(FirstNameTest::class.java.simpleName)

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = FirstName()

        TestLifecycle.onTestStart("generate user name")
        describe("generate user name") {
            (1..generateValues).forEach {
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                it(" $name should be valid name") {
                    validateName(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user name")

        TestLifecycle.onTestStart("generate user names")
        describe("generate user names") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user names")
                it(" ${name.size} all should be valid name") {
                    validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user names")

        TestLifecycle.onTestStart("generate user names($userSize)")
        describe("generate user names($userSize)") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(userSize)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == userSize)
                it(" ${name[0]} should be valid name") {
                    validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user names($userSize)")
    }

})
