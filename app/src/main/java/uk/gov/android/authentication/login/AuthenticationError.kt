package uk.gov.android.authentication.login

import android.content.Intent
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors

class AuthenticationError(
    override val message: String,
    val type: ErrorType
) : Error() {
    enum class ErrorType {
        OAUTH,
        ACCESS_DENIED
    }

    companion object {
        internal const val NULL_AUTH_MESSAGE = "Auth response was null"

        fun from(intent: Intent) = from(AuthorizationException.fromIntent(intent))

        fun from(exception: AuthorizationException?): AuthenticationError {
            return when (exception) {
                AuthorizationRequestErrors.ACCESS_DENIED -> AuthenticationError(
                    message = exception.message ?: NULL_AUTH_MESSAGE,
                    type = ErrorType.ACCESS_DENIED
                )

                else -> AuthenticationError(
                    message = exception?.message ?: NULL_AUTH_MESSAGE,
                    type = ErrorType.OAUTH
                )
            }
        }
    }
}
