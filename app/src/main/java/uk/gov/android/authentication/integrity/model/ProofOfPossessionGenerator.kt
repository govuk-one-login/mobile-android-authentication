package uk.gov.android.authentication.integrity.model

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object ProofOfPossessionGenerator {
    @OptIn(ExperimentalUuidApi::class)
    fun createBase64PoP(
        iss: String,
        aud: String,
        exp: Long = getExpiryTime(),
        jti: String = Uuid.random().toString()
    ): String {
        val pop = ProofOfPossession(
            header = Header(alg = ALG),
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
        Log.d("HeaderJson", headerBase64)
        Log.d("PayloadJson", payloadBase64)
        // Return the PoP
        return "$headerBase64.$payloadBase64"
    }

    @Serializable
    data class ProofOfPossession(
        val header: Header,
        val payload: Payload
    )

    @Serializable
    data class Header(
        val alg: String
    )

    @Serializable
    data class Payload(
        val iss: String,
        val aud: String,
        val exp: Long,
        val jti: String
    )

    private fun getExpiryTime(): Long {
        return (System.currentTimeMillis() + (MINUTES * MINUTE_IN_MILLISECONDS))
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun getUrlSafeNoPaddingBase64(input: ByteArray): String {
        return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(input)
    }

    private const val ALG = "ES256"
    private const val MINUTES = 3
    private const val MINUTE_IN_MILLISECONDS = 60000
}
