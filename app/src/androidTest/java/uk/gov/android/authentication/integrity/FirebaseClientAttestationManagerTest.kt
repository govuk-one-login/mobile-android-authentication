package uk.gov.android.authentication.integrity

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.keymanager.ECKeyManager
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.model.AppCheckToken
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.ProofOfPossessionGenerator
import uk.gov.android.authentication.integrity.model.SignedResponse
import uk.gov.android.authentication.integrity.usecase.AttestationCaller
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FirebaseClientAttestationManagerTest {
    private lateinit var clientAttestationManager: ClientAttestationManager

    private val mockCaller: AttestationCaller = mock()
    private val mockAppChecker: AppChecker = mock()
    private val mockKeyStoreManager: KeyStoreManager = mock()

    @BeforeTest
    fun setup() {
        val config = AppIntegrityConfiguration(
            mockCaller,
            mockAppChecker,
            mockKeyStoreManager
        )

        clientAttestationManager = FirebaseClientAttestationManager(config)
    }

    @Test
    fun check_success_response_from_get_attestation(): Unit = runBlocking {
        whenever(mockAppChecker.getAppCheckToken())
            .thenReturn(Result.success(AppCheckToken("Success")))
        whenever(mockCaller.call(any(), any()))
            .thenReturn(AttestationResponse.Success(
                "Success",
                0
            ))
        val result = clientAttestationManager.getAttestation()

        assertEquals(AttestationResponse.Success("Success", 0),
            result)
    }

    @Test
    fun check_failure_response_from_get_firebase_token() = runBlocking {
        whenever(mockAppChecker.getAppCheckToken()).thenReturn(
            Result.failure(Exception("Error"))
        )
        val result = clientAttestationManager.getAttestation()

        assertEquals(Exception("Error").toString(),
            (result as AttestationResponse.Failure).reason)
    }

    @Test
    fun check_failure_response_from_get_attestation() = runBlocking {
        whenever(mockAppChecker.getAppCheckToken())
            .thenReturn(Result.success(AppCheckToken("Success")))
        whenever(mockCaller.call(any(), any()))
            .thenReturn(AttestationResponse.Failure("Error"))
        val result = clientAttestationManager.getAttestation()

        assertEquals("Error",
            (result as AttestationResponse.Failure).reason)
    }

    @Test
    fun check_success_response_from_generate_PoP() = runBlocking {
        val mockSignatureByte = "Success".toByteArray()
        val mockSignature = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(mockSignatureByte)

        whenever(mockKeyStoreManager.sign(any())).thenReturn(mockSignatureByte)

        val result = clientAttestationManager.generatePoP(MOCK_VALUE, MOCK_VALUE)

        assertTrue(result is SignedResponse.Success)

        val splitJwt = result.signedAttestationJwt.split(".")
        assertTrue(splitJwt.size == 3)
        assertEquals(mockSignature, splitJwt.last())
    }

    @OptIn(ExperimentalEncodingApi::class)
    @Test(expected = Exception::class)
    fun check_failure_response_from_generate_PoP() = runBlocking {
        whenever(mockKeyStoreManager.sign(any()))
            .thenThrow(ECKeyManager.SigningError.InvalidSignature)
        val result = clientAttestationManager.generatePoP("test", "test")

        assertTrue(result is SignedResponse.Failure)
        assertEquals("Signature couldn't be verified.", result.reason)
    }

    companion object {
        private const val MOCK_VALUE = "test"
    }
}
