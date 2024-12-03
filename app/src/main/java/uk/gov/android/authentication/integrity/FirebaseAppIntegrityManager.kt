package uk.gov.android.authentication.integrity

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.android.authentication.integrity.appcheck.usecase.AppChecker
import uk.gov.android.authentication.integrity.keymanager.ECKeyManager
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.appcheck.model.AppCheckToken
import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.pop.SignedPoP
import uk.gov.android.authentication.integrity.appcheck.usecase.AttestationCaller
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.appcheck.usecase.JWK
import java.security.SignatureException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.text.split

@OptIn(ExperimentalEncodingApi::class)
class FirebaseAppIntegrityManager(
    config: AppIntegrityConfiguration
) : AppIntegrityManager {
    private val appChecker: AppChecker = config.appChecker
    private val attestationCaller: AttestationCaller = config.attestationCaller
    private val keyStoreManager: KeyStoreManager = config.keyStoreManager
    private val pubKeyECCoord = keyStoreManager.getPublicKey()
    private val jwk = JWK.makeJWK(x = pubKeyECCoord.first, y = pubKeyECCoord.second)

    override suspend fun getAttestation(): AttestationResponse {
        // Get Firebase token
        val token = appChecker.getAppCheckToken().getOrElse { err ->
            AttestationResponse.Failure(err.toString())
        }
        // If successful -> functionality to get signed attestation from Mobile back-end
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
        val pop = ProofOfPossessionGenerator.createBase64PoP(iss, aud)
        // Convert into ByteArray
        val popByteArray = pop.toByteArray()
        return try {
            // Get signature to be appended to PoPJwt
            val byteSignature = keyStoreManager.sign(popByteArray)
            // Encode signature in Base64 configured with UrlSafe and no padding
            val signature = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(byteSignature)
            // Return the signed PopJwt
            val signedPop = "$pop.$signature"
            Log.d("SignedPoP", signedPop)
            Log.d(
                "VerifySignePoP",
                "${keyStoreManager.verify(signedPop, Json.encodeToString(jwk.jwk))}"
            )
            SignedPoP.Success(signedPop)
        } catch (e: ECKeyManager.SigningError) {
            SignedPoP.Failure(e.message ?: VERIFF_ERROR, e)
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
        val (x, y) = keyStoreManager.getPublicKey()
        // Compare attestation with local cert
        return jwk["x"].asString == x && jwk["y"].asString == y
    }

    override fun getExpiry(attestation: String): Long? {
        return extractFieldFrom(attestation, "exp")?.toLongOrNull()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun extractFieldFrom(attestation: String, field: String): String? {
        return try {
            val body = String(Base64.withPadding(Base64.PaddingOption.ABSENT)
                .decode(attestation.split(".")[1]))
            JsonParser.parseString(body).asJsonObject[field]?.toString()
        } catch (e: Exception) {
            Log.e(this::class.simpleName, e.message, e)
            null
        }
    }

    companion object {
        const val VERIFF_ERROR = "Verification Error"
        const val SIGN_ERROR = "Signing Error"
    }
}
