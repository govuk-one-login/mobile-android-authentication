package uk.gov.android.authentication.integrity.keymanager

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertThrows
import uk.gov.android.authentication.integrity.appcheck.usecase.JWK
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import org.junit.Test as JUnitTest
import java.security.KeyStore
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class ECKeyManagerTest {
    private lateinit var keyStore: KeyStore
    private lateinit var ecKeyManager: ECKeyManager

    private val headerAndBody = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJiWXJjdVJWdm55bHZFZ1lTU2JCandYekhyd0oiLCJ" +
            "hdWQiOiJodHRwczovL3Rva2VuLmJ1aWxkLmFjY291bnQuZ292LnVrIiwiZXhwIjoxNzMzMjYxNjI2LC" +
            "JqdGkiOiIxM2YxZTA3NC1jMmY4LTRlZDktYjk1NC1lYjZjMjAwZjVjMGUifQ"
    private val jwt = "eyJhbGciOiJFUzI1NiJ9.eyJpc3MiOiJiWXJjdVJWdm55bHZFZ1lTU2JCandYekhyd0oiLCJ" +
            "hdWQiOiJodHRwczovL3Rva2VuLmJ1aWxkLmFjY291bnQuZ292LnVrIiwiZXhwIjoxNzMzMjYxNjI2LC" +
            "JqdGkiOiIxM2YxZTA3NC1jMmY4LTRlZDktYjk1NC1lYjZjMjAwZjVjMGUifQ.oAgdAcHuaQyS7s3QMhk" +
            "GdUwTlwJBBnCyee4NuXVK9a0g4fDQRO6h_VlwfWenJr_ydcA5M4a4f2ARcQP3iCQgmA"

    private val invalidPublicKeyJwk = "{" +
            "\"kty\":\"EC\"," +
            "\"use\":\"sig\"," +
            "\"crv\":\"P-256\"," +
            "\"x\":\"mp8hc5ZveAA4ZMGWnpeDSa3Y0w4pFDjLQMrN9-shuab\"," +
            "\"y\":\"VRFdiJgOO9q4pcVFJiQoWRj_YIqFOoE1FWoR1_IhdaS\"}"

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
    fun check_getPublicKey() {
        val actual = ecKeyManager.getPublicKey()

        assertTrue(checkInputIsBase64(actual.first))
        assertTrue(checkInputIsBase64(actual.second))
    }

    @Test
    fun check_sign_success() {
        // When signing data
        val result = ecKeyManager.sign(headerAndBody.toByteArray())
        // And creating the JWT
        val jwt = "$headerAndBody.${ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(result)}"

        // And get public key in JWK format
        val ecPoints = ecKeyManager.getPublicKey()
        val jwk = JWK.makeJWK(ecPoints.first, ecPoints.second)

        // Then
        assertTrue(ecKeyManager.verify(jwt, Json.encodeToString(jwk.jwk)))
    }

    @JUnitTest
    fun check_verify_failure() {
        assertThrows(ECKeyManager.SigningError.InvalidSignature::class.java) {
            ecKeyManager.verify(jwt, invalidPublicKeyJwk)
        }
    }

    @Suppress("SwallowedException")
    private fun checkInputIsBase64(input: String): Boolean {
        return try {
            ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(input.toByteArray())
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
