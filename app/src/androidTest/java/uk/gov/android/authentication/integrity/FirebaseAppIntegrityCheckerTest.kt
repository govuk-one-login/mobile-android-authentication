package uk.gov.android.authentication.integrity

import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import org.mockito.kotlin.mock
import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.model.AppIntegrityConfiguration
import uk.gov.android.authentication.integrity.model.AttestationResponse
import uk.gov.android.authentication.integrity.model.SignedResponse
import uk.gov.android.authentication.integrity.usecase.AttestationClient
import kotlin.test.assertEquals

class FirebaseAppIntegrityCheckerTest {
    private lateinit var clientAttestationManager: ClientAttestationManager

    private val client: AttestationClient = mock()
    private val mockAppChecker: AppChecker = mock()

    @BeforeTest
    fun setup() {
        val config = AppIntegrityConfiguration(
            client,
            mockAppChecker
        )

        clientAttestationManager = FirebaseClientAttestationManager(config)
    }

    @Test
    fun check_failure_response_from_get_attestation() = runBlocking {
        val result = clientAttestationManager.getAttestation()

        assertEquals("Not yet implemented", (result as AttestationResponse.Failure).reason)
    }

    @Test
    fun check_failure_response_from_sign_attestation() = runBlocking {
        val result = clientAttestationManager.signAttestation("attestation")

        assertEquals("Not yet implemented", (result as SignedResponse.Failure).reason)
    }
}
