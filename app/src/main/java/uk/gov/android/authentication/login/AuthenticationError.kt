package uk.gov.android.authentication.login

import android.content.Intent
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors

class AuthenticationError(
    override val message: String,
    val type: ErrorType,
    val status: Int = 0
) : Error() {
    enum class ErrorType {
        OAUTH,
        ACCESS_DENIED,
        SERVER_ERROR
    }

    companion object {
        internal const val NULL_AUTH_MESSAGE = "Auth response was null"

        fun from(intent: Intent) = from(AuthorizationException.fromIntent(intent))

        fun from(exception: AuthorizationException?): AuthenticationError {
            val status = exception?.code ?: 0
            return when (exception) {
                null -> AuthenticationError(
                    message = NULL_AUTH_MESSAGE,
                    type = ErrorType.OAUTH,
                    status = status
                )

                AuthorizationRequestErrors.ACCESS_DENIED -> AuthenticationError(
                    message = exception.message ?: NULL_AUTH_MESSAGE,
                    type = ErrorType.ACCESS_DENIED,
                    status = status
                )

                AuthorizationRequestErrors.SERVER_ERROR -> AuthenticationError(
                    message = exception.message ?: NULL_AUTH_MESSAGE,
                    type = ErrorType.SERVER_ERROR,
                    status = status
                )

                else -> AuthenticationError(
                    message = exception.message ?: NULL_AUTH_MESSAGE,
                    type = ErrorType.OAUTH,
                    status = status
                )
            }
        }
    }
}
