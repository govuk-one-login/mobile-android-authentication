package uk.gov.android.authentication.json.jwk

import kotlinx.serialization.Serializable
import org.jose4j.jwk.JsonWebKey
import java.security.Key

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
@Suppress("MemberVisibilityCanBePrivate", "unused")
object JWK {
    private const val keyTypeValue = "EC"
    private const val useValue = "sig"
    private const val curveValue = "P-256"

    /**
     * Method to get a PublicKey in JWK format that includes "use" field.
     * **Used in One Login app**
     * @param x - ECPoint in Base64Url format with no padding - 32 bits
     * @param y - ECPoint in Base64Url format with no padding - 32 bits
     * @return [JsonWebKey]
     */
    fun generateJwk(x: String, y: String): JsonWebKey {
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

    /**
     * Method to get a PublicKey in JWK format that uses the Jose library.
     * **To be used in Wallet**
     * @param key - ECPublicKey
     * @return [org.jose4j.jwk.JsonWebKey]
     */
    fun generateJwk(key: Key): org.jose4j.jwk.JsonWebKey {
        return org.jose4j.jwk.JsonWebKey.Factory.newJwk(key)
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
