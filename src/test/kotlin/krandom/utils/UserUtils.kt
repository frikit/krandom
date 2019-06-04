package krandom.utils

object UserUtils {
    fun validateName(name: String) {
        assert(!name.contains(Regex("0-9")))
        assert(!name.contains(Regex("[!@#$%^&*()_+]")))
        assert(name.contains(Regex("^\\S+$")))
    }
}