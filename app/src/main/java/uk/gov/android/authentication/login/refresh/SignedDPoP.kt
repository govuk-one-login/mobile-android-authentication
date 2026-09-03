package uk.gov.android.authentication.login.refresh

sealed class SignedDPoP {
    data class Success(val popJwt: String) : SignedDPoP()
    data class Failure(val reason: String, val error: Throwable? = null) : SignedDPoP()
}
