package uk.gov.android.authentication.login.refresh

import java.security.SignatureException
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.logging.api.Logger

class DemonstratingProofOfPossessionManagerTest {
    private val expectedDPoP = ClassLoader.getSystemResource("bodyDPoPBase64.txt")
        .readText()
    private lateinit var dPoPManager: DemonstratingProofOfPossessionManager

    private val mockLogger: Logger = mock()
    private val mockKeyStoreManager: KeyStoreManager = mock()
    private val mockPopGenerator: ProofOfPossessionGenerator = mock()

    @BeforeEach
    fun setup() {
        val config = DemonstratingProofOfPossessionConfig(
            logger = mockLogger,
            popGenerator = mockPopGenerator,
            keyStoreManager = mockKeyStoreManager
        )

        dPoPManager = DemonstratingProofOfPossessionManagerImpl(config)
    }

    @Test
    fun testGenerateDPoPSuccess() {
        val mockSignatureByte = "Success".toByteArray()
        val mockSignatureBase64 = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(
            mockSignatureByte
        )

        whenever(mockKeyStoreManager.getPublicKeyCoordinates())
            .thenReturn(Pair("Success", "Success"))
        whenever(mockPopGenerator.createBase64DPoP(any(), any(), any())).thenReturn(expectedDPoP)
        whenever(mockPopGenerator.getUrlSafeNoPaddingBase64(any())).thenReturn(mockSignatureBase64)
        whenever(mockKeyStoreManager.sign(any())).thenReturn(mockSignatureByte)

        val result = dPoPManager.generateDPoP()

        assertEquals(SignedDPoP.Success("$expectedDPoP.$mockSignatureBase64"), result)
    }

    @Test
    fun testGenerateDPoPFailure() {
        val exp = SignatureException("Filed signing dpop!")

        whenever(mockPopGenerator.createBase64DPoP(any(), any(), any())).thenReturn(expectedDPoP)
        whenever(mockKeyStoreManager.getPublicKeyCoordinates())
            .thenReturn(Pair("Success", "Success"))
        whenever(mockKeyStoreManager.sign(any())).thenAnswer {
            throw exp
        }

        val result = dPoPManager.generateDPoP()

        assertEquals(SignedDPoP.Failure(exp.message!!, exp), result)
    }
}
