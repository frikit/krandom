package krandom.utils

import java.io.File

object ResourceResolver {

    fun getResourceContent(relativePath: String): String {
        val rel = File("").absoluteFile
        var result: String? = ""

        val resources = rel.walkTopDown()
                .map { it }
                .filter { it.absolutePath.contains("/resources/") }
                .filter { !it.absolutePath.contains("/resources/test/") }
                .filter { it.isFile }
                .toList()

        resources.forEach {
            if (it.absolutePath.endsWith(relativePath)) {
                result = it.reader().readText()
                return@forEach
            }
        }

        return result!!
    }
}