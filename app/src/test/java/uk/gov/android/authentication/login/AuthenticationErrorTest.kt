package uk.gov.android.authentication.login

import android.content.Intent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import net.openid.appauth.AuthorizationException

class AuthenticationErrorTest {
    @Test
    fun `Error construction with message and type`() {
        // When constructing AuthenticationError
        val errorMessage = "Invalid credentials"
        val error = AuthenticationError(errorMessage, AuthenticationError.ErrorType.OAUTH)
        // Then set the message and type member variables
        assertEquals(errorMessage, error.message)
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
    }

    @Test
    fun `from(exception Intent) creates AuthenticationError`() {
        // Given an Intent that maps to an OpenId AuthorizationException
        val intent = Intent().apply {
            putExtra(AuthorizationException.EXTRA_EXCEPTION, "{}")
        }
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(intent)
        // Then return an AuthenticationError
        assertIs<AuthenticationError>(actual)
    }

    @Test
    fun `from(Intent) creates AuthenticationError`() {
        // Given an Intent that doesn't map to AuthorizationException
        val intent = Intent()
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(intent)
        // Then return an AuthenticationError
        assertIs<AuthenticationError>(actual)
    }

    @Test
    fun `from(AuthorizationException) creates AuthenticationError`() {
        // Given an OpenId AuthorizationException
        val networkError = AuthorizationException(0, 3, null, NETWORK_ERROR_MSG, null, null)
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(networkError)
        // Then return an AuthenticationError
        assertIs<AuthenticationError>(actual)
        assertEquals(NETWORK_ERROR_MSG, actual.message)
    }

    @Test
    fun `from(null) creates AuthenticationError`() {
        // When calling the from mapping method with null
        val actual = AuthenticationError.Companion.from(null)
        // Then return an AuthenticationError
        assertIs<AuthenticationError>(actual)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, actual.message)
    }

    companion object {
        private const val NETWORK_ERROR_MSG = "Network error"
    }
}
