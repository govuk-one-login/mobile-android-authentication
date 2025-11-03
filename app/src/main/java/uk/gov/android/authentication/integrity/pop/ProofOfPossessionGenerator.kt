package uk.gov.android.authentication.integrity.pop

import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import uk.gov.android.authentication.json.jwk.JWK
import java.time.temporal.ChronoUnit

object ProofOfPossessionGenerator {
    /**
     * Function that created the Base 64 for the Proof of Possession used for the AppIntegrity checks.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun createBase64PoP(
        iss: String,
        aud: String,
        exp: Long,
        jti: String = Uuid.random().toString()
    ): String {
        val pop = ProofOfPossession(
            header = Header(
                alg = ALG,
                typ = APP_INTEGRITY_TYP
            ),
            payload = Payload(
                iss = iss,
                aud = aud,
                exp = exp,
                jti = jti
            )
        )
        // Convert into ByteArray
        val headerByteArray = Json.encodeToString(pop.header).toByteArray()
        val payloadByteArray = Json.encodeToString(pop.payload).toByteArray()
        // Get Base64 configured with UrlSafe and no padding
        val headerBase64 = getUrlSafeNoPaddingBase64(headerByteArray)
        val payloadBase64 = getUrlSafeNoPaddingBase64(payloadByteArray)
        // Return the PoP
        return "$headerBase64.$payloadBase64"
    }

    /**
     * Function that created the Base 64 for the Proof of Possession used for the refresh token exchange.
     */
    @OptIn(ExperimentalUuidApi::class)
    fun createBase64DPoP(
        jwk: JWK.JsonWebKey,
        htu: String,
        jti: String = Uuid.random().toString(),
        iat: Long = getIssueTime()
    ): String {
        val pop = DemonstratingProofOfPossession(
            header = DPoPHeader(
                alg = ALG,
                typ = REFRESH_TYP,
                jwk = jwk.jwk
            ),
            payload = DemonstratingPoPPayload(
                jti = jti,
                htm = HTTP_METHOD,
                htu = htu,
                iat = iat
            )
        )
        // Convert into ByteArray
        val headerByteArray = Json.encodeToString(pop.header).toByteArray()
        val payloadByteArray = Json.encodeToString(pop.payload).toByteArray()
        // Get Base64 configured with UrlSafe and no padding
        val headerBase64 = getUrlSafeNoPaddingBase64(headerByteArray)
        val payloadBase64 = getUrlSafeNoPaddingBase64(payloadByteArray)
        // Return the PoP
        return "$headerBase64.$payloadBase64"
    }

    @Serializable
    data class ProofOfPossession(
        val header: Header,
        val payload: Payload
    )

    @Serializable
    data class DemonstratingProofOfPossession(
        val header: DPoPHeader,
        val payload: DemonstratingPoPPayload
    )

    @Serializable
    data class Header(
        val alg: String,
        val typ: String
    )

    @Serializable
    data class DPoPHeader(
        val alg: String,
        val typ: String,
        val jwk: JWK.JsonWebKeyFormat
    )

    @Serializable
    data class Payload(
        val iss: String,
        val aud: String,
        val exp: Long,
        val jti: String
    )

    @Serializable
    data class DemonstratingPoPPayload(
        val jti: String,
        val htm: String,
        val htu: String,
        val iat: Long
    )

    fun getExpiryTime(): Long {
        val expiry = Instant
            .now()
            .plus(3, ChronoUnit.MINUTES)
            .epochSecond
        return expiry
    }

    fun getIssueTime(): Long {
        return Instant.now().epochSecond
    }

    fun getUrlSafeNoPaddingBase64(input: ByteArray): String {
        return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(input)
    }

    fun isPopExpired(exp: Long): Boolean {
        return exp <= Instant.now().epochSecond
    }

    private const val ALG = "ES256"
    private const val APP_INTEGRITY_TYP = "oauth-client-attestation-pop+jwt"
    private const val REFRESH_TYP = "dpop+jwt"
    private const val HTTP_METHOD = "POST"
}
