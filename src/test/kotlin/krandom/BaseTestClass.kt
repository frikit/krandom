package krandom

import krandom.common.KRandom
import krandom.common.Randomizer
import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KotlinLogging
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName

open class BaseTestClass {

    @Rule
    @JvmField
    var name: TestName = TestName()

    val randomizer: KRandom = Randomizer()
    val logger: KLogger = KotlinLogging.logger(BaseTestClass::class.java.simpleName)

    var currentNumber = Any()

    val kDoubles: MutableList<Double> = ArrayList()

    @Before
    fun before() {
        TestLifecycle().onTestStart(name.methodName)
        logger.info { "Current number is $currentNumber" }

        kDoubles.clear()
    }

    @After
    fun after() {
        TestLifecycle().onTestFinish(name.methodName)
    }
}