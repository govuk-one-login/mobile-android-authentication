package uk.gov.android.authentication.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import uk.gov.android.authentication.integrity.AppIntegrityParameters

class AppAuthSession(
    context: Context
) : LoginSession {
    private val authService: AuthorizationService = AuthorizationService(context)
    private val clientAuthenticationProvider = ClientAuthenticationProviderImpl()

    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        val intent = authService.getAuthorizationRequestIntent(configuration.createRequest())
        launcher.launch(intent)
    }

    override fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        callback: (tokens: TokenResponse) -> Unit
    ) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        if (authResponse == null) {
            val exception = AuthorizationException.fromIntent(intent)

            throw AuthenticationError.from(exception)
        }

        // Create object that allows for additional headers/ body parameters
        val clientAuthenticationWithExtraHeaders =
            clientAuthenticationProvider.setCustomClientAuthentication(
                appIntegrity.attestation,
                appIntegrity.pop
            )

        // Create the standard request
        val request = authResponse.createTokenExchangeRequest()

        authService.performTokenRequest(
            request,
            clientAuthenticationWithExtraHeaders
        ) { response, exception ->
            callback(
                response?.toTokenResponse() ?: throw AuthenticationError.from(exception)
            )
        }
    }
}
