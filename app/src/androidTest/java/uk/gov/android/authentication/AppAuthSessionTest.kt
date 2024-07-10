package uk.gov.android.authentication

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationService
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AppAuthSessionTest {
    private val mockContext: Context = mock()
    private val mockAuthService: AuthorizationService = mock()
    private val mockLauncher: ActivityResultLauncher<Intent> = mock()

    private val loginSession: LoginSession = AppAuthSession(
        mockContext,
        mockAuthService
    )

    @Test
    fun presentLaunchesAuthRequest() {
        val authUri = Uri.parse("https://example.com/authorize")
        val redirectUri = Uri.parse("https://example.com/redirect")
        val tokenUri = Uri.parse("https://example.com/token")
        val loginSessionConfiguration = LoginSessionConfiguration(
            authorizeEndpoint = authUri,
            clientId = "sampleClientId",
            redirectUri = redirectUri,
            scopes = listOf(LoginSessionConfiguration.Scope.OPENID),
            tokenEndpoint = tokenUri
        )

        val expectedIntent = Intent(Intent.ACTION_VIEW)
        whenever(mockAuthService.getAuthorizationRequestIntent(any()))
            .thenReturn(expectedIntent)

        loginSession.present(mockLauncher, loginSessionConfiguration)

        verify(mockLauncher).launch(expectedIntent)
    }
}
