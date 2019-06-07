package krandom.user

import krandom.KRandomUser
import krandom.common.RandomizerSpek
import krandom.utils.Constants.generateValues
import krandom.utils.TestLifecycle
import krandom.utils.UserUtils.validateName
import mu.KLogger
import mu.KLogging
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.on

class SurNameTest : Spek({
    val logger: KLogger = KLogging().logger(RandomizerSpek::class.java.simpleName)

    describe("a user randomizer") {
        val kRandomUser: KRandomUser<String> = SurName()

        TestLifecycle.onTestStart("generate user name")
        on("generate user name") {
            (1..generateValues).forEach {
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                validateName(name)
            }
        }
        TestLifecycle.onTestFinish("generate user name")

        TestLifecycle.onTestStart("generate user names")
        on("generate user names") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user names")
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user names")

        TestLifecycle.onTestStart("generate user names(10)")
        on("generate user names(10)") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(10)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == 10)
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user names(10)")
    }

})