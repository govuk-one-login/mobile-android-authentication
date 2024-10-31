package uk.gov.android.authentication.integrity

import android.util.Log
import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.model.AppCheckToken
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.SignedResponse
import uk.gov.android.authentication.integrity.usecase.AttestationCaller

@Suppress("UnusedPrivateProperty")
class FirebaseClientAttestationManager(
    config: AppIntegrityConfiguration
) : ClientAttestationManager {
    private val appChecker: AppChecker = config.appChecker
    private val attestationCaller: AttestationCaller = config.attestationCaller
    private val keyManager = KeystoreManager()

    override suspend fun getAttestation(): AttestationResponse {
        // Get Firebase token
        val token = appChecker.getAppCheckToken().getOrElse { err ->
            AttestationResponse.Failure(err.toString())
        }
        // If successful -> functionality to get signed attestation form Mobile back-end
        return if (token is AppCheckToken) {
            AttestationResponse.Success(token.jwtToken)
            // If unsuccessful -> return the failure
        } else {
            token as AttestationResponse.Failure
        }
    }

    override suspend fun signAttestation(attestation: String): SignedResponse {
        // Not yet implemented
        return SignedResponse.Failure("Not yet implemented")
    }
}
