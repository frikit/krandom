/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.GeneratorConfig
import io.github.frikit.krandom.generator.`object`.ObjectGenerator
import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.matchers.types.shouldBeInstanceOf
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

class KotlinValueClassTest :
    DescribeSpec({

        describe("Kotlin value classes") {

            it("boxes value classes over strings, numbers and other value classes") {
                val user = krandom<KUser> { config { seed(1L) } }

                user.id.value.shouldNotBeBlank()
                user.balance.shouldBeInstanceOf<KCents>()
                user.account.id.value
                    .shouldNotBeBlank()
                user.ids.shouldNotBeEmpty()
                user.ids.forEach { id -> id.value.shouldNotBeBlank() }
                user.boxed.value.shouldBeInstanceOf<String>()
            }

            it("generates the wrapped value under the property's name") {
                krandom<KUser> { config { seed(2L) } }.email.value shouldContain "@"
            }

            it("applies the property's constraints, then those declared inside the value class") {
                val user = krandom<KUser> { config { seed(3L) } }

                user.tag.value.length shouldBe 3
                user.initials.value.length shouldBe 2
                user.pin.digits.length shouldBe 4
                user.code.text.length shouldBe 6
                user.percent.value shouldBeInRange 0..100
            }

            it("generates the same values for the same seed") {
                krandom<KUser> { config { seed(4L) } } shouldBe krandom<KUser> { config { seed(4L) } }
            }

            it("generates nullable value classes and value classes over nullable values") {
                val nullable =
                    ObjectGenerator(
                        KNullables::class.java,
                        GeneratorConfig.builder().objectNullProbability(1.0).build(),
                    ).generate()

                nullable.backup shouldBe null
                nullable.note shouldBe KNote(null)
                krandom<KNullables> { exclude(KNullables::backup) }.backup shouldBe null
                krandom<KNullables> { rule(KNullables::backup) { KUserId("spare") } }.backup shouldBe
                    KUserId("spare")
                krandom<KNullables> { rule(KNullables::backup) { null } }.backup shouldBe null
                krandom<KNullables> { config { seed(6L) } }
                    .optionalText.value
                    .shouldBeInstanceOf<String>()
            }

            it("boxes a value class through a private constructor, as a Java one is called") {
                krandom<KSecretHolder>().secret.value.shouldNotBeBlank()
                krandom<KSecret>().value.shouldNotBeBlank()
            }

            it("passes a generic owner's type argument through a value class") {
                val owner = krandom<KGenericOwnerHolder> { config { seed(7L) } }.owner

                owner.boxed.value.shouldBeInstanceOf<String>()
                owner.plain.shouldBeInstanceOf<String>()
            }

            it("passes type arguments into the type a generic value class wraps") {
                val batch = krandom<KBatchHolder> { config { seed(8L) } }.batch

                batch.values.shouldNotBeEmpty()
                batch.values.forEach { value -> value.shouldBeInstanceOf<String>() }
            }

            it("passes projections and arrays into the type a generic value class wraps") {
                val variances = krandom<KVariances> { config { seed(9L) } }

                variances.produced.values.forEach { value -> value.shouldBeInstanceOf<String>() }
                variances.consumed.values.forEach { value -> value.shouldBeInstanceOf<String>() }
                variances.array.values.forEach { value -> value.shouldBeInstanceOf<String>() }
            }

            it("passes type arguments into arrays nested inside the type a generic value class wraps") {
                val nested = krandom<KNestedArraysHolder> { config { seed(11L) } }.nested

                nested.values.shouldNotBeEmpty()
                nested.values.forEach { array -> array.forEach { value -> value.shouldBeInstanceOf<String>() } }
            }

            it("gives a generic owner's parameterized types their arguments where its constructor is synthetic") {
                val mixed = krandom<KMixedHolder> { config { seed(10L) } }.mixed

                mixed.items.shouldNotBeEmpty()
                mixed.items.forEach { item -> item.shouldBeInstanceOf<String>() }
                mixed.array.forEach { item -> item.shouldBeInstanceOf<String>() }
                mixed.names.shouldNotBeEmpty()
                mixed.names.forEach { name -> name.shouldNotBeBlank() }
                mixed.counts.forEach { count -> count.shouldBeInstanceOf<Int>() }
                mixed.lookup.forEach { (key, value) ->
                    key.shouldBeInstanceOf<String>()
                    value.shouldBeInstanceOf<Long>()
                }
            }

            it("fails with context for a star projection inside the type a generic value class wraps") {
                val failure = shouldThrow<ObjectGenerationException> { krandom<KStarBatch>() }

                failure.message shouldBe
                    "Unsupported type at 'KStarBatch.batch' (declared type java.util.List<?>, depth 0)"
            }

            it("needs a rule for a generic value class as the generated type or as a var property") {
                shouldThrow<ObjectGenerationException> { krandom<KBox<String>>() }
                krandom<KBox<String>> { rule("value") { "ruled" } } shouldBe KBox("ruled")

                val failure = shouldThrow<ObjectGenerationException> { krandom<KMutableGeneric>() }
                failure.message shouldBe
                    "Unsupported type at 'KMutableGeneric.boxed' (declared type java.lang.Object, depth 0)"
            }

            it("fails with context for a generic value class whose type argument is a star projection") {
                val failure = shouldThrow<ObjectGenerationException> { krandom<KStarHolder>() }

                failure.message shouldBe "Unsupported type at 'KStarHolder.box' (declared type T, depth 0)"
            }

            it("takes a rule's value class, or the value it wraps") {
                krandom<KUser> { rule(KUser::id) { KUserId("fixed") } }.id shouldBe KUserId("fixed")
                krandom<KUser> { rule("id") { "raw" } }.id shouldBe KUserId("raw")
                val typed = krandom<KUser> { ruleForType<KUserId> { KUserId("typed") } }
                typed.id shouldBe KUserId("typed")
                typed.account shouldBe KAccountId(KUserId("typed"))
                typed.ids.forEach { id -> id shouldBe KUserId("typed") }
                krandom<KUser> { ruleForType<String> { "text" } }.id shouldBe KUserId("text")
            }

            it("fails with context when a rule gives a value the value class cannot hold") {
                val failure = shouldThrow<ObjectGenerationException> { krandom<KUser> { rule("id") { 42 } } }

                failure.cause?.message shouldBe
                    "Kotlin value class ${KUserId::class.java.name} cannot hold the java.lang.Integer generated " +
                    "for 'id' of ${KUser::class.java.name}"
            }

            it("fails with context when the value class rejects the generated value") {
                val failure =
                    shouldThrow<ObjectGenerationException> { krandom<KScore> { rule("percent") { 500 } } }

                failure.cause?.message shouldBe
                    "Kotlin value class ${KPercent::class.java.name} rejected the value generated for 'percent' of " +
                    "${KScore::class.java.name}; constrain the value it wraps or register a rule"
                failure.cause?.cause?.message shouldBe "percent out of range: 500"
            }

            it("generates a value class as a root") {
                krandom<KUserId>().value.shouldNotBeBlank()
                krandom<KPin>().digits.length shouldBe 4
                krandom<KAccountId>().id.value.shouldNotBeBlank()
                krandom<KPin> { rule("digits") { "1234" } } shouldBe KPin("1234")
            }

            it("fills value-class properties of a class populated after construction") {
                val account = krandom<KMutableAccount> { config { seed(5L) } }

                account.id.value.shouldNotBeBlank()
                account.id shouldNotBe KUserId("unset")
                account.savings.shouldBeInstanceOf<KCents>()
                account.label.value.length shouldBe 5
            }

            it("constructs a class whose constructor takes a value class through its Kotlin form") {
                krandom<KVarHolder>().id.value.shouldNotBeBlank()
            }

            it("leaves a class with a no-argument constructor to be filled field by field, rules included") {
                val model = krandom<KModel> { rule(KModel::count) { 42 } }
                val defaulted = krandom<KDefaultedModel> { rule(KDefaultedModel::count) { 7 } }

                model.count shouldBe 42
                model.id shouldNotBe KUserId("preset")
                defaulted.count shouldBe 7
                defaulted.id shouldNotBe KUserId("preset")
            }

            it("runs the guide's member example") {
                val member = krandom<GuideMember> { config { seed(42L) } }

                member.email.value shouldContain "@"
                member.pin.digits.length shouldBe 4
                member.initials.letters.length shouldBe 2
                member.balance.shouldBeInstanceOf<GuideCents>()
                krandom<GuideMember> { rule("pin") { "1234" } }.pin shouldBe GuidePin("1234")
            }

            it("generates Kotlin's unsigned value classes") {
                krandom<KCounter> { rule(KCounter::count) { 7u } }.count shouldBe 7u
                krandom<KCounter>().count.shouldBeInstanceOf<UInt>()
            }
        }
    })

