package uk.gov.android.authentication.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.OptIn
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.ExperimentalEphemeralBrowsing
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import uk.gov.android.authentication.integrity.AppIntegrityParameters

class AppAuthSession(
    context: Context
) : LoginSession {
    private val authService: AuthorizationService = AuthorizationService(context)
    private val clientAuthenticationProvider = ClientAuthenticationProviderImpl()

    @OptIn(ExperimentalEphemeralBrowsing::class)
    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        val customEphemeralTabIntent = CustomTabsIntent.Builder()
            .setEphemeralBrowsingEnabled(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()
        val intent = authService.getAuthorizationRequestIntent(
            configuration.createRequest(),
            customEphemeralTabIntent
        )
        launcher.launch(intent)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        onSuccess: (tokens: TokenResponse) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        try {
            val authResponse = AuthorizationResponse.fromIntent(intent)
            if (authResponse == null) {
                val exception = AuthorizationException.fromIntent(intent)

                onFailure(AuthenticationError.from(exception))
                return
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
                val tokenResponse = response?.toTokenResponse()
                if (tokenResponse == null) {
                    onFailure(AuthenticationError.from(exception))
                } else {
                    onSuccess(tokenResponse)
                }
            }
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @Deprecated("Use other finalise method instead")
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
