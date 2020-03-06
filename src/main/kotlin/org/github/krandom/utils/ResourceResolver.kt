package org.github.krandom.utils

import java.io.File

object ResourceResolver {

    fun getResourceContent(relativePath: String): String {
        val rel = File("").absoluteFile

        val resources = rel.walkTopDown()
                .map { it }
                .filter { it.absolutePath.contains("/resources/") }
                .filter { !it.absolutePath.contains("/resources/test/") }
                .filter { it.isFile }
                .toList()

        val file: File? = resources.firstOrNull { it.absolutePath.endsWith(relativePath) }

        return file?.reader()?.readText() ?: ""
    }
}
