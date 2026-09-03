package uk.gov.android.authentication.integrity.did

import kotlin.io.encoding.Base64
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.PromptConfig
import uk.gov.android.authentication.integrity.keymanager.KeyPairManager
import uk.gov.android.authentication.integrity.keymanager.SignRequest
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64

/**
 * Manager for generating DID key-based Proof of Possession (PoP) JWTs.
 *
 * This interface provides functionality to create signed PoP tokens using DID (Decentralized Identifier)
 * keys with biometric authentication. The generated PoP is a signed JWT that proves possession of the
 * private key corresponding to a DID key identifier.
 */
fun interface DidKeyProofOfPossessionManager {
    /**
     * Generates a signed Proof of Possession JWT using a DID key.
     *
     * This method creates a PoP JWT with the specified claims, signs it using the key identified by
     * [alias] after biometric authentication, and returns the complete signed JWT string.
     *
     * @param authHandler Handler for biometric authentication UI
     * @param alias The key alias in the KeyStore to use for signing
     * @param aud The audience claim - the intended recipient of the PoP
     * @param nonce A unique nonce value to prevent replay attacks
     * @param iss The issuer claim - the entity generating the PoP
     * @return A signed JWT string in the format: header.payload.signature
     * @throws BiometricAuthException if biometric authentication fails
     * @throws KeySigningException if signing the PoP fails
     */
    suspend fun generatePoP(
        authHandler: BiometricAuthHandler,
        alias: String,
        aud: String,
        nonce: String,
        iss: String
    ): String
}

/**
 * Default implementation of [DidKeyProofOfPossessionManager].
 *
 * This implementation generates DID key-based Proof of Possession JWTs by:
 * 1. Converting the EC public key to a compressed DID key format
 * 2. Creating an unsigned JWT with the DID key as the key identifier (kid)
 * 3. Signing the JWT using biometric authentication
 *
 * @param keyPairManager Manager for key operations and biometric authentication
 * @param popGenerator Generator for creating PoP JWT payloads
 * @param promptConfig Configuration for the biometric authentication prompt
 */
class DidKeyProofOfPossessionManagerImpl(
    private val keyPairManager: KeyPairManager,
    private val popGenerator: ProofOfPossessionGenerator,
    private val promptConfig: PromptConfig
) : DidKeyProofOfPossessionManager {

    override suspend fun generatePoP(
        authHandler: BiometricAuthHandler,
        alias: String,
        aud: String,
        nonce: String,
        iss: String
    ): String {
        // Create Proof of Possession
        val kid = getDidKey(alias)
        val unsignedPoPJwt = popGenerator.createBase64DidKeyPoP(kid, nonce, aud, iss)

        // Get signature to be appended to PoPJwt
        val (signedData) =
            keyPairManager.authenticateAndSign(
                SignRequest(alias, unsignedPoPJwt.toByteArray()),
                promptConfig = promptConfig,
                authHandler = authHandler
            )
        val signatureBytes = signedData.signature
        // Encode signature in Base64 configured with UrlSafe and no padding
        val signatureBase64 = getUrlSafeNoPaddingBase64(signatureBytes)
        // Return the signed PopJwt
        val signedPoPJwt = "$unsignedPoPJwt.$signatureBase64"
        return signedPoPJwt
    }

    private fun getDidKey(alias: String): String {
        val coordinates = keyPairManager.getPublicKeyCoordinates(alias)
        val base64Decoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
        val xBytes = base64Decoder.decode(coordinates.first)
        val yBytes = base64Decoder.decode(coordinates.second)
        val prefix = if (yBytes.last().hasEvenParity()) {
            EC_POINT_COMPRESSION_EVEN
        } else {
            EC_POINT_COMPRESSION_ODD
        }
        val compressedKey = byteArrayOf(prefix.toByte()) + xBytes
        return DidKeyEncoder.encodeDidKey(compressedKey)
    }

    private fun Byte.hasEvenParity() = (this.toInt() and 1) == 0

    companion object {
        private const val EC_POINT_COMPRESSION_EVEN = 0x02
        private const val EC_POINT_COMPRESSION_ODD = 0x03
    }
}
