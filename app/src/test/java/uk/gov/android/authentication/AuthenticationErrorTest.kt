package uk.gov.android.authentication

import android.content.Intent
import net.openid.appauth.AuthorizationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthenticationErrorTest {
    @Test
    fun `Error construction with message and type`() {
        // WHEN constructing AuthenticationError
        val errorMessage = "Invalid credentials"
        val error = AuthenticationError(errorMessage, AuthenticationError.ErrorType.OAUTH)
        // THEN the message and type are set
        assertEquals(errorMessage, error.message)
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
    }

    @Test
    fun `from(exception Intent) creates AuthenticationError`() {
        // GIVEN an Intent that maps to an openId AuthorizationException
        val intent = Intent().apply {
            putExtra(AuthorizationException.EXTRA_EXCEPTION, "{}")
        }
        // WHEN the from mapping method is called
        val actual = AuthenticationError.from(intent)
        // THEN an AuthenticationError is returned
        assertIs<AuthenticationError>(actual)
    }

    @Test
    fun `from(Intent) creates AuthenticationError`() {
        // GIVEN an Intent that doesn't map to AuthorizationException
        val intent = Intent()
        // WHEN the from mapping method is called
        val actual = AuthenticationError.from(intent)
        // THEN an AuthenticationError is returned
        assertIs<AuthenticationError>(actual)
    }

    @Test
    fun `from(AuthorizationException) creates AuthenticationError`() {
        // GIVEN an openId AuthorizationException
        val networkError = AuthorizationException(0, 3, null, NETWORK_ERROR_MSG, null, null)
        // WHEN the from mapping method is called
        val actual = AuthenticationError.from(networkError)
        // THEN an AuthenticationError is returned
        assertIs<AuthenticationError>(actual)
        assertEquals(NETWORK_ERROR_MSG, actual.message)
    }

    @Test
    fun `from(null) creates AuthenticationError`() {
        // WHEN the from mapping method is called with null
        val actual = AuthenticationError.from(null)
        // THEN an AuthenticationError is returned
        assertIs<AuthenticationError>(actual)
        assertEquals(AuthenticationError.NULL_AUTH_MESSAGE, actual.message)
    }

    companion object {
        private const val NETWORK_ERROR_MSG = "Network error"
    }
}
