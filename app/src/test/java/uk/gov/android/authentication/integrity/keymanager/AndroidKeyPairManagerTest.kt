package uk.gov.android.authentication.integrity.keymanager

import androidx.biometric.BiometricPrompt
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.Signature
import java.security.cert.Certificate
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.PromptConfig
import uk.gov.logging.testdouble.SystemLogger

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidKeyPairManagerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val logger = SystemLogger()
    private val keyStore: KeyStore = mock()
    private val keyPairGenerator: KeyPairGenerator = mock()
    private val keyPairManager =
        AndroidKeyPairManager.createForTesting(
            logger = logger,
            userAuthRequired = true,
            keyStore = keyStore,
            keyPairGenerator = keyPairGenerator,
            mainDispatcher = testDispatcher
        )

    @Test
    fun `getPublicKey - returns public key of existing POP key`() {
        val alias = "test-alias"
        given(keyStore.containsAlias(alias)).willReturn(true)
        val certificate: Certificate = mock()
        given(certificate.publicKey)
            .willReturn(KeyPairGenerator.getInstance("EC").generateKeyPair().public)
        given(keyStore.getCertificate(alias)).willReturn(certificate)

        keyPairManager.getPublicKey(alias)

        assertEquals(1, logger.size)
        assertEquals("alias: $alias - get public key", logger[0].message)
    }

    @Test
    fun `getPublicKey - returns public key of created POP key when no key in keystore`() {
        val alias = "test-alias"
        given(keyStore.containsAlias(alias)).willReturn(false)

        assertThrows<NullPointerException> {
            keyPairManager.getPublicKey(alias)
        }

        assertTrue(logger.contains("alias: $alias - create new POP key"))
        assertTrue(
            logger.contains("alias: $alias - KeyPair generated using software-backed KeyStore")
        )
        assertTrue(logger.contains("alias: $alias - get public key"))
    }

    @Test
    fun `getPublicKey - throws exception when uninitialized keystore`() {
        val alias = "test-alias"
        val message = "Uninitialized keystore"
        given(keyStore.containsAlias(alias)).willThrow(KeyStoreException(message))

        val ex =
            assertThrows<KeyStoreException> {
                keyPairManager.getPublicKey(alias)
            }

        assertEquals(message, ex.message)
    }

    @Test
    fun `deleteAlias - when alias exists in keystore`() {
        val alias = "test-alias"
        given(keyStore.containsAlias(alias)).willReturn(true)

        keyPairManager.deleteKeyFor(alias)

        assertEquals(1, logger.size)
        assertEquals("alias: $alias - deleted", logger[0].message)
    }

    @Test
    fun `deleteAlias - when alias does not exist in keystore`() {
        val alias = "test-alias"
        given(keyStore.containsAlias(alias)).willReturn(false)

        keyPairManager.deleteKeyFor(alias)

        assertEquals(1, logger.size)
        assertEquals("alias: $alias - it's not wallet alias", logger[0].message)
    }

    @Test
    fun `deleteAllAliases - when keystore contains wallet keys`() {
        val alias1 = "wallet-popkey-UUID1"
        val alias2 = "wallet-popkey-UUID2"
        val aliases = listOf(alias1, alias2)
        given(keyStore.aliases()).willReturn(Collections.enumeration(aliases))

        keyPairManager.deleteAllKeysWithPrefix(POP_KEY_PREFIX)

        assertEquals(2, logger.size)
        assertEquals("alias: $alias1 - deleted", logger[0].message)
        assertEquals("alias: $alias2 - deleted", logger[1].message)
    }

    @Test
    fun `deleteAllAliases - when keystore does not contain any wallet keys`() {
        val alias1 = "ol-popkey-UUID1"
        val alias2 = "secure-store"
        val aliases = listOf(alias1, alias2)
        given(keyStore.aliases()).willReturn(Collections.enumeration(aliases))

        keyPairManager.deleteAllKeysWithPrefix(POP_KEY_PREFIX)

        assertEquals(2, logger.size)
        assertEquals("alias: $alias1 - it's not wallet alias", logger[0].message)
        assertEquals("alias: $alias2 - it's not wallet alias", logger[1].message)
    }

    @Test
    fun `KEY_TIMEOUT_SECONDS is set to 15 seconds`() {
        assertEquals(15, AndroidKeyPairManager.KEY_TIMEOUT_SECONDS)
    }

    @Test
    fun `authenticateAndSign - success callback returns signed data`() =
        runTest {
            val alias = "test-alias"
            val data = "test-data".toByteArray()
            val authHandler: BiometricAuthHandler = mock()
            val promptConfig = PromptConfig("Title", "Close")
            val keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair()
            val entry = mock<KeyStore.PrivateKeyEntry>()
            val certificate: Certificate = mock()
            val signature =
                Signature.getInstance("SHA256withECDSA").apply {
                    initSign(keyPair.private)
                    update(data)
                }
            val cryptoObject: BiometricPrompt.CryptoObject = mock()
            val authenticationResult: BiometricPrompt.AuthenticationResult = mock()

            given(cryptoObject.signature).willReturn(signature)
            given(authenticationResult.cryptoObject).willReturn(cryptoObject)
            given(keyStore.getEntry(any(), anyOrNull())).willReturn(entry)
            given(entry.privateKey).willReturn(keyPair.private)
            given(keyStore.containsAlias(alias)).willReturn(true)
            given(certificate.publicKey).willReturn(keyPair.public)
            given(keyStore.getCertificate(alias)).willReturn(certificate)

            val requestCaptor = argumentCaptor<BiometricAuthHandler.Request>()
            doAnswer {
                val request = requestCaptor.firstValue
                request.callback.onSuccess(authenticationResult)
            }.`when`(authHandler).authenticate(requestCaptor.capture())

            val result =
                keyPairManager.authenticateAndSign(
                    SignRequest(alias, data),
                    promptConfig = promptConfig,
                    authHandler = authHandler
                )

            assertTrue(result.isNotEmpty())
            verify(authHandler).authenticate(any())
            verify(authHandler).close()
        }

    @Test
    fun `authenticateAndSign - error callback throws exception`() =
        runTest {
            val alias = "test-alias"
            val data = "test-data".toByteArray()
            val authHandler: BiometricAuthHandler = mock()
            val promptConfig = PromptConfig("Title", "Close")
            val keyPair = KeyPairGenerator.getInstance("EC").generateKeyPair()
            val entry = mock<KeyStore.PrivateKeyEntry>()
            val certificate: Certificate = mock()

            given(keyStore.getEntry(any(), anyOrNull())).willReturn(entry)
            given(entry.privateKey).willReturn(keyPair.private)
            given(keyStore.containsAlias(alias)).willReturn(true)
            given(certificate.publicKey).willReturn(keyPair.public)
            given(keyStore.getCertificate(alias)).willReturn(certificate)

            val requestCaptor = argumentCaptor<BiometricAuthHandler.Request>()
            doAnswer {
                val request = requestCaptor.firstValue
                request.callback.onError(1, "Error")
            }.`when`(authHandler).authenticate(requestCaptor.capture())

            val exception =
                assertThrows<Exception> {
                    keyPairManager.authenticateAndSign(
                        SignRequest(alias, data),
                        promptConfig = promptConfig,
                        authHandler = authHandler
                    )
                }

            assertEquals("Biometric authentication failed: 1 - Error", exception.message)
            verify(authHandler).close()
        }

    companion object {
        private const val POP_KEY_PREFIX = "wallet-popkey-"
    }
}
