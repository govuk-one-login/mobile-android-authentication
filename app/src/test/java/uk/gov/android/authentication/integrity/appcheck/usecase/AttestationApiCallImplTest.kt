package uk.gov.android.authentication.integrity.appcheck.usecase

import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.network.api.ApiRequest
import uk.gov.android.network.api.ApiResponse
import uk.gov.android.network.client.GenericHttpClient
import kotlin.to

class AttestationApiCallImplTest {
    private lateinit var httpClient: GenericHttpClient
    private lateinit var attestationApiCaller: AttestationApiCaller

    @BeforeEach
    fun setUp() {
        httpClient = mock()
        attestationApiCaller =
            AttestationApiCallerImpl(httpClient)
    }

    @Test
    fun `invoke() - Success`() = runBlocking {
        val firebaseToken = "testToken"
        val expectedRequest = ApiRequest.Get(
            url = "url",
            headers = listOf(
                "X-Firebase-Token" to firebaseToken
            )
        )
        whenever(httpClient.makeRequest(expectedRequest)).thenReturn(ApiResponse.Success("Success"))

        val result = attestationApiCaller.call(firebaseToken, "url")

        assertEquals("Success", result)
    }

    @Test
    fun `invoke() - Failure with error message`() = runBlocking {
        val firebaseToken = "testToken"
        val errorMessage = "Test error message"
        whenever(httpClient.makeRequest(any()))
            .thenReturn(ApiResponse.Failure(500, IOException(errorMessage)))

        val result = attestationApiCaller.call(firebaseToken, "url")

        assertEquals(errorMessage, result)
    }

    @Test
    fun `invoke() - Failure without error message`() = runBlocking {
        val firebaseToken = "testToken"
        whenever(httpClient.makeRequest(any()))
            .thenReturn(ApiResponse.Failure(500, IOException()))

        val result = attestationApiCaller.call(firebaseToken, "url")

        assertEquals("Error", result)
    }
}
