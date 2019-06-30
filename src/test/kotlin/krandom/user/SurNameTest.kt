package krandom.user

import krandom.KRandomUser
import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import krandom.utils.UserUtils
import krandom.utils.Constants.userSize
import krandom.utils.UserUtils.validateName
import mu.KLogger
import mu.KLogging
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SurNameTest : Spek({
    val logger: KLogger = KLogging().logger(SurNameTest::class.java.simpleName)

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = SurName()

        TestLifecycle.onTestStart("generate user surname")
        describe("generate user surname") {
            (1..generateValues).forEach {
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                it(" $name should be valid name") {
                    validateName(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user surname")

        TestLifecycle.onTestStart("generate user surnames")
        describe("generate user surnames") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user surnames")
                it(" ${name.size} all should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user surnames")

        TestLifecycle.onTestStart("generate user surnames($userSize)")
        describe("generate user surnames($userSize)") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(userSize)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == 10)
                it(" ${name[0]} should be valid name") {
                    UserUtils.validateNames(name)
                }
            }
        }
        TestLifecycle.onTestFinish("generate user surnames($userSize)")
    }

})
