package uk.gov.android.authentication.integrity

import android.security.keystore.KeyProperties
import kotlin.test.BeforeTest
import java.security.KeyStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
}
