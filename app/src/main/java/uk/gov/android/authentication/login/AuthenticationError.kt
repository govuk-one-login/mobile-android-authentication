package uk.gov.android.authentication.login

import android.content.Intent
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationException.TokenRequestErrors

data class AuthenticationError(
    override val message: String,
    val type: ErrorType,
    val status: Int = 0
) : Error() {
    enum class ErrorType {
        OAUTH,
        ACCESS_DENIED,
        SERVER_ERROR,
        TOKEN_ERROR
    }

    companion object {
        internal const val NULL_AUTH_MESSAGE = "Auth response was null"

        fun from(intent: Intent) = from(AuthorizationException.fromIntent(intent))

        fun from(exception: AuthorizationException?): AuthenticationError {
            val status = exception?.code ?: 0
            val message = exception?.message ?: NULL_AUTH_MESSAGE
            return when (exception) {
                null -> AuthenticationError(
                    message = NULL_AUTH_MESSAGE,
                    type = ErrorType.OAUTH,
                    status = status
                )

                AuthorizationRequestErrors.ACCESS_DENIED -> AuthenticationError(
                    message = message,
                    type = ErrorType.ACCESS_DENIED,
                    status = status
                )

                AuthorizationRequestErrors.SERVER_ERROR -> AuthenticationError(
                    message = message,
                    type = ErrorType.SERVER_ERROR,
                    status = status
                )

                TokenRequestErrors.INVALID_REQUEST,
                TokenRequestErrors.UNSUPPORTED_GRANT_TYPE,
                TokenRequestErrors.INVALID_GRANT,
                TokenRequestErrors.INVALID_CLIENT,
                TokenRequestErrors.UNAUTHORIZED_CLIENT -> AuthenticationError(
                    message = message,
                    type = ErrorType.TOKEN_ERROR,
                    status = status
                )

                else -> AuthenticationError(
                    message = message,
                    type = ErrorType.OAUTH,
                    status = status
                )
            }
        }
    }
}
