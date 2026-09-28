/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
package io.github.frikit.krandom.dsl

import io.github.frikit.krandom.generator.`object`.exception.ObjectGenerationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Exclusions apply to immutable Kotlin data classes, which are built through the
 * primary-constructor adapter.
 */
class DataClassExclusionTest {

    @Test
    fun excludedNullableConstructorPropertyIsNullAndNestedSameNamedPropertyIsGenerated() {
        val customer = krandom<ExcludedDataCustomer> {
            exclude(ExcludedDataCustomer::name)
        }

        assertNull(customer.name, "an excluded nullable constructor property must receive null")
        assertFalse(customer.company.name.isBlank(), "exclusions are scoped to the root type")
    }

    @Test
    fun excludedOptionalConstructorPropertyKeepsItsDefault() {
        val customer = krandom<ExcludedDataCustomer> {
            exclude(ExcludedDataCustomer::tier)
        }

        assertEquals("standard", customer.tier, "an excluded optional constructor property keeps its default")
    }

    @Test
    fun excludedRequiredNonNullConstructorPropertyFailsClearly() {
        val failure = assertThrows(ObjectGenerationException::class.java) {
            krandom<ExcludedDataCompany> {
                exclude(ExcludedDataCompany::name)
            }
        }

        assertTrue(
            generateSequence<Throwable>(failure) { it.cause }.any { it.message.orEmpty().contains("required and not nullable") },
            "the failure must name the excluded required parameter: $failure"
        )
    }
}

data class ExcludedDataCustomer(
    val name: String?,
    val company: ExcludedDataCompany,
    val tier: String = "standard"
)

data class ExcludedDataCompany(val name: String)
