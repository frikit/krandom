package org.github.krandom.utils

import org.github.krandom.utils.ResourcePathHolder.relativeFirstNamePath
import org.github.krandom.utils.ResourcePathHolder.relativeSurNamePath
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ResourceResolverSpek : Spek({

    val validTestCases = listOf(relativeFirstNamePath, relativeSurNamePath)
    val invalidTestCases = listOf("", "   ", "/", "\\", "  /  ", "  \\  ", "/not/existing/file/path")

    describe("try to access resource from test folder") {
        val path = "dummy.test.resource"
        val content = ResourceResolver.getResourceContent(path)

        describe("check [$path] as it is return empty content") {
            it("[$path] should be blank") {
                assert(content.isBlank()) { "[$content] should be blank!" }
            }
            it("[$path] should be empty") {
                assert(content.isEmpty()) { "[$content] should be empty!" }
            }
        }
    }

    for (path in validTestCases) {
        val content = ResourceResolver.getResourceContent(path)

        describe("check [$path] as it is return some content") {
            it("[$path] should not be blank") {
                assert(content.isNotBlank()) { "[$content] should not be blank!" }
            }
            it("[$path] should not be empty") {
                assert(content.isNotEmpty()) { "[$content] should not be empty!" }
            }
        }
    }

    for (path in invalidTestCases) {
        val content = ResourceResolver.getResourceContent(path)

        describe("check [$path] as it is return empty content") {
            it("[$path] should be blank") {
                assert(content.isBlank()) { "[$content] should be blank!" }
            }
            it("[$path] should be empty") {
                assert(content.isEmpty()) { "[$content] should be empty!" }
            }
        }
    }
})
