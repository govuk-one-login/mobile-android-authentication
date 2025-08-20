package uk.gov.android.authentication.integrity.keymanager

import java.security.KeyStore
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.json.jwk.JWK
import uk.gov.android.authentication.json.jwt.Jose4jJwtVerifier

@OptIn(ExperimentalEncodingApi::class)
class ECKeyManagerTest {
    private lateinit var keyStore: KeyStore
    private lateinit var ecKeyManager: ECKeyManager
    private val jwtVerifier = Jose4jJwtVerifier()

    private val manuallyGeneratedTestJwtHeaderAndBody =
        "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJiWXJjdUFiY2RFZmdqZEVnWVNTYkJqd1h6SHJ3SiIsImF1ZCI6Imh" +
            "0dHBzOi8vZXhhbXBsZS5jb20iLCJleHAiOjE3MzMyNjE2MjYsImp0aSI6ImE3ZmNhMzRlLWJlNzI" +
            "tNDJmYi05MDY1LTRkNTVlOWMxNjljZSJ9"

    @BeforeTest
    fun setup() {
        keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        ecKeyManager = ECKeyManager()
    }

    @Test
    fun check_keys_created_on_initialization() {
        assertTrue(keyStore.containsAlias("app_check_keys"))
    }

    @Test
    fun check_getPublicKeyCoordinates() {
        val actual = ecKeyManager.getPublicKeyCoordinates()

        assertTrue(checkInputIsBase64(actual.first))
        assertTrue(checkInputIsBase64(actual.second))
    }

    @Test
    fun check_sign_success() {
        // When signing data
        val result = ecKeyManager.sign(manuallyGeneratedTestJwtHeaderAndBody.toByteArray())
        // And creating the JWT
        val jwt = "$manuallyGeneratedTestJwtHeaderAndBody.${
            ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(result)
        }"

        // And get public key in JWK format
        val ecPoints = ecKeyManager.getPublicKeyCoordinates()
        val jwk = JWK.generateJwk(ecPoints.first, ecPoints.second)

        // Then
        assertTrue(jwtVerifier.verify(jwt, Json.encodeToString(jwk.jwk)))
    }

    @Suppress("SwallowedException")
    private fun checkInputIsBase64(input: String): Boolean {
        return try {
            Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(input.toByteArray())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
