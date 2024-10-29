package uk.gov.android.authentication.integrity.usecase

import kotlinx.coroutines.runBlocking
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.BeforeTest
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AttestationApiCallImplTest {
    private lateinit var client: AttestationClient
    private lateinit var caller: AttestationApiCallerImpl

    @BeforeTest
    fun setUp() {
        client = mock()
        caller = AttestationApiCallerImpl(client)
    }

    @Test
    fun `call() - Success`() = runBlocking {
        val response = AttestationClient.Response("Success", 1000L)
        val firebaseToken = "testToken"
        whenever(client.attest(eq(firebaseToken), any())).thenReturn(Result.success(response))

        val result = caller.call(firebaseToken, JWKTest.X, JWKTest.Y)

        assertEquals("Success", result)
    }

    @Test
    fun `call() - Failure with error message`() = runBlocking {
        val firebaseToken = "testToken"
        val errorMessage = "Test error message"
        whenever(client.attest(eq(firebaseToken), any()))
            .thenReturn(Result.failure(IOException(errorMessage)))

        val result = caller.call(firebaseToken, JWKTest.X, JWKTest.Y)

        assertEquals(errorMessage, result)
    }

    @Test
    fun `call() - Failure without error message`() = runBlocking {
        val firebaseToken = "testToken"
        whenever(client.attest(eq(firebaseToken), any()))
            .thenReturn(Result.failure(IOException()))

        val result = caller.call(firebaseToken, JWKTest.X, JWKTest.Y)

        assertEquals("Error", result)
    }
}
