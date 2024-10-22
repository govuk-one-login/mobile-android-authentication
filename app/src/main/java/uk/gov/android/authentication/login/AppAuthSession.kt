package uk.gov.android.authentication.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService

class AppAuthSession(
    context: Context,
) : LoginSession {
    private val authService: AuthorizationService = AuthorizationService(context)

    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        val intent = authService.getAuthorizationRequestIntent(configuration.createRequest())
        launcher.launch(intent)
    }

    override fun finalise(
        intent: Intent,
        callback: (tokens: TokenResponse) -> Unit
    ) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        val request = authResponse?.createTokenExchangeRequest() ?: throw AuthenticationError.from(intent)
        request.additionalParameters[""] = ""
        authService.performTokenRequest(
            request
        ) { response, exception ->
            callback(
                response?.toTokenResponse() ?: throw AuthenticationError.from(exception)
            )
        }
    }
}
