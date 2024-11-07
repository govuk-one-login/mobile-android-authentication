package uk.gov.android.authentication.integrity

import android.security.keystore.KeyProperties
import java.security.KeyStore
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalEncodingApi::class)
class KeystoreManagerTest {
    private lateinit var keyStore: KeyStore
    private lateinit var keystoreManager: KeystoreManager

    @BeforeTest
    fun setup() {
        keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        keystoreManager = KeystoreManager()
    }

    @Test
    fun check_keys_created_on_initialization() {
        assertTrue(keyStore.containsAlias("app_check_keys"))
    }

    @Test
    fun hasAppCheckKeys() {
        val actual = keystoreManager.hasAppCheckKeys
        assertTrue(actual)
    }

    @Test
    fun appCheckPrivateKey() {
        val privateKey = keystoreManager.appCheckPrivateKey
        assertEquals(KeyProperties.KEY_ALGORITHM_EC, privateKey.algorithm)
    }

    @Test
    fun appCheckPublicKey() {
        val publicKey = keystoreManager.appCheckPublicKey
        assertEquals(KeyProperties.KEY_ALGORITHM_EC, publicKey.algorithm)
    }

    @Test
    fun check_getPubKeyBase64ECCoord() {
        val actual = keystoreManager.getPubKeyBase64ECCoord()

        assertTrue(checkInputIsBase64(actual.first))
        assertTrue(checkInputIsBase64(actual.second))
    }

    private fun checkInputIsBase64(input: String): Boolean {
        return try {
            Base64.decode(input)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}
