/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import jakarta.validation.constraints.Size

class KotlinImmutableObjectGenerationTest : DescribeSpec({

    describe("immutable Kotlin object generation") {

        it("constructs primary parameters through constraints and nested generic resolution") {
            val profile = krandom<ImmutableProfile>()

            profile.code.length shouldBe 4
            profile.fieldCode.length shouldBe 3
            profile.name.shouldNotBeBlank()
            profile.labels.shouldNotBeEmpty()
            profile.labels.forEach { label -> label.email.shouldNotBeBlank() }
            profile.optionalText shouldBe "preserved-default"
        }

        it("resolves concrete generic bindings through nested primary constructors") {
            val envelope = krandom<GenericEnvelope>()

            envelope.label.value.shouldNotBeBlank()
            envelope.counts.value.shouldNotBeEmpty()
            envelope.counts.value.forEach { count -> count.inc() shouldBe count + 1 }
            envelope.nested.value.value.shouldNotBeBlank()
        }

        it("applies explicit field rules without discarding optional defaults by default") {
            val profile = krandom<ImmutableProfile> {
                rule("code") { "1234" }
                rule("fieldCode") { "123" }
                rule("name") { "Ada" }
                rule("optionalText") { "explicit-value" }
            }

            profile.code shouldBe "1234"
            profile.fieldCode shouldBe "123"
            profile.name shouldBe "Ada"
            profile.optionalText shouldBe "explicit-value"
        }

        it("allows null only for nullable primary parameters") {
            val nullable = krandom<NullableProfile> {
                rule<String?>("note") { null }
            }

            nullable.note shouldBe null
        }

        it("rejects an explicit null for a non-null primary parameter") {
            val failure = shouldThrow<ObjectGenerationException> {
                krandom<NonNullProfile> {
                    rule<String?>("value") { null }
                }
            }
            val expectedMessage =
                "Kotlin non-null constructor parameter 'value' of " +
                    NonNullProfile::class.java.name + " resolved to null"

            failure.cause?.message shouldBe expectedMessage
        }

        it("returns Kotlin object singletons without reflective allocation") {
            krandom<SingletonProfile>() shouldBe SingletonProfile
        }

        it("uses the primary constructor and initializes delegated properties") {
            SecondaryConstructorProfile.secondaryCalls = 0
            val secondary = krandom<SecondaryConstructorProfile> {
                rule("value") { "primary" }
            }
            val delegated = krandom<DelegatedProfile> {
                rule("value") { "Ada" }
            }

            secondary.value shouldBe "primary"
            SecondaryConstructorProfile.secondaryCalls shouldBe 0
            delegated.normalized shouldBe "ADA"
        }

        it("rejects a required immutable cycle before returning a partial value") {
            shouldThrow<ObjectGenerationException> {
                krandom<RequiredCycle>()
            }
        }

        it("rejects value and sealed classes before a value escapes") {
            val valueFailure = shouldThrow<ObjectGenerationException> {
                krandom<InlineIdentifier>()
            }
            valueFailure.cause?.message shouldBe
                "Kotlin value classes are not supported for object generation; register a type override"

            val sealedFailure = shouldThrow<ObjectGenerationException> {
                krandom<SealedProfile>()
            }
            sealedFailure.cause?.message shouldBe
                "Kotlin sealed and abstract types require a concrete type override"
        }
    }
})

data class ImmutableProfile(
    @param:Size(min = 4, max = 4)
    val code: String,
    @field:Size(min = 3, max = 3)
    val fieldCode: String,
    val name: String,
    val labels: List<ImmutableLabel>,
    val optionalText: String = "preserved-default"
)

data class ImmutableLabel(val email: String)

data class GenericEnvelope(
    val label: GenericBox<String>,
    val counts: GenericBox<List<Int>>,
    val nested: GenericBox<GenericBox<String>>
)

data class GenericBox<T>(val value: T)

data class NullableProfile(val note: String?)

data class NonNullProfile(val value: String)

object SingletonProfile

class SecondaryConstructorProfile(val value: String) {

    companion object {
        var secondaryCalls: Int = 0
    }

    constructor(number: Int) : this("secondary-$number") {
        secondaryCalls++
    }
}

class DelegatedProfile(val value: String) {

    val normalized: String by lazy { value.uppercase() }
}

data class RequiredCycle(val child: RequiredCycle)

@JvmInline
value class InlineIdentifier(val value: String)

sealed interface SealedProfile
