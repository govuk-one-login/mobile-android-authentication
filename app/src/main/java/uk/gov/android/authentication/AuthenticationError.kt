package uk.gov.android.authentication

class AuthenticationError(
    override val message: String,
    val type: ErrorType,
) : Error() {
    enum class ErrorType {
        OAUTH,
        NETWORK,
    }
}
