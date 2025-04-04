package uk.gov.android.authentication.login

import android.content.Intent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import uk.gov.android.authentication.login.AuthenticationError.ErrorType

class AuthenticationErrorTest {
    @Test
    fun `Error construction with message and type`() {
        // When constructing AuthenticationError
        val errorMessage = "Invalid credentials"
        val error = AuthenticationError(errorMessage, ErrorType.OAUTH)
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
    fun `from(exception Exception) creates AuthenticationError of type access_denied`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.ACCESS_DENIED.code,
            AuthorizationRequestErrors.ACCESS_DENIED.error,
            AuthorizationRequestErrors.ACCESS_DENIED.errorDescription,
            AuthorizationRequestErrors.ACCESS_DENIED.errorUri,
            AuthorizationRequestErrors.ACCESS_DENIED.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.ACCESS_DENIED, actual.type)
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
