package uk.gov.android.authentication

import android.content.Intent
import net.openid.appauth.AuthorizationException

class AuthenticationError(
    override val message: String,
    val type: ErrorType
) : Error() {
    enum class ErrorType {
        OAUTH,
        NETWORK
    }

    companion object {
        internal const val NULL_AUTH_MESSAGE = "Auth response was null"

        fun from(intent: Intent) = from(AuthorizationException.fromIntent(intent))

        fun from(exception: AuthorizationException?) = AuthenticationError(
            message = exception?.message ?: NULL_AUTH_MESSAGE,
            type = ErrorType.OAUTH
        )
    }
}
