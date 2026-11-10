/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import java.util.Optional

class KotlinSealedTypeTest :
    DescribeSpec({

        describe("Kotlin sealed types") {

            it("generates a sealed interface as each of its subclasses, objects and enums included") {
                val shapes = (0L until 120L).map { seed -> krandom<KShape> { config { seed(seed) } } }

                shapes.map { shape -> shape::class }.toSet() shouldBe
                    setOf(KCircle::class, KSquare::class, KTriangle::class, KNoShape::class, KFill::class)
                shapes.filterIsInstance<KNoShape>().forEach { shape -> shape shouldBeSameInstanceAs KNoShape }
                shapes.filterIsInstance<KFill>().toSet() shouldBe setOf(KFill.SOLID, KFill.HATCHED)
            }

            it("chooses the same subclasses for the same seed") {
                val first = krandomList<KCanvas>(20) { config { seed(11L) } }
                val second = krandomList<KCanvas>(20) { config { seed(11L) } }

                first shouldBe second
                first.map { canvas -> canvas.shape::class }.toSet().size shouldBe 5
            }

            it("reads the subclasses from Kotlin metadata") {
                val adapter = KotlinPrimaryConstructorAdapter()

                adapter.sealedSubclasses(KShape::class.java) shouldBe
                    Optional.of(KShape::class.sealedSubclasses.map { subclass -> subclass.java })
                adapter.sealedSubclasses(KCircle::class.java) shouldBe Optional.empty()
            }

            it("fails for a sealed interface that Kotlin declares without subclasses") {
                // The JVM does not see a sealed interface with no subclass as sealed; Kotlin metadata does.
                val failure = shouldThrow<ObjectGenerationException> { krandom<SealedProfile>() }

                failure.cause?.message shouldBe
                    "Sealed type ${SealedProfile::class.java.name} has no subclass to generate; " +
                    "register a type override for it"
            }

            it("ends a recursive hierarchy in a leaf, so non-null children do not fail") {
                val roots =
                    (0L until 100L).map { seed -> krandom<KFormula> { config { seed(seed) } }.expr::class }

                roots.toSet() shouldBe setOf(KNum::class, KAdd::class)
            }

            it("fails with context for an abstract subclass that is not sealed") {
                val failure = shouldThrow<ObjectGenerationException> { krandom<KEventLog>() }

                failure.cause?.message shouldBe
                    "Sealed type ${KEvent::class.java.name} permits ${KCustomEvent::class.java.name}, which is " +
                    "abstract and not sealed; map it with objectSubtype or register a type override for it"
            }

            it("lets a type rule for the sealed type win") {
                krandom<KCanvas> { ruleForType<KShape> { KCircle(1.0) } }.shape shouldBe KCircle(1.0)
                krandom<KShape> { ruleForType<KShape> { KCircle(2.0) } } shouldBe KCircle(2.0)
            }

            it("passes a generic sealed property's type argument to the chosen subclass") {
                val results = (0L until 40L).map { seed -> krandom<KResponse> { config { seed(seed) } }.result }

                results.map { result -> result::class }.toSet() shouldBe setOf(KOk::class, KErr::class)
                results.filterIsInstance<KOk<*>>().forEach { ok -> ok.value.shouldBeInstanceOf<String>() }
            }

            it("runs the guide's payment example") {
                val payments =
                    (0L until 40L).map { seed -> krandom<GuideOrder> { config { seed(seed) } }.payment }

                payments.map { payment -> payment::class }.toSet() shouldBe
                    setOf(GuideCard::class, GuideTransfer::class, GuideCash::class)
                krandom<GuideOrder> { config { seed(42L) } } shouldBe krandom<GuideOrder> { config { seed(42L) } }
            }

            it("still rejects an abstract Kotlin type that is not sealed") {
                val failure = shouldThrow<ObjectGenerationException> { krandom<KCustomEvent>() }

                failure.cause?.message shouldBe
                    "Kotlin abstract type ${KCustomEvent::class.java.name} requires a concrete type override"
            }
        }
    })

sealed interface KShape

data class KCircle(
    val radius: Double,
) : KShape

sealed class KPolygon : KShape

data class KSquare(
    val side: Double,
) : KPolygon()

data class KTriangle(
    val base: Double,
    val height: Double,
) : KPolygon()

object KNoShape : KShape

enum class KFill : KShape {
    SOLID,
    HATCHED,
}

data class KCanvas(
    val shape: KShape,
)

sealed interface KExpr

data class KNum(
    val value: Int,
) : KExpr

data class KAdd(
    val left: KExpr,
    val right: KExpr,
) : KExpr

data class KFormula(
    val expr: KExpr,
)

sealed class KEvent

data class KCreated(
    val id: String,
) : KEvent()

abstract class KCustomEvent : KEvent()

data class KEventLog(
    val event: KEvent,
)

sealed interface KResult<out T>

data class KOk<T>(
    val value: T,
) : KResult<T>

data class KErr(
    val message: String,
) : KResult<Nothing>

data class KResponse(
    val result: KResult<String>,
)

sealed interface GuidePayment

data class GuideCard(
    val number: String,
) : GuidePayment

data class GuideTransfer(
    val iban: String,
) : GuidePayment

data object GuideCash : GuidePayment

data class GuideOrder(
    val payment: GuidePayment,
)
