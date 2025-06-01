package uk.gov.android.authentication.integrity.pop

sealed class SignedPoP {
    data class Success(
        val popJwt: String,
    ) : SignedPoP()

    data class Failure(
        val reason: String,
        val error: Throwable? = null,
    ) : SignedPoP()
}
