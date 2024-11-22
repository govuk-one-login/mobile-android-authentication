package uk.gov.android.authentication.integrity

import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.integrity.pop.SignedPoP

/**
 * Interface to manage the process of completing steps required for an App Integrity
 * journey.
 */
interface AppIntegrityManager {

    /**
     * Retrieves a signed attestation statement from the Mobile backend.
     *
     * This method first obtains an App Check token and then uses it to request
     * a signed attestation from the backend. The attestation statement verifies
     * the integrity of the app.
     *
     * @return An [AttestationResponse] object indicating success or failure.
     *         On success, the response contains the signed attestation statement.
     */
    suspend fun getAttestation(): AttestationResponse

    /**
     * Generates a Proof-of-Possession (PoP) JSON Web Token (JWT).
     *
     * The PoP JWT proves possession of the private key associated with the
     * public key used for attestation. It's signed using the private key
     * and includes the 'issuer' and 'audience' claims.
     *
     * @param iss The issuer of the PoP JWT (usually your backend server identifier).
     * @param aud The audience of the PoP JWT (usually the resource server).
     * @return A [SignedPoP] object indicating success or failure.
     *         On success, the response contains the signed PoP JWT.
     */
    fun generatePoP(iss: String, aud: String): SignedPoP

    /**
     * Verifies the JSON Web Key (JWK) in the attestation statement.
     *
     * This method extracts the JWK from the provided attestation statement
     * and compares it against the locally stored public key.
     *
     * @param attestation The attestation statement containing the JWK.
     * @return `true` if the JWK matches the local public key, `false` otherwise.
     */
    fun verifyAttestationJwk(attestation: String): Boolean

    /**
     * Retrieves the expiry time from the attestation statement.
     *
     * @param attestation The attestation statement containing the expiry time.
     * @return The expiry time as a Long value (Unix timestamp in milliseconds),
     *         or `null` if the expiry time is not found or invalid.
     */
    fun getExpiry(attestation: String): Long?
}
