package uk.gov.android.authentication.login.refresh

import java.security.SignatureException
import uk.gov.android.authentication.integrity.FirebaseAppIntegrityManager.Companion.SIGN_ERROR
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.json.jwk.JWK

/**
 * Component allowing to create a signed Demonstrating Proof of Possession that can be used in the token exchange and retrieve a refresh token
 */
fun interface DemonstratingProofOfPossessionManager {
    /**
     * Function that creates the DPoP JWT (signed with the same key as for the AppIntegrity PoP).
     *
     * @param htu - provides the http request service url required to be added to the code
     */
    fun generateDPoP(htu: String): SignedDPoP
}

/**
 * Implementation class for creating the Demonstrating Proof of Possession
 *
 * @param config - Provides all components required to log and create the JWT
 */
class DemonstratingProofOfPossessionManagerImpl(
    private val config: DemonstratingProofOfPossessionConfig
) : DemonstratingProofOfPossessionManager {
    private val keyStoreManager: KeyStoreManager = config.keyStoreManager
    private val popGenerator = config.popGenerator
    private val logger = config.logger

    override fun generateDPoP(htu: String): SignedDPoP {
        val pubKeyECCoord = keyStoreManager.getPublicKeyCoordinates()
        val jwk = JWK.generateJwk(x = pubKeyECCoord.first, y = pubKeyECCoord.second)
        val dPoPJwt = popGenerator.createBase64DPoP(jwk, htu)
        val dPoPByteArray = dPoPJwt.toByteArray()
        return try {
            // Get signature to be appended to DPoPJwt
            val byteSignature = keyStoreManager.sign(dPoPByteArray)
            // Encode signature in Base64 configured with UrlSafe and no padding
            val signature = popGenerator.getUrlSafeNoPaddingBase64(byteSignature)
            // Return the signed PopJwt
            val signedDPop = "$dPoPJwt.$signature"
            println(signedDPop)
            // Check if PoP is expired before returning the result
            SignedDPoP.Success(signedDPop)
        } catch (e: SignatureException) {
            logger.error(
                e.javaClass.simpleName,
                e.message ?: SIGN_ERROR,
                e
            )
            SignedDPoP.Failure(e.message ?: SIGN_ERROR, e)
        }
    }
}
