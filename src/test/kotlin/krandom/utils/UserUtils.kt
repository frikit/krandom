package krandom.utils

object UserUtils {
    val size: Int by lazy {
        10
    }

    fun validateName(name: String) {
        assert(!name.contains(Regex("0-9"))) { "Name should not contains numbers! [$name]" }
        assert(!name.contains(Regex("[!@#$%^&*()_+]"))) { "Name should not contains special chars expect \'! [$name]" }
        assert(name.contains(Regex("^\\S+$"))) { "Name should not contains white spaces! [$name]" }
    }

    fun validateNames(name: List<String>) {
        name.forEach { validateName(it) }
    }
}