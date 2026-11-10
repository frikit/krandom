/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.examples

import io.github.frikit.krandom.dsl.krandomList
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The Kotlin DSL generates a sealed class as one of its subclasses, and a `@JvmInline` value class as
 * the value it wraps, boxed through its primary constructor so that its `init` block runs.
 */
class SealedAndValueClassExampleTest {
    @Test
    fun `kotlin dsl generates sealed classes and value classes`() {
        val customers = krandomList<Customer>(30) { config { seed(42L) } }

        assertEquals(setOf(Card::class, Transfer::class, Cash::class), customers.map { it.payment::class }.toSet())
        // The wrapped value is generated under the parameter's name, email.
        customers.forEach { assertTrue("@" in it.email.value) }
    }
}

sealed class Payment

data class Card(
    val number: String,
) : Payment()

data class Transfer(
    val iban: String,
) : Payment()

data object Cash : Payment()

@JvmInline
value class EmailAddress(
    val value: String,
) {
    init {
        require("@" in value) { "an email address needs an @" }
    }
}

data class Customer(
    val email: EmailAddress,
    val payment: Payment,
)
