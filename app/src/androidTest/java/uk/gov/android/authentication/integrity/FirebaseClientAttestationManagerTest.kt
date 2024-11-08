package uk.gov.android.authentication.integrity

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.any
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.model.AppCheckToken
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.SignedResponse
import uk.gov.android.authentication.integrity.usecase.AttestationCaller
import kotlin.test.assertEquals

class FirebaseClientAttestationManagerTest {
    private lateinit var clientAttestationManager: ClientAttestationManager

    private val caller: AttestationCaller = mock()
    private val mockAppChecker: AppChecker = mock()

    @BeforeTest
    fun setup() {
        val config = AppIntegrityConfiguration(
            caller,
            mockAppChecker
        )

        clientAttestationManager = FirebaseClientAttestationManager(config)
    }

    @Test
    fun check_success_response_from_get_attestation(): Unit = runBlocking {
        whenever(mockAppChecker.getAppCheckToken())
            .thenReturn(Result.success(AppCheckToken("Success")))
        whenever(caller.call(any(), any()))
            .thenReturn(Result.success(AttestationResponse.Success("Success")))
        val result = clientAttestationManager.getAttestation()

        assertEquals(AttestationResponse.Success("Success"),
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
        whenever(caller.call(any(), any()))
            .thenReturn(Result.success(AttestationResponse.Failure("Error")))
        val result = clientAttestationManager.getAttestation()

        assertEquals("Error",
            (result as AttestationResponse.Failure).reason)
    }

    @Test
    fun check_failure_response_from_sign_attestation() = runBlocking {
        val result = clientAttestationManager.signAttestation("attestation")

        assertEquals("Not yet implemented", (result as SignedResponse.Failure).reason)
    }
}
