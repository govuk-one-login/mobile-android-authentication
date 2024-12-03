package uk.gov.android.authentication.integrity.appcheck.usecase

import kotlinx.serialization.Serializable

/**
 * Object to create a JWK following the required format:
 *
 * @param x - Elliptic Curve Point (ECPoint) x in Base64UrlEncoded format
 *          of the public key corresponding to the private key used to sign the PoP.
 * @param y - ECPoint y in Base64UrlEncoded format of the public key corresponding to the private
 *          key used to sign the PoP.
 *
 * @return A custom [JsonWebKey].
 */
@Suppress("MemberVisibilityCanBePrivate")
object JWK {
    private const val keyTypeValue = "EC"
    private const val useValue = "sig"
    private const val curveValue = "P-256"

    fun makeJWK(x: String, y: String): JsonWebKey {
        return JsonWebKey(
            jwk = JsonWebKeyFormat(
                keyTypeValue,
                useValue,
                curveValue,
                x,
                y
            )
        )
    }

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
