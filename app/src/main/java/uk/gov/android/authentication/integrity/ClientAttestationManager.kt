package uk.gov.android.authentication.integrity

import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.SignedResponse

interface ClientAttestationManager {
    suspend fun getAttestation(): AttestationResponse
    suspend fun generatePoP(iss: String, aud: String): SignedResponse
}
