package org.github.krandom.user

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import kotlin.test.assertTrue
import org.github.krandom.testhelper.Constants // Assuming this constant exists for loop counts

object CountryGeneratorSpek : Spek({

    // Accessing the list of countries from the generator itself for assertion
    // This is a way to avoid duplicating the list in tests.
    // If CountryGenerator.countries were private, we'd need a different approach
    // or accept some duplication. For now, let's assume we can make it internal or public for testing
    // or use reflection if strictly private.
    // For simplicity in this instruction, I'll assume direct access or a helper is possible.
    // If not, the worker should try to get the list or a representative sample.
    // For now, let's define a local list that MUST match the one in CountryGenerator for the test to be valid.
    // This is not ideal, but avoids making changes to the main code just for testing access.

    val expectedCountries = listOf(
        "United States", "Canada", "Mexico", "Brazil", "Argentina",
        "United Kingdom", "Germany", "France", "Spain", "Italy",
        "China", "India", "Japan", "South Korea", "Australia",
        "South Africa", "Nigeria", "Egypt", "Kenya", "Russia"
    )

    describe("CountryGenerator.randomCountry()") {
        it("should return a valid country from the predefined list") {
            val country = CountryGenerator.randomCountry()
            assertTrue(expectedCountries.contains(country), "Generated country '$country' is not in the expected list.")
        }

        it("should return different countries over multiple calls (probabilistic)") {
            // This test is probabilistic. It doesn't guarantee randomness but checks for variation.
            val generatedCountries = mutableSetOf<String>()
            val numberOfCalls = Constants.generateValues // Use a constant from the project if available
            
            if (expectedCountries.size > 1) { // Only run this if there's more than one country to choose from
                // Try to get at least 2 different countries, or half the list size if small
                val minimumDifferentCountries = minOf(expectedCountries.size / 2, 2).coerceAtLeast(1)
                var attempts = 0
                // Attempt a certain number of times to get a variety, especially if the list is small
                val maxAttempts = numberOfCalls * 2 


                while(generatedCountries.size < minimumDifferentCountries && attempts < maxAttempts) {
                    generatedCountries.add(CountryGenerator.randomCountry())
                    attempts++
                }
                assertTrue(
                    generatedCountries.size >= minimumDifferentCountries,
                    "Expected at least $minimumDifferentCountries different countries after $maxAttempts calls, but got ${generatedCountries.size}. " +
                    "This test is probabilistic and might occasionally fail even with a correct implementation."
                )
            } else {
                // If only one country, it should always return that one
                val country = CountryGenerator.randomCountry()
                assertTrue(expectedCountries.contains(country), "Generated country '$country' is not in the expected list.")
            }
        }

        describe("consistency over many calls") {
            (1..Constants.generateValues).forEach { attempt ->
                it("attempt #$attempt: should return a country from the predefined list") {
                    val country = CountryGenerator.randomCountry()
                    assertTrue(expectedCountries.contains(country), "Generated country '$country' is not in the expected list on attempt #$attempt.")
                }
            }
        }
    }
})
