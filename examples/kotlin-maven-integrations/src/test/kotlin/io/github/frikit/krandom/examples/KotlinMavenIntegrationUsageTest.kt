/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples

import io.github.frikit.krandom.dsl.krandom
import io.github.frikit.krandom.dsl.krandomList
import io.github.frikit.krandom.generator.Generator
import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.Generators
import io.github.frikit.krandom.generator.commerce.OrderInfo
import io.github.frikit.krandom.generator.finance.PaymentCardSafetyPolicy
import io.github.frikit.krandom.generator.`object`.ObjectFieldStreamPolicy
import io.github.frikit.krandom.generator.provider.ProviderHub
import io.github.frikit.krandom.generator.user.EmailGenerator
import io.github.frikit.krandom.jackson.KrandomJackson
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
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import java.util.OptionalLong

class KotlinMavenIntegrationUsageTest {
    @Test
    fun `kotest extension is consumable from maven`() {
        val emails =
            krandomArb(GeneratorConfig.defaults()) { config -> EmailGenerator(config) }
                .take(5)
                .toList()

        assertEquals(5, emails.size)
        emails.forEach { email -> assertTrue(email.contains("@")) }
    }

    @Test
    fun `kotlin dsl is consumable from maven`() {
        val user =
            krandom<MavenDslUserFixture> {
                rule("name") { "Ada Lovelace" }
                rule("email") { "ada@example.test" }
            }
        val users =
            krandomList<MavenDslUserFixture>(3) {
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
    fun `independent streams and snapshots work from published Kotlin artifacts`() {
        val plain =
            krandomList<IntegrationStreamFixture>(5) {
                config {
                    seed(42L)
                    objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                }
            }
        val custom =
            krandomList<IntegrationStreamFixture>(5) {
                config {
                    seed(42L)
                    objectFieldStreamPolicy(ObjectFieldStreamPolicy.INDEPENDENT)
                }
                rule(IntegrationStreamFixture::name) { "fixed" }
            }
        assertEquals(plain.map { it.age }, custom.map { it.age })
        val session =
            GeneratorConfig
                .builder()
                .seed(42L)
                .build()
                .snapshotClock()
        assertEquals(
            session.clock,
            session.generationRecipe
                .orElseThrow()
                .toGeneratorConfig()
                .clock,
        )
    }

    @Test
    fun `spring starter is consumable from Kotlin Maven`() {
        val runner =
            ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration::class.java))
                .withPropertyValues("krandom.locale=en-US", "krandom.seed=42")

        runner.run { context ->
            val config = context.getBean(GeneratorConfig::class.java)
            val hub = context.getBean(ProviderHub::class.java)
            val factory = context.getBean(KrandomObjectFakerFactory::class.java)

            assertEquals(Locale.US, config.locale)
            assertFalse(
                hub
                    .get("person.email", Generator::class.java)
                    .generate()
                    .toString()
                    .isBlank(),
            )
            assertNotNull(factory.generator(KotlinSpringFixture::class.java).generate())
        }
    }

    @Test
    fun `spring starter binds krandom properties from Kotlin Maven`() {
        val runner =
            ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(KrandomAutoConfiguration::class.java))
                .withPropertyValues(
                    "krandom.seed=42",
                    "krandom.locale=de-DE",
                    "krandom.clock=2026-01-01T00:00:00Z",
                    "krandom.clock-zone=Europe/Berlin",
                    "krandom.object-max-depth=3",
                    "krandom.min-collection-size=2",
                    "krandom.max-collection-size=4",
                    "krandom.payment-card-safety-policy=stripe-sandbox",
                )

        runner.run { context ->
            val config = context.getBean(GeneratorConfig::class.java)

            assertEquals(OptionalLong.of(42L), config.seed)
            assertEquals(Locale.GERMANY, config.locale)
            assertEquals(Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Europe/Berlin")), config.clock)
            assertEquals(3, config.objectMaxDepth)
            assertEquals(2, config.minCollectionSize)
            assertEquals(4, config.maxCollectionSize)
            assertEquals(PaymentCardSafetyPolicy.STRIPE_SANDBOX, config.paymentCardSafetyPolicy)
        }
    }

    @Test
    fun `krandom-jackson writes schemas and generated rows with Jackson 3`() {
        val config =
            GeneratorConfig
                .builder()
                .locale(Locale.US)
                .seed(42L)
                .build()
        val schema =
            Generators.ofSchema(
                config,
                mapOf("order" to Generators.ofField(config).bind("commerce.order_info")),
            )
        // Jackson 3 mappers are immutable, so the module is added to the builder.
        val mapper = KrandomJackson.configure(JsonMapper.builder()).build()

        val jsonSchema: JsonNode = mapper.valueToTree(schema)
        assertEquals("object", jsonSchema.at("/properties/order/type").asString())

        // Jackson 3 writes java.time values as ISO-8601 text without an extra module.
        val row = schema.generate()
        val json: JsonNode = mapper.valueToTree(row)
        val order = row.getValue("order") as OrderInfo
        assertEquals(order.orderedOn().toString(), json.at("/order/orderedOn").asString())
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

data class IntegrationStreamFixture(
    val name: String,
    val age: Int,
)
