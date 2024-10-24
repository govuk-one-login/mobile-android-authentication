package uk.gov.android.authentication.integrity.model

sealed class SignedResponse {
    data class Success(val signedAttestationJwt: String) : SignedResponse()
    data class Failure(val reason: String, val error: Throwable? = null) : SignedResponse()
}
