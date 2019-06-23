package krandom.exceptions

class SizeLimitExceedException : IllegalArgumentException {
    constructor(s: String?) : super(s)
}