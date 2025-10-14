package uk.gov.android.authentication.integrity

import java.security.SignatureException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.FirebaseAppIntegrityManager.Companion.POP_ERROR_MSG
import uk.gov.android.authentication.integrity.FirebaseAppIntegrityManager.Companion.POP_INFO_MSG
import uk.gov.android.authentication.integrity.FirebaseAppIntegrityManager.Companion.POP_TAG
import uk.gov.android.authentication.integrity.appcheck.model.AppCheckToken
import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse
import uk.gov.android.authentication.integrity.appcheck.usecase.AppChecker
import uk.gov.android.authentication.integrity.appcheck.usecase.AttestationCaller
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.pop.SignedPoP
import uk.gov.android.keystore.KeyStoreManager
import uk.gov.logging.api.Logger

class FirebaseAppIntegrityManagerTest {
    private val expectedResult = ClassLoader.getSystemResource("bodyPoPBase64.txt")
        .readText()
    private lateinit var appIntegrityManager: AppIntegrityManager

    private val mockPopGenerator: ProofOfPossessionGenerator = mock()
    private val mockCaller: AttestationCaller = mock()
    private val mockAppChecker: AppChecker = mock()
    private val logger: Logger = mock()
    private val mockKeyStoreManager: KeyStoreManager = mock()

    private val exampleAttestation = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIs" +
        "ImtpZCI6ImY2M2FlNDkxLWZjNzAtNGExNS05ZThhLTkwNWQ0OWEzZmU2ZCJ9." +
        "eyJpc3MiOiJodHRwczovL21vYmlsZS5idWlsZC5hY2NvdW50Lmdvdi51ayIsI" +
        "nN1YiI6ImJZcmN1UlZ2bnlsdkVnWVNTYkJqd1h6SHJ3SiIsImV4cCI6MTczMj" +
        "EyNzMyOCwiY25mIjp7Imp3ayI6eyJrdHkiOiJFQyIsInVzZSI6InNpZyIsImN" +
        "ydiI6IlAtMjU2IiwieCI6ImVmY1ltN3l3bUpOVkNWTmNqUnRiRm53Y1J6Z2JK" +
        "NFl1eXlmX3J1eDFJSHciLCJ5IjoiQVBBRW51ZHRfQVNCRWNBNGdPMWdGZGpua" +
        "UFoNE1kMXFQbnlZWlRHWHd3U0gifX19.nVnTomI-RQ0GKEqgcXzSGPSrpJdhm" +
        "RBdXHmN0Od1Iep-360_VzhiTCKU1ZINzV4IabC_KUi0tM0NznWvLnqXiQ"

    @BeforeEach
    fun setup() {
        val config = AppIntegrityConfiguration(
            mockCaller,
            mockAppChecker,
            mockKeyStoreManager
        )

        appIntegrityManager = FirebaseAppIntegrityManager(logger, config, mockPopGenerator)
    }

    @Test
    fun check_success_response_from_get_attestation(): Unit = runBlocking {
        whenever(mockAppChecker.getAppCheckToken())
            .thenReturn(Result.success(AppCheckToken("Success")))
        whenever(mockCaller.call(any(), any()))
            .thenReturn(
                AttestationResponse.Success(
                    "Success",
                    0
                )
            )
        whenever(mockKeyStoreManager.getPublicKeyCoordinates())
            .thenReturn(Pair("Success", "Success"))
        val result = appIntegrityManager.getAttestation()

        assertEquals(
            AttestationResponse.Success("Success", 0),
            result
        )
    }

    @Test
    fun check_failure_response_from_get_firebase_token() = runBlocking {
        whenever(mockAppChecker.getAppCheckToken()).thenReturn(
            Result.failure(Exception("Error"))
        )
        whenever(mockKeyStoreManager.getPublicKeyCoordinates())
            .thenReturn(Pair("Success", "Success"))

        val result = appIntegrityManager.getAttestation()

        assertEquals(
            Exception("Error").toString(),
            (result as AttestationResponse.Failure).reason
        )
    }

    @Test
    fun check_failure_response_from_get_attestation() = runBlocking {
        whenever(mockAppChecker.getAppCheckToken())
            .thenReturn(Result.success(AppCheckToken("Success")))
        whenever(mockCaller.call(any(), any()))
            .thenReturn(AttestationResponse.Failure("Error"))
        whenever(mockKeyStoreManager.getPublicKeyCoordinates())
            .thenReturn(Pair("Success", "Success"))
        val result = appIntegrityManager.getAttestation()

        assertEquals(
            "Error",
            (result as AttestationResponse.Failure).reason
        )
    }

