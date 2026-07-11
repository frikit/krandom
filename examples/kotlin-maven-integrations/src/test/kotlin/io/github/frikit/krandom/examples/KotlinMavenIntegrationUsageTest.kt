package io.github.frikit.krandom.examples

import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.dsl.krandomList
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.user.EmailGenerator
import io.github.frikit.krandom.kotest.krandomArb
import io.kotest.property.arbitrary.take
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
}

class MavenDslUserFixture {
    var name: String = ""
    var email: String = ""
}
