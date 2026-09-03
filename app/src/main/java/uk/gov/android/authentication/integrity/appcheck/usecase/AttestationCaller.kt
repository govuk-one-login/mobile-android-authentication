package uk.gov.android.authentication.integrity.appcheck.usecase

import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.json.jwk.JWK

/**
 * Interface to perform a network request that provides an Attestation from a custom or standard
 * backend service.
 *
 * It is used in the process of providing a ClientAttestation for the AppIntegrityCheck - allows
 * the integrity package to be encapsulated and not have any dependencies on any network .
 */
fun interface AttestationCaller {
    /**
     * Retrieves a ClientAttestation from the Mobile backend provided a Public Key and
     * a AppCheckToken.
     *
     * @param token - The AppCheckToken retrieved from [AppChecker].
     * @param jwk - The JSON Web Key (public key) that the PoP will be signed with.
     *
     * @return An [AttestationResponse] object indicating success or failure.
     *         On success, the response contains the signed attestation statement.
     */
    suspend fun call(
        token: String,
        jwk: JWK.JsonWebKey,
    ): AttestationResponse

    companion object {
        const val FIREBASE_HEADER = "X-Firebase-AppCheck"
        const val CONTENT_TYPE = "Content-type"
        const val CONTENT_TYPE_VALUE = "application/json"
    }
}
