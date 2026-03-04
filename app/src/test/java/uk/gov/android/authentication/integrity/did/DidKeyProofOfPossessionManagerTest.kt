package uk.gov.android.authentication.integrity.did

import android.content.Context
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler
import uk.gov.android.authentication.integrity.keymanager.KeyPairManager
import uk.gov.android.authentication.integrity.keymanager.SignRequest
import uk.gov.android.authentication.integrity.keymanager.SignedData
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator

class DidKeyProofOfPossessionManagerTest {
    private val keyPairManager: KeyPairManager = mock()
    private val popGenerator: ProofOfPossessionGenerator = mock()
    private val context: Context = mock()
    private val authHandler: BiometricAuthHandler = mock()
    private val promptConfig: BiometricAuthHandler.PromptConfig = mock()
    private lateinit var manager: DidKeyProofOfPossessionManager

    @BeforeEach
    fun setUp() {
        given(context.getString(any())).willReturn("Unlock documents")

        manager = DidKeyProofOfPossessionManagerImpl(keyPairManager, popGenerator, promptConfig)
    }

    @Test
    fun `generatePoP returns valid JWT with three parts`() =
        runTest {
            setupMocks()

            val result =
                manager.generatePoP(
                    authHandler = authHandler,
                    alias = "test-alias",
                    aud = "https://example.com",
                    nonce = "test-nonce",
                    iss = ISS
                )

            val parts = result.split(".")
            assertEquals(3, parts.size, "Signed JWT should have header, payload, and signature")
        }

    @Test
    fun `generatePoP calls keyManager to get public key coordinates`() =
        runTest {
            setupMocks()

            manager.generatePoP(
                authHandler = authHandler,
                alias = "test-alias",
                aud = "https://example.com",
                nonce = "test-nonce",
                iss = ISS
            )

            verify(keyPairManager).getPublicKeyCoordinates("test-alias")
        }

    @Test
    fun `generatePoP calls popGenerator with correct parameters`() =
        runTest {
            setupMocks()

            manager.generatePoP(
                authHandler = authHandler,
                alias = "test-alias",
                aud = "https://example.com",
                nonce = "test-nonce",
                iss = ISS
            )

            verify(popGenerator).createBase64DidKeyPoP(
                kid = any(),
                nonce = eq("test-nonce"),
                aud = eq("https://example.com"),
                iss = eq(ISS)
            )
        }

    @Test
    fun `generatePoP calls keyManager sign with unsigned JWT`() =
        runTest {
            setupMocks()

            manager.generatePoP(
                authHandler = authHandler,
                alias = "test-alias",
                aud = "https://example.com",
                nonce = "test-nonce",
                iss = ISS
            )

            val captor = argumentCaptor<Array<SignRequest>>()
            verify(keyPairManager).authenticateAndSign(
                *captor.capture(),
                promptConfig = any(),
                authHandler = any()
            )
            assertEquals("test-alias", captor.firstValue[0].keyAlias)
        }

    @Test
    fun `generatePoP signature is base64 URL safe encoded`() =
        runTest {
            setupMocks()

            val result =
                manager.generatePoP(
                    authHandler = authHandler,
                    alias = "test-alias",
                    aud = "https://example.com",
                    nonce = "test-nonce",
                    iss = ISS
                )

            val signature = result.split(".")[2]

            assertFalse(signature.contains("+"))
            assertFalse(signature.contains("/"))
            assertFalse(signature.contains("="))
        }