@JvmInline
value class KUserId(
    val value: String,
)

@JvmInline
value class KEmailAddress(
    val value: String,
)

@JvmInline
value class KCents(
    val amount: Long,
)

@JvmInline
value class KPin(
    @param:Size(min = 4, max = 4) val digits: String,
)

@JvmInline
value class KCode(
    @field:Size(min = 6, max = 6) val text: String,
)

@JvmInline
value class KAccountId(
    val id: KUserId,
)

@JvmInline
value class KNote(
    val text: String?,
)

@JvmInline
value class KBox<T>(
    val value: T,
)

@JvmInline
value class KPercent(
    @param:Min(0) @param:Max(100) val value: Int,
) {
    init {
        require(value in 0..100) { "percent out of range: $value" }
    }
}

data class KUser(
    val id: KUserId,
    val email: KEmailAddress,
    val balance: KCents,
    val pin: KPin,
    val code: KCode,
    @field:Size(min = 3, max = 3) val tag: KUserId,
    @param:Size(min = 2, max = 2) val initials: KUserId,
    val account: KAccountId,
    val percent: KPercent,
    val ids: List<KUserId>,
    val boxed: KBox<String>,
)

data class KNullables(
    val backup: KUserId?,
    val note: KNote,
    val optionalText: KBox<String?>,
)

