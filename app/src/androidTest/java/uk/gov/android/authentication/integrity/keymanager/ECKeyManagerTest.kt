package uk.gov.android.authentication.integrity.keymanager

import org.junit.Assert.assertThrows
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
        val signature = ecKeyManager.sign("Success".toByteArray())

        assertTrue(ecKeyManager.verify("Success".toByteArray(), signature))
    }



    @JUnitTest
    fun check_verify_failure() {
        assertThrows(ECKeyManager.SigningError.InvalidSignature::class.java) {
            ecKeyManager.verify("Success".toByteArray(), "Success".toByteArray())
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