    @Test
    fun check_success_response_from_generate_PoP() {
        val mockSignatureByte = "Success".toByteArray()

        whenever(mockPopGenerator.createBase64PoP(any(), any(), any(), any()))
            .thenReturn(expectedResult)
        whenever(mockPopGenerator.getExpiryTime()).thenReturn(
            java.time.Instant.now().toEpochMilli() + 180000
        )
        whenever(mockPopGenerator.isPopExpired(anyLong())).thenReturn(false)
        whenever(mockKeyStoreManager.sign(any())).thenReturn(mockSignatureByte)

        val result = appIntegrityManager.generatePoP(MOCK_VALUE, MOCK_VALUE)

        assertTrue(result is SignedPoP.Success)

        val splitJwt = result.popJwt.split(".")

        verify(logger).info(POP_TAG, POP_INFO_MSG)
        assertTrue(splitJwt.size == 3)
    }

    @Test
    fun check_PoP_expired() {
        val mockSignatureByte = "Success".toByteArray()

        whenever(mockPopGenerator.createBase64PoP(any(), any(), any(), any()))
            .thenReturn(expectedResult)
        whenever(mockPopGenerator.isPopExpired(anyLong())).thenReturn(true)
        whenever(mockKeyStoreManager.sign(any())).thenReturn(mockSignatureByte)

        val result = appIntegrityManager.generatePoP(MOCK_VALUE, MOCK_VALUE)

        assertTrue(result is SignedPoP.Success)

        val splitJwt = result.popJwt.split(".")

        verify(logger).error(eq(POP_TAG), eq(POP_ERROR_MSG), any())
        assertTrue(splitJwt.size == 3)
    }

    @Test()
    fun check_failure_response_from_generate_PoP_signing_failure() {
        whenever(mockPopGenerator.createBase64PoP(any(), any(), any(), any()))
            .thenReturn(expectedResult)
        whenever(mockKeyStoreManager.sign(any()))
            .thenAnswer {
                throw SignatureException()
            }
        val result = appIntegrityManager.generatePoP("test", "test")

        assertTrue(result is SignedPoP.Failure)
        assertTrue(result.error!! is SignatureException)
        assertEquals("Signing Error", result.reason)
    }

    @Test
    fun testVerifyAttestationJwkSuccess() {
        whenever(mockKeyStoreManager.getPublicKeyCoordinates()).thenReturn(
            Pair(
                "efcYm7ywmJNVCVNcjRtbFnwcRzgbJ4Yuyyf_rux1IHw",
                "APAEnudt_ASBEcA4gO1gFdjniAh4Md1qPnyYZTGXwwSH"
            )
        )
        val result =
            appIntegrityManager.verifyAttestationJwk(exampleAttestation)

        assertTrue(result)
    }

    @Test
    fun testVerifyAttestation_Jwk_xDoesNotMatch() {
        whenever(mockKeyStoreManager.getPublicKeyCoordinates()).thenReturn(
            Pair(
                "wrong",
                "APAEnudt_ASBEcA4gO1gFdjniAh4Md1qPnyYZTGXwwSH"
            )
        )
        val result =
            appIntegrityManager.verifyAttestationJwk(exampleAttestation)

        assertFalse(result)
    }

    @Test
    fun testVerifyAttestation_Jwk_yDoesNotMatch() {
        whenever(mockKeyStoreManager.getPublicKeyCoordinates()).thenReturn(
            Pair(
                "efcYm7ywmJNVCVNcjRtbFnwcRzgbJ4Yuyyf_rux1IHw",
                "wrong"
            )
        )
        val result =
            appIntegrityManager.verifyAttestationJwk(exampleAttestation)

        assertFalse(result)
    }

    @Test
    fun testVerifyAttestation_invalidAttestationJwk() {
        whenever(mockKeyStoreManager.getPublicKeyCoordinates()).thenReturn(
            Pair(
                "efcYm7ywmJNVCVNcjRtbFnwcRzgbJ4Yuyyf_rux1IHw",
                "wrong"
            )
        )
        val result =
            appIntegrityManager.verifyAttestationJwk("test")

        assertFalse(result)
    }

    @Test
    fun getExpiry_success() {
        val expected: Long = 1732127328
        val result = appIntegrityManager.getExpiry(exampleAttestation)

        assertEquals(expected, result)
    }

    @Test
    fun getExpiry_failure() {
        val result = appIntegrityManager.getExpiry("test")

        assertNull(result)
    }

    companion object {
        private const val MOCK_VALUE = "test"
    }
}
