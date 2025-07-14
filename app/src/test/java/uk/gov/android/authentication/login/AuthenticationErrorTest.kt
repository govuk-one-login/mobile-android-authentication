package uk.gov.android.authentication.login

import android.content.Intent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationException.TokenRequestErrors
import uk.gov.android.authentication.login.AuthenticationError.ErrorType

class AuthenticationErrorTest {
    @Test
    fun `Error construction with message and type`() {
        // When constructing AuthenticationError
        val errorMessage = "Invalid credentials"
        val error = AuthenticationError(errorMessage, ErrorType.OAUTH)
        // Then set the message and type member variables
        assertEquals(errorMessage, error.message)
        assertEquals(ErrorType.OAUTH, error.type)
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
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.ACCESS_DENIED.code,
            AuthorizationRequestErrors.ACCESS_DENIED.error,
            nonNullMessage,
            AuthorizationRequestErrors.ACCESS_DENIED.errorUri,
            AuthorizationRequestErrors.ACCESS_DENIED.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.ACCESS_DENIED, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.ACCESS_DENIED.code, actual.status)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type invalid_scope`() {
        // Given an Intent that doesn't map to AuthorizationException
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.INVALID_SCOPE.code,
            AuthorizationRequestErrors.INVALID_SCOPE.error,
            nonNullMessage,
            AuthorizationRequestErrors.INVALID_SCOPE.errorUri,
            AuthorizationRequestErrors.INVALID_SCOPE.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.OAUTH, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.INVALID_SCOPE.code, actual.status)
    }

    @Test
    fun `from(exception Exception) create AuthenticationError of type unsupported_response_type`() {
        // Given an Intent that doesn't map to AuthorizationException
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE.code,
            AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE.error,
            nonNullMessage,
            AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE.errorUri,
            AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.OAUTH, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.UNSUPPORTED_RESPONSE_TYPE.code, actual.status)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type unauthorized_client`() {
        // Given an Intent that doesn't map to AuthorizationException
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.UNAUTHORIZED_CLIENT.code,
            AuthorizationRequestErrors.UNAUTHORIZED_CLIENT.error,
            nonNullMessage,
            AuthorizationRequestErrors.UNAUTHORIZED_CLIENT.errorUri,
            AuthorizationRequestErrors.UNAUTHORIZED_CLIENT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.OAUTH, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.UNAUTHORIZED_CLIENT.code, actual.status)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type invalid_request`() {
        // Given an Intent that doesn't map to AuthorizationException
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.INVALID_REQUEST.code,
            AuthorizationRequestErrors.INVALID_REQUEST.error,
            nonNullMessage,
            AuthorizationRequestErrors.INVALID_REQUEST.errorUri,
            AuthorizationRequestErrors.INVALID_REQUEST.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.OAUTH, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.INVALID_REQUEST.code, actual.status)
    }

    @Test
    fun `from(exception Exception) type access_denied null message`() {
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
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, actual.message)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type server_error`() {
        // Given an Intent that doesn't map to AuthorizationException
        val nonNullMessage = "error message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.SERVER_ERROR.code,
            AuthorizationRequestErrors.SERVER_ERROR.error,
            nonNullMessage,
            AuthorizationRequestErrors.SERVER_ERROR.errorUri,
            AuthorizationRequestErrors.SERVER_ERROR.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.SERVER_ERROR, actual.type)
        assertEquals(nonNullMessage, actual.message)
        assertEquals(AuthorizationRequestErrors.SERVER_ERROR.code, actual.status)
    }

    @Test
    fun `from(exception Exception) create token AuthenticationError of type invalid_request`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_REQUEST.code,
            TokenRequestErrors.INVALID_REQUEST.error,
            TokenRequestErrors.INVALID_REQUEST.message,
            TokenRequestErrors.INVALID_REQUEST.errorUri,
            TokenRequestErrors.INVALID_REQUEST.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_REQUEST.code, actual.status)
    }

    @Test
    fun `from(exception Exception) invalid_request non null message`() {
        // Given an Intent that doesn't map to AuthorizationException
        val message = "message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_REQUEST.code,
            TokenRequestErrors.INVALID_REQUEST.error,
            message,
            TokenRequestErrors.INVALID_REQUEST.errorUri,
            TokenRequestErrors.INVALID_REQUEST.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_REQUEST.code, actual.status)
        assertEquals(message, actual.message)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type unsupported_grant_type`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.code,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.error,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.message,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.errorUri,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.code, actual.status)
    }

    @Test
    fun `from(exception Exception) unsupported_grant_type non null msg`() {
        // Given an Intent that doesn't map to AuthorizationException
        val message = "message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.code,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.error,
            message,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.errorUri,
            TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.UNSUPPORTED_GRANT_TYPE.code, actual.status)
        assertEquals(message, actual.message)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type invalid_grant`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_GRANT.code,
            TokenRequestErrors.INVALID_GRANT.error,
            TokenRequestErrors.INVALID_GRANT.message,
            TokenRequestErrors.INVALID_GRANT.errorUri,
            TokenRequestErrors.INVALID_GRANT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_GRANT.code, actual.status)
    }

    @Test
    fun `from(exception Exception) invalid_grant non null msg`() {
        // Given an Intent that doesn't map to AuthorizationException
        val message = "message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_GRANT.code,
            TokenRequestErrors.INVALID_GRANT.error,
            message,
            TokenRequestErrors.INVALID_GRANT.errorUri,
            TokenRequestErrors.INVALID_GRANT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_GRANT.code, actual.status)
        assertEquals(message, actual.message)
    }

    @Test
    fun `from(exception Exception) creates AuthenticationError of type invalid_client`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_CLIENT.code,
            TokenRequestErrors.INVALID_CLIENT.error,
            TokenRequestErrors.INVALID_CLIENT.message,
            TokenRequestErrors.INVALID_CLIENT.errorUri,
            TokenRequestErrors.INVALID_CLIENT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_CLIENT.code, actual.status)
    }

    @Test
    fun `from(exception Exception) invalid_client non null msg`() {
        // Given an Intent that doesn't map to AuthorizationException
        val message = "message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.INVALID_CLIENT.code,
            TokenRequestErrors.INVALID_CLIENT.error,
            message,
            TokenRequestErrors.INVALID_CLIENT.errorUri,
            TokenRequestErrors.INVALID_CLIENT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.INVALID_CLIENT.code, actual.status)
        assertEquals(message, actual.message)
    }

    @Test
    fun `from(exception Exception) create token AuthenticationError of type unauthorized_client`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.code,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.error,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.message,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.errorUri,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.UNAUTHORIZED_CLIENT.code, actual.status)
    }

    @Test
    fun `from(exception Exception) unauthorized_client non null msg`() {
        // Given an Intent that doesn't map to AuthorizationException
        val message = "message"
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_TOKEN_ERROR,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.code,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.error,
            message,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.errorUri,
            TokenRequestErrors.UNAUTHORIZED_CLIENT.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.TOKEN_ERROR, actual.type)
        assertEquals(TokenRequestErrors.UNAUTHORIZED_CLIENT.code, actual.status)
        assertEquals(message, actual.message)
    }

    @Test
    fun `from(exception Exception) type server_error null message`() {
        // Given an Intent that doesn't map to AuthorizationException
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.SERVER_ERROR.code,
            AuthorizationRequestErrors.SERVER_ERROR.error,
            AuthorizationRequestErrors.SERVER_ERROR.errorDescription,
            AuthorizationRequestErrors.SERVER_ERROR.errorUri,
            AuthorizationRequestErrors.SERVER_ERROR.cause
        )
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(exception)
        // Then return an AuthenticationError
        assertEquals(ErrorType.SERVER_ERROR, actual.type)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, actual.message)
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
    fun `from(AuthorizationException) creates AuthenticationError null message`() {
        // Given an OpenId AuthorizationException
        val networkError = AuthorizationException(0, 3, null, null, null, null)
        // When calling the from mapping method
        val actual = AuthenticationError.Companion.from(networkError)
        // Then return an AuthenticationError
        assertIs<AuthenticationError>(actual)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, actual.message)
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
