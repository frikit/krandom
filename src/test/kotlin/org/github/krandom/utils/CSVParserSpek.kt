package org.github.krandom.utils

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object CSVParserSpek : Spek({

    val validTestCases = listOf("1,\n2,\n3")
    val invalidTestCases = listOf("", "   ")

    for (content in validTestCases) {
        val contentParsed = CSVParser.parse<String>(content, CSVParser.csvDelimiter)
        val expectedList = content.split(CSVParser.csvDelimiter)

        describe("check [$content] as it is return the list") {
            it("[$contentParsed] should be expected list") {
                assert(contentParsed == expectedList) { "[$contentParsed] should be [$expectedList]!" }
            }
        }
    }

    for (content in invalidTestCases) {
        val contentParsed = CSVParser.parse<String>(content, CSVParser.csvDelimiter)

        describe("check [$content] as it is return empty list") {
            it("[$contentParsed] should be blank") {
                assert(contentParsed.isEmpty()) { "[$contentParsed] should be blank!" }
            }
        }
    }
})
