package uk.gov.android.authentication.integrity.model

sealed class AttestationResponse {
    data class Success(val attestationJwt: String) : AttestationResponse()
    data class Failure(val reason: String, val error: Throwable? = null) : AttestationResponse()
}