    @Test
    fun `generatePoP with different aliases produces different results`() =
        runTest {
            given(keyPairManager.getPublicKeyCoordinates("alias1"))
                .willReturn(
                    Pair(
                        encodeBase64(
                            ByteArray(32) {
                                1
                            }
                        ),
                        encodeBase64(ByteArray(32) { 2 })
                    )
                )
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                "header.payload"
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("alias1", byteArrayOf(1, 2, 3))))
            val result1 = manager.generatePoP(
                authHandler,
                "alias1",
                "https://example.com",
                "nonce",
                ISS
            )

            given(keyPairManager.getPublicKeyCoordinates("alias2"))
                .willReturn(
                    Pair(
                        encodeBase64(
                            ByteArray(32) {
                                3
                            }
                        ),
                        encodeBase64(ByteArray(32) { 4 })
                    )
                )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("alias2", byteArrayOf(4, 5, 6))))
            val result2 = manager.generatePoP(
                authHandler,
                "alias2",
                "https://example.com",
                "nonce",
                ISS
            )

            assertNotEquals(result1, result2)
        }

    @Test
    fun `generatePoP with different audiences produces different results`() =
        runTest {
            val xBytes = ByteArray(32) { 1 }
            val yBytes = ByteArray(32) { 2 }

            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(xBytes), encodeBase64(yBytes)))
            given(
                popGenerator.createBase64DidKeyPoP(any(), any(), eq("https://example1.com"), any())
            )
                .willReturn("header1.payload1")
            given(
                popGenerator.createBase64DidKeyPoP(any(), any(), eq("https://example2.com"), any())
            )
                .willReturn("header2.payload2")
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3, 4, 5))))

            val result1 = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example1.com",
                "nonce",
                ISS
            )
            val result2 = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example2.com",
                "nonce",
                ISS
            )

            assertNotEquals(result1, result2)
        }

    @Test
    fun `generatePoP with different nonces produces different results`() =
        runTest {
            val xBytes = ByteArray(32) { 1 }
            val yBytes = ByteArray(32) { 2 }

            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(xBytes), encodeBase64(yBytes)))
            given(popGenerator.createBase64DidKeyPoP(any(), eq("nonce1"), any(), any()))
                .willReturn("header1.payload1")
            given(popGenerator.createBase64DidKeyPoP(any(), eq("nonce2"), any(), any()))
                .willReturn("header2.payload2")
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3, 4, 5))))

            val result1 = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example.com",
                "nonce1",
                ISS
            )
            val result2 = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example.com",
                "nonce2",
                ISS
            )

            assertNotEquals(result1, result2)
        }

    @Test
    fun `generatePoP handles even parity Y coordinate`() =
        runTest {
            val yBytesEven = byteArrayOf(1, 2, 3, 4)
            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(ByteArray(32) { 1 }), encodeBase64(yBytesEven)))
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                "header.payload"
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3))))

            val result = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example.com",
                "nonce",
                ISS
            )

            assertNotNull(result)
            verify(keyPairManager).getPublicKeyCoordinates("test-alias")
        }

    @Test
    fun `generatePoP handles odd parity Y coordinate`() =
        runTest {
            val yBytesOdd = byteArrayOf(1, 2, 3, 5)
            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(ByteArray(32) { 1 }), encodeBase64(yBytesOdd)))
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                "header.payload"
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3))))

            val result = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example.com",
                "nonce",
                ISS
            )

            assertNotNull(result)
            verify(keyPairManager).getPublicKeyCoordinates("test-alias")
        }

    @Test
    fun `generatePoP creates DID key with correct format`() =
        runTest {
            val xBytes = ByteArray(32) { it.toByte() }
            val yBytesEven = byteArrayOf(1, 2, 3, 4)

            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(xBytes), encodeBase64(yBytesEven)))
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                "header.payload"
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3))))

            manager.generatePoP(authHandler, "test-alias", "https://example.com", "nonce", ISS)

            val kidCaptor = argumentCaptor<String>()
            verify(popGenerator).createBase64DidKeyPoP(kidCaptor.capture(), any(), any(), any())
            assertTrue(kidCaptor.firstValue.startsWith("did:key:z"))
        }

    @Test
    fun `generatePoP throws exception when keyManager fails to get coordinates`() =
        runTest {
            given(keyPairManager.getPublicKeyCoordinates(any())).willThrow(
                RuntimeException("Key not found")
            )

            assertThrows<RuntimeException> {
                manager.generatePoP(authHandler, "test-alias", "https://example.com", "nonce", ISS)
            }
        }

    @Test
    fun `generatePoP throws exception when keyManager fails to sign`() =
        runTest {
            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(ByteArray(32)), encodeBase64(ByteArray(32))))
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                "header.payload"
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willThrow(RuntimeException("Signing failed"))

            assertThrows<RuntimeException> {
                manager.generatePoP(authHandler, "test-alias", "https://example.com", "nonce", ISS)
            }
        }

    @Test
    fun `generatePoP result can be split into header payload and signature`() =
        runTest {
            setupMocks()

            val result =
                manager.generatePoP(
                    authHandler = authHandler,
                    alias = "test-alias",
                    aud = "https://example.com",
                    nonce = "test-nonce",
                    iss = ISS
                )

            val parts = result.split(".")
            assertEquals(3, parts.size)
            assertTrue(parts[0].isNotEmpty(), "Header should not be empty")
            assertTrue(parts[1].isNotEmpty(), "Payload should not be empty")
            assertTrue(parts[2].isNotEmpty(), "Signature should not be empty")
        }

    @Test
    fun `generatePoP signature is appended to unsigned JWT`() =
        runTest {
            val unsignedJwt = "header.payload"
            val signature = byteArrayOf(1, 2, 3, 4, 5)

            given(keyPairManager.getPublicKeyCoordinates(any()))
                .willReturn(Pair(encodeBase64(ByteArray(32)), encodeBase64(ByteArray(32))))
            given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
                unsignedJwt
            )
            given(
                keyPairManager.authenticateAndSign(
                    anyVararg(),
                    promptConfig = any(),
                    authHandler = any()
                )
            ).willReturn(listOf(SignedData("test-alias", signature)))

            val result = manager.generatePoP(
                authHandler,
                "test-alias",
                "https://example.com",
                "nonce",
                ISS
            )

            assertTrue(result.startsWith(unsignedJwt))
            val parts = result.split(".")
            assertEquals(3, parts.size)
        }

    @OptIn(ExperimentalEncodingApi::class)
    private fun encodeBase64(bytes: ByteArray): String =
        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(bytes)

    private suspend fun setupMocks() {
        val xBytes = ByteArray(32) { 1 }
        val yBytes = ByteArray(32) { 2 }

        given(keyPairManager.getPublicKeyCoordinates(any()))
            .willReturn(Pair(encodeBase64(xBytes), encodeBase64(yBytes)))
        given(popGenerator.createBase64DidKeyPoP(any(), any(), any(), any())).willReturn(
            "header.payload"
        )
        given(
            keyPairManager.authenticateAndSign(
                anyVararg(),
                promptConfig = any(),
                authHandler = any()
            )
        ).willReturn(listOf(SignedData("test-alias", byteArrayOf(1, 2, 3, 4, 5))))
    }

    companion object {
        private const val ISS = "urn:fdc:gov:uk:test-iss"
    }
}
