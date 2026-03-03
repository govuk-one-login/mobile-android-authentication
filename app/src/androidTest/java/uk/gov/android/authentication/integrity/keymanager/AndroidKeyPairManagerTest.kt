package uk.gov.android.authentication.integrity.keymanager

import android.app.KeyguardManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.security.KeyStore
import kotlin.io.encoding.ExperimentalEncodingApi
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import uk.gov.android.authentication.integrity.keymanager.AndroidKeyPairManager.Companion.KEYSTORE
import uk.gov.logging.testdouble.SystemLogger

@OptIn(ExperimentalEncodingApi::class)
class AndroidKeyPairManagerTest {
    private val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private lateinit var keyStore: KeyStore
    private lateinit var keyPairManager: AndroidKeyPairManager
    private val logger = SystemLogger()
    private lateinit var keyguardManager: KeyguardManager
    private var isTestLockScreenEnabled: Boolean = false

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        keyStore =
            KeyStore.getInstance(KEYSTORE).apply {
                load(null)
            }

        keyPairManager = AndroidKeyPairManager(logger, false)
        keyguardManager = context.getSystemService(KeyguardManager::class.java)

        enableTestLockScreenIfRequired()
    }

    @After
    fun tearDown() {
        keyStore.deleteEntry(POP_KEY)

        disableTestLockScreenIfWasEnabled()
    }

    @Test
    fun check_getPublicKey() {
        val actual = keyPairManager.getPublicKey(POP_KEY)
        assertEquals(actual, keyStore.getCertificate(POP_KEY).publicKey)
    }

    @Test
    fun check_deleteAliasWhenInKeyStore() {
        val alias = "test-alias"
        val testPublicKey = keyPairManager.getPublicKey(alias)

        keyPairManager.deleteKeyFor(alias)

        val newTestPublicKey = keyPairManager.getPublicKey(alias)
        assertNotEquals(testPublicKey, newTestPublicKey)
    }

    @Test
    fun check_deleteAliasWhenNotPresentInKeyStore() {
        val alias = "test-alias"
        val testPublicKey = keyPairManager.getPublicKey(alias)

        val foreignAlias = "foreign-alias"
        keyPairManager.deleteKeyFor(foreignAlias)

        val actualTestPublicKey = keyPairManager.getPublicKey(alias)
        assertEquals(testPublicKey, actualTestPublicKey)
    }

    @Test
    fun check_deleteAllAliasesWhenForeignAlias() {
        val alias = "test-alias"
        val testPublicKey = keyPairManager.getPublicKey(alias)

        keyPairManager.deleteAllKeysWithPrefix(POP_KEY_PREFIX)

        val newTestPublicKey = keyPairManager.getPublicKey(alias)
        assertEquals(testPublicKey, newTestPublicKey)
    }

    @Test
    fun check_deleteAllAliasesWhenWalletPopKeyAlias() {
        val alias = POP_KEY
        val testPublicKey = keyPairManager.getPublicKey(alias)

        keyPairManager.deleteAllKeysWithPrefix(POP_KEY_PREFIX)

        val newTestPublicKey = keyPairManager.getPublicKey(alias)
        assertNotEquals(testPublicKey, newTestPublicKey)
    }

    private fun enableTestLockScreenIfRequired() {
        if (!keyguardManager.isDeviceSecure) {
            isTestLockScreenEnabled = true
            uiDevice.executeShellCommand("locksettings set-pin $PIN")
        }
    }

    private fun disableTestLockScreenIfWasEnabled() {
        if (isTestLockScreenEnabled) {
            uiDevice.executeShellCommand("locksettings clear --old $PIN")
            isTestLockScreenEnabled = false
        }
    }

    private companion object {
        private const val PIN = 1234
        private const val POP_KEY_PREFIX = "wallet-popkey-"
        private const val POP_KEY = "${POP_KEY_PREFIX}27ab7cca-7cf0-4aa7-a443-f37671f426c9"
    }
}
