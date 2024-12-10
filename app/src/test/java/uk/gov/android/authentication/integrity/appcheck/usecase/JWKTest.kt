package uk.gov.android.authentication.integrity.appcheck.usecase

import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jose4j.jwk.JsonWebKey
import uk.gov.android.authentication.json.jwk.JWK

class JWKTest {
    @Test
    fun `generateJWK with ECCoord provided sets defaults`() {
        val actual = JWK.generateJwk(X, Y)
        assertEquals(jwk, actual)
    }

    @Test
    fun `generateJWK with Key provided returns expected`() {
        val keyInBytes = byteArrayOf(
            48, 89, 48, 19, 6, 7, 42, -122, 72, -50, 61, 2, 1, 6, 8, 42,
            -122, 72, -50, 61, 3, 1, 7, 3, 66, 0, 4, -45, 113, 67, 94, -79, 85, -108, 14, -79, -116,
            0, 101, -101, -60, 32, 60, -20, -128, 70, -65, -62, -54, 113, -13, 53, 14, 39, -18, 56,
            25, -83, 94, -102, -33, -118, 27, -89, 80, -51, 21, -54, -58, -19, -118, -30, -47, 94,
            36, -57, -114, -17, -52, -94, 125, -85, -110, -54, -50, -18, 17, -14, 82, 1, 32
        )
        val key = createMockKey(keyInBytes)
        val actual = JWK.generateJwk(key)
        assertEquals(joseJwk.toJson(), actual.toJson())
    }

    companion object {
        private const val X = "18wHLeIgW9wVN6VD1Txgpqy2LszYkMf6J8njVAibvhM"
        private const val Y = "-V4dS4UaLMgP_4fY4j8ir7cl1TXlFdAgcx55o7TkcSA"
        private val jwk = JWK.JsonWebKey(
            jwk = JWK.JsonWebKeyFormat(
                "EC",
                "sig",
                "P-256",
                X,
                Y
            )
        )
        private val joseJwk = JsonWebKey.Factory.newJwk(
            mapOf(
                "kty" to "EC",
                "x" to "03FDXrFVlA6xjABlm8QgPOyARr_CynHzNQ4n7jgZrV4",
                "y" to "mt-KG6dQzRXKxu2K4tFeJMeO78yifauSys7uEfJSASA",
                "crv" to "P-256"
            )
        )
        private fun createMockKey(key: ByteArray): PublicKey {
            val factory = KeyFactory.getInstance("EC")
            return factory.generatePublic(
                X509EncodedKeySpec(key)
            )
        }
    }
}
