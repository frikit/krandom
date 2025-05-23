package org.github.krandom.user

import org.github.krandom.common.Randomizer
import java.util.Random

object CountryGenerator {

    private val countries = listOf(
        "United States", "Canada", "Mexico", "Brazil", "Argentina",
        "United Kingdom", "Germany", "France", "Spain", "Italy",
        "China", "India", "Japan", "South Korea", "Australia",
        "South Africa", "Nigeria", "Egypt", "Kenya", "Russia"
    )

    private val random = Randomizer()

    /**
     * Returns a randomly selected country name from a predefined list.
     *
     * @return A string representing a country name.
     */
    fun randomCountry(): String {
        return countries[random.randomInt(0, countries.size - 1)]
    }
}
