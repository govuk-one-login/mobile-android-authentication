package uk.gov.android.authentication.integrity

import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.integrity.pop.SignedPoP

interface ClientAttestationManager {
    suspend fun getAttestation(): AttestationResponse
    fun generatePoP(iss: String, aud: String): SignedPoP
}
