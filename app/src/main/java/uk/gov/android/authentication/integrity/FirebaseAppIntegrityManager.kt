package uk.gov.android.authentication.integrity

import android.util.Log
import com.google.gson.JsonParser
import java.security.SignatureException
import kotlin.io.encoding.Base64
import kotlin.text.split
import uk.gov.android.authentication.integrity.appcheck.model.AppCheckToken
import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.integrity.appcheck.usecase.AppChecker
import uk.gov.android.authentication.integrity.appcheck.usecase.AttestationCaller
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.pop.SignedPoP
import uk.gov.android.authentication.json.jwk.JWK
import uk.gov.logging.api.Logger

class FirebaseAppIntegrityManager(
    private val logger: Logger,
    config: AppIntegrityConfiguration,
    private val popGenerator: ProofOfPossessionGenerator = ProofOfPossessionGenerator
) : AppIntegrityManager {
    private val appChecker: AppChecker = config.appChecker
    private val attestationCaller: AttestationCaller = config.attestationCaller
    private val keyStoreManager: KeyStoreManager = config.keyStoreManager

    override suspend fun getAttestation(): AttestationResponse {
        // Get Firebase token
        val token = appChecker.getAppCheckToken().getOrElse { err ->
            AttestationResponse.Failure(err.toString())
        }
        // If successful -> functionality to get signed attestation from Mobile back-end
        val pubKeyECCoord = keyStoreManager.getPublicKeyCoordinates()
        val jwk = JWK.generateJwk(x = pubKeyECCoord.first, y = pubKeyECCoord.second)
        return if (token is AppCheckToken) {
            attestationCaller.call(
                token.jwt,
                jwk
            )
            // If unsuccessful -> return the failure
        } else {
            token as AttestationResponse.Failure
        }
    }

    override fun generatePoP(iss: String, aud: String): SignedPoP {
        // Create Proof of Possession
        val expiry = popGenerator.getExpiryTime()
        val pop = popGenerator.createBase64PoP(iss, aud, expiry)
        // Convert into ByteArray
        val popByteArray = pop.toByteArray()
        return try {
            // Get signature to be appended to PoPJwt
            val byteSignature = keyStoreManager.sign(popByteArray)
            // Encode signature in Base64 configured with UrlSafe and no padding
            val signature = popGenerator.getUrlSafeNoPaddingBase64(byteSignature)
            // Return the signed PopJwt
            val signedPop = "$pop.$signature"
            // Check if PoP is expired before returning teh result
            if (popGenerator.isPopExpired(expiry)) {
                logger.error(
                    POP_TAG,
                    POP_ERROR_MSG,
                    Exception(POP_ERROR_MSG)
                )
            } else {
                logger.info(
                    POP_TAG,
                    POP_INFO_MSG
                )
            }
            SignedPoP.Success(signedPop)
        } catch (e: SignatureException) {
            SignedPoP.Failure(e.message ?: SIGN_ERROR, e)
        }
    }

    override fun verifyAttestationJwk(attestation: String): Boolean {
        // Get JWK from attestation
        val jwk = extractFieldFrom(attestation, "cnf")?.let {
            JsonParser.parseString(it).asJsonObject["jwk"]?.asJsonObject
        } ?: return false
        // Get local cert coordinates
        val (x, y) = keyStoreManager.getPublicKeyCoordinates()
        // Compare attestation with local cert
        return jwk["x"].asString == x && jwk["y"].asString == y
    }

    override fun getExpiry(attestation: String): Long? {
        return extractFieldFrom(attestation, "exp")?.toLongOrNull()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun extractFieldFrom(attestation: String, field: String): String? {
        return try {
            val body = String(
                Base64.withPadding(Base64.PaddingOption.ABSENT)
                    .decode(attestation.split(".")[1])
            )
            JsonParser.parseString(body).asJsonObject[field]?.toString()
        } catch (e: Exception) {
            Log.e(this::class.simpleName, e.message, e)
            null
        }
    }

    companion object {
        const val SIGN_ERROR = "Signing Error"
        const val POP_TAG = "ProofOfPossession"
        const val POP_ERROR_MSG = "Proof of Possession is expired"
        const val POP_INFO_MSG = "Proof of Possession is not expired after signing the JWT"
    }
}
