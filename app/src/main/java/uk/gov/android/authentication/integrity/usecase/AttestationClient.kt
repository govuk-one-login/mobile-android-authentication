package uk.gov.android.authentication.integrity.usecase

import org.jose4j.jwk.JsonWebKey

fun interface AttestationClient {
    suspend fun attest(popJWT: String, request: JsonWebKey): Result<Response>

    data class Response(val jwt: String, val expiresIn: Long)

    companion object {
        @Suppress("unused")
        protected const val FIREBASE_HEADER = "X-Firebase-AppCheck"
    }
}