@JvmInline
value class KSecret private constructor(
    val value: String,
) {
    companion object {
        fun of(value: String): KSecret = KSecret(value)
    }
}

data class KSecretHolder(
    val secret: KSecret,
)

data class KStarHolder(
    val box: KBox<*>,
)

data class KGenericOwner<T>(
    val boxed: KBox<T>,
    val plain: T,
)

class KMutableGeneric {
    var boxed: KBox<String> = KBox("initial")
}

@JvmInline
value class KBatch<T>(
    val values: List<T>,
)

@JvmInline
value class KOutList<T>(
    val values: MutableList<out T>,
)

@JvmInline
value class KInList<T>(
    val values: MutableList<in T>,
)

@JvmInline
value class KArrayBox<T>(
    val values: Array<T>,
)

data class KBatchHolder(
    val batch: KBatch<String>,
)

@JvmInline
value class KNestedArrays<T>(
    val values: List<Array<T>>,
)

data class KNestedArraysHolder(
    val nested: KNestedArrays<String>,
)

data class KVariances(
    val produced: KOutList<String>,
    val consumed: KInList<String>,
    val array: KArrayBox<String>,
)

data class KMixed<T>(
    val id: KUserId,
    val items: List<T>,
    val array: Array<T>,
    val names: Array<String>,
    val counts: Array<Int>,
    val lookup: Map<String, Long>,
)

data class KMixedHolder(
    val mixed: KMixed<String>,
)

data class KStarBatch(
    val batch: KBatch<*>,
)

data class KGenericOwnerHolder(
    val owner: KGenericOwner<String>,
)

data class KScore(
    val percent: KPercent,
)

class KMutableAccount {
    var id: KUserId = KUserId("unset")
    var savings: KCents? = null

    @get:Size(min = 5, max = 5)
    var label: KUserId = KUserId("x")
}

class KVarHolder(
    var id: KUserId,
)

/** A mutable class with a value-class parameter and a no-argument constructor of its own. */
class KModel(
    var id: KUserId,
) {
    constructor() : this(KUserId("preset"))

    var count: Int = 0
}

/** The same, where the no-argument constructor comes from the parameter's default. */
class KDefaultedModel(
    var id: KUserId = KUserId("preset"),
) {
    var count: Int = 0
}

data class KCounter(
    val count: UInt,
)

@JvmInline
value class GuideEmailAddress(
    val value: String,
)

@JvmInline
value class GuidePin(
    @param:Size(min = 4, max = 4) val digits: String,
)

@JvmInline
value class GuideInitials(
    val letters: String,
)

@JvmInline
value class GuideCents(
    val amount: Long,
)

data class GuideMember(
    val email: GuideEmailAddress,
    val pin: GuidePin,
    @field:Size(min = 2, max = 2) val initials: GuideInitials,
    val balance: GuideCents,
    val referrer: GuideEmailAddress?,
)
