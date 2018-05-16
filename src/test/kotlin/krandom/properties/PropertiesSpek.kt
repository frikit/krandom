package krandom.properties

import krandom.utils.TestLifecycle
import mu.KLogger
import mu.KLogging
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.on
import java.lang.reflect.Field

class PropertiesSpek : Spek({

    val logger: KLogger = KLogging().logger(PropertiesSpek::class.java.simpleName)

    val expectedFields: Map<String, Any> = mapOf(
            Pair("maxDouble", Double.MAX_VALUE),
            Pair("minDouble", Double.MIN_VALUE),
            Pair("maxFloat", Float.MAX_VALUE),
            Pair("minFloat", Float.MIN_VALUE),
            Pair("maxLong", Long.MAX_VALUE),
            Pair("minLong", Long.MIN_VALUE),
            Pair("maxInt", Int.MAX_VALUE),
            Pair("minInt", Int.MIN_VALUE),
            Pair("maxShort", Short.MAX_VALUE),
            Pair("minShort", Short.MIN_VALUE),
            Pair("maxByte", Byte.MAX_VALUE),
            Pair("minByte", Byte.MIN_VALUE)
    )

    TestLifecycle().onTestStart("test properties class")
    describe("a properties object") {
        val properties = Properties
        describe("get fields from object") {
            val fields: Array<Field> = properties.javaClass.fields
            val fieldNames: List<String> = fields
                    .filter { it.name != "INSTANCE" }//filter object of instance
                    .map { it.toGenericString() }
                    .map { it.split(".Properties.")[1] }

            fieldNames.forEach {
                on("check prop name = $it it should not be empty or blank") {
                    assert(it.isNotBlank())
                    assert(it.isNotEmpty())
                    logger.info { "Prop with name $it is not blank and not empty!" }
                }
            }

            fieldNames.forEach {
                val expectedValue = expectedFields[it]
                val getValue = properties.javaClass.getDeclaredField(it).get(properties)
                on("check prop name = $expectedValue as expectedValue should be = $getValue") {
                    logger.info { "check if $expectedValue == $getValue" }
                    assert(expectedValue == getValue, { "Expected  should be the same!" })
                }
            }
        }
    }
    TestLifecycle().onTestFinish("test properties class")
})