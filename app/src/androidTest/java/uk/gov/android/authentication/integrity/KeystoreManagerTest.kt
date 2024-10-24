package uk.gov.android.authentication.integrity

import org.junit.Before
import java.security.KeyStore
import kotlin.test.Test
import kotlin.test.assertTrue

class KeystoreManagerTest {
    private lateinit var keyStore: KeyStore
    private lateinit var keystoreManager: KeystoreManager

    @Before
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
}
