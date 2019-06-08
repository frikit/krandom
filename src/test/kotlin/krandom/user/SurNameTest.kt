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

        TestLifecycle.onTestStart("generate user surname")
        on("generate user surname") {
            (1..generateValues).forEach {
                val name: String = kRandomUser.randomData()
                TestLifecycle.onTestStep(logger, "generated : [$name]")
                validateName(name)
            }
        }
        TestLifecycle.onTestFinish("generate user surname")

        TestLifecycle.onTestStart("generate user surnames")
        on("generate user surnames") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas()
                TestLifecycle.onTestStep(logger, "generated : [${name.size}] user surnames")
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user surnames")

        TestLifecycle.onTestStart("generate user surnames(10)")
        on("generate user surnames(10)") {
            (1..generateValues).forEach {
                val name: List<String> = kRandomUser.randomDatas(10)
                TestLifecycle.onTestStep(logger, "generated : [${name}]")
                assert(name.size == 10)
                name.forEach { validateName(it) }
            }
        }
        TestLifecycle.onTestFinish("generate user surnames(10)")
    }

})