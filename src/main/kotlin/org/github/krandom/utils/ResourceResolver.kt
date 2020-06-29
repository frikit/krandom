package org.github.krandom.utils

import java.io.File

object ResourceResolver {

    private const val emptyString = ""

    fun getResourceContent(relativePath: String): String {
        if (relativePath.isEmpty() || relativePath.isBlank()) return emptyString
        if (relativePath.trim() == "/" || relativePath.trim() == "\\" ) return emptyString

        val rel = File("").absoluteFile

        val resources = rel.walkTopDown()
                .maxDepth(100)
                .asSequence()
                .filter { it.absolutePath.contains("/resources/") }
                .filter { !it.absolutePath.contains("/test/") }
                .filter { it.isFile }
                .toSet()

        val file: File? = resources.firstOrNull { it.absolutePath.endsWith(relativePath) }

        return file?.reader()?.readText() ?: emptyString
    }
}
