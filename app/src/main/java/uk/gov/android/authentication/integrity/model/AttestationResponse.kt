package uk.gov.android.authentication.integrity.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
sealed class AttestationResponse {
    @Serializable
    data class Success(
        @JsonNames("client_attestation")
        val attestationJwt: String,
        @JsonNames("expires_in")
        val expiresIn: Int
    ) : AttestationResponse()
    data class Failure(val reason: String, val error: Throwable? = null) : AttestationResponse()
}
