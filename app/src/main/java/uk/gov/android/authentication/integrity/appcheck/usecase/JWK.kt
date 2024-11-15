package uk.gov.android.authentication.integrity.appcheck.usecase

import kotlinx.serialization.Serializable

@Suppress("MemberVisibilityCanBePrivate")
object JWK {
    private const val keyTypeValue = "EC"
    private const val useValue = "sig"
    private const val curveValue = "P-256"

    fun makeJWK(x: String, y: String): JsonWebKey = JsonWebKey(
        jwk = JsonWebKeyFormat(
            keyTypeValue,
            useValue,
            curveValue,
            x,
            y
        )
    )

    @Serializable
    data class JsonWebKey(
        val jwk: JsonWebKeyFormat
    )

    @Serializable
    data class JsonWebKeyFormat(
        val kty: String,
        val use: String,
        val crv: String,
        val x: String,
        val y: String
    )
}
