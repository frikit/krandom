package io.github.frikit.krandom.examples

import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.dsl.krandomList
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.provider.ProviderHub
import io.github.frikit.krandom.generator.user.EmailGenerator
import io.github.frikit.krandom.kotest.krandomArb
import io.github.frikit.krandom.spring.KrandomAutoConfiguration
import io.github.frikit.krandom.spring.KrandomObjectFakerFactory
import io.kotest.property.arbitrary.take
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class KotlinMavenIntegrationUsageTest {

    @Test
    fun `kotest extension is consumable from maven`() {
        val emails = krandomArb(GeneratorConfig.defaults()) { config -> EmailGenerator(config) }
            .take(5).toList()

        assertEquals(5, emails.size)
        emails.forEach { email -> assertTrue(email.contains("@")) }
    }

    @Test
    fun `kotlin dsl is consumable from maven`() {
        val user = krandom<MavenDslUserFixture> {
            rule("name") { "Ada Lovelace" }
            rule("email") { "ada@example.test" }
        }
        val users = krandomList<MavenDslUserFixture>(3) {
            rule("name") { "Grace Hopper" }
            rule("email") { "grace@example.test" }
        }

        assertEquals("Ada Lovelace", user.name)
        assertEquals("ada@example.test", user.email)
        assertEquals(3, users.size)
        users.forEach { generated ->
            assertEquals("Grace Hopper", generated.name)
            assertEquals("grace@example.test", generated.email)
        }
    }

    @Test
    fun `spring starter is consumable from Kotlin Maven`() {
        val runner = ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration::class.java))
            .withPropertyValues("krandom.locale=en-US", "krandom.seed=42")

        runner.run { context ->
            val config = context.getBean(GeneratorConfig::class.java)
            val hub = context.getBean(ProviderHub::class.java)
            val factory = context.getBean(KrandomObjectFakerFactory::class.java)

            assertEquals(java.util.Locale.US, config.locale)
            assertFalse(hub.get("person.email", Generator::class.java).generate().toString().isBlank())
            assertNotNull(factory.generator(KotlinSpringFixture::class.java).generate())
        }
    }
}

class MavenDslUserFixture {
    var name: String = ""
    var email: String = ""
}

class KotlinSpringFixture {
    var name: String = ""
    var email: String = ""
}
