package uk.gov.android.authentication.integrity

import android.util.Log
import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.keymanager.ECKeyManager
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.model.AppCheckToken
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.model.SignedResponse
import uk.gov.android.authentication.integrity.usecase.AttestationCaller
import uk.gov.android.authentication.integrity.usecase.JWK
import java.security.SignatureException
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
class FirebaseClientAttestationManager(
    config: AppIntegrityConfiguration
) : ClientAttestationManager {
    private val appChecker: AppChecker = config.appChecker
    private val attestationCaller: AttestationCaller = config.attestationCaller
    private val keyStoreManager: KeyStoreManager = config.keyStoreManager

    override suspend fun getAttestation(): AttestationResponse {
        // Get Firebase token
        val token = appChecker.getAppCheckToken().getOrElse { err ->
            AttestationResponse.Failure(err.toString())
        }
        // If successful -> functionality to get signed attestation form Mobile back-end
        val pubKeyECCoord = keyStoreManager.getPublicKey()
        val jwk = JWK.makeJWK(x = pubKeyECCoord.first, y = pubKeyECCoord.second)
        return if (token is AppCheckToken) {
            attestationCaller.call(
                token.jwtToken,
                jwk
            )
            // If unsuccessful -> return the failure
        } else {
            token as AttestationResponse.Failure
        }
    }

    override suspend fun generatePoP(iss: String, aud: String): SignedResponse {
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
            SignedResponse.Success(signedPop)
        } catch (e: ECKeyManager.SigningError) {
            SignedResponse.Failure(e.message ?: VERIFF_ERROR, e)
        } catch (e: SignatureException) {
            SignedResponse.Failure(e.message ?: SIGN_ERROR, e)
        }
    }

    companion object {
        const val VERIFF_ERROR = "Verification Error"
        const val SIGN_ERROR = "Signing Error"
    }
}
