package uk.gov.android.authentication

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

/* What can't be checked here:
* Full route of AppAuthSession::present
* Full route of AppAuthSession::finalise
* Because the real AuthorizationService has to have a browser available from the context
*  */
class AppAuthSessionTest {
    private lateinit var appAuthSession: AppAuthSession
    private lateinit var authService: AuthorizationService

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        authService = mock()
        appAuthSession = AppAuthSession(context, authService)
    }

    @Test
    fun presentCreatesAndLaunchesAuthorisationIntent() {
        // GIVEN a mock AuthorizationService, a mock ActivityResultLauncher, and a real LoginSessionConfiguration
        whenever(authService.getAuthorizationRequestIntent(any()))
            .thenReturn(Intent(Intent.ACTION_VIEW))

        val launcher: ActivityResultLauncher<Intent> = mock()
        whenever(launcher.launch(any())).then {  } // take no action

        val loginSessionConfig = LoginSessionConfiguration(
            authorizeEndpoint = Uri.parse("https://auth.gov.uk/test"),
            clientId = "clientId.Test",
            locale = LoginSessionConfiguration.Locale.EN,
            prefersEphemeralWebSession = true,
            redirectUri = Uri.parse("https://redirect.gov.uk/test"),
            responseType = LoginSessionConfiguration.ResponseType.CODE,
            scopes = emptyList(),
            tokenEndpoint = Uri.parse("https://token.gov.uk/test"),
            vectorsOfTrust = "[\"Cl.Cm.P0\"]",
            persistentSessionId = "persistentSessionTestId"
        )
        // WHEN present is called
        appAuthSession.present(
            launcher = launcher,
            configuration = loginSessionConfig
        )
        // THEN the AuthorisationIntent is created and the launcher's launch function is called
        verify(launcher).launch(any())
    }

    @Test(expected = IllegalArgumentException::class)
    fun finaliseThrowsIllegalArgumentExceptionForMalformedIntentResponse() {
        // GIVEN an intent with a malformed (empty) response data json extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, "{}")
        }
        // WHEN finalise is called
        appAuthSession.finalise(intent) {}
        // THEN IllegalArgumentException is thrown
    }

    @Test
    fun finaliseThrowsAuthenticationErrorForIntentWithoutResponse() {
        // GIVEN an (empty) intent without a response data json extra
        val intent = Intent()
        // WHEN finalise is called
        val error = assertThrows(AuthenticationError::class.java) {
            appAuthSession.finalise(intent) {}
        }
        // THEN an AuthenticationError is thrown
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals(AuthenticationError.NULL_AUTH_MESSAGE, error.message)
    }

    @Test
    fun finaliseThrowsAuthenticationErrorOnCallbackNullResponse() {
        // GIVEN a performTokenRequest that fails with an AuthorizationException
        whenever(authService.performTokenRequest(any(), any())).thenAnswer {
            (it.arguments[1] as TokenResponseCallback).onTokenRequestCompleted(
                null,
                NETWORK_ERROR
            )
        }

        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, TEST_EXTRA_VALUE)
        }
        // WHEN finalise is called
        val error = assertThrows(AuthenticationError::class.java) {
            appAuthSession.finalise(intent) {}
        }
        // THEN an AuthenticationError is thrown
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals(NETWORK_ERROR_MSG, error.message)
    }

    companion object {
        private const val NETWORK_ERROR_MSG = "Network error"
        private val NETWORK_ERROR = AuthorizationException(0, 3, null, NETWORK_ERROR_MSG, null, null)
        private const val TEST_EXTRA_VALUE = "{\"request\":" +
                "{\"configuration\":{\"authorizationEndpoint\":\"https:\\/\\/token.build.account." +
                "gov.uk\\/authorize\",\"tokenEndpoint\":\"https:\\/\\/token.build.account.gov.uk" +
                "\\/token\"},\"clientId\":\"bYrcuRVvnylvEgYSSbBjwXzHrwJ\",\"responseType\":\"code\"" +
                ",\"redirectUri\":\"https:\\/\\/mobile.build.account.gov.uk\\/redirect\",\"scope\"" +
                ":\"openid\",\"ui_locales\":\"en\",\"state\":\"ZSP4n9sNkWAEEBwcIcMKHg\",\"nonce\":" +
                "\"14481e71-df65-4e8a-8a15-e48afc21d99b\",\"codeVerifier\":\"Wg22z1mcHVSjjwxtjsRXl" +
                "cGUnBp1Ilh46j30ZOKpl_9E_yPg0lyjzQ1dC0aaXAZlM-7LClB0jgPnc0MDrSl9Zg\",\"codeVerifier" +
                "Challenge\":\"_kmKDDhAwultvWmu-7E4Gn7GO5xTFsQum2HCpY5EfaI\",\"codeVerifierChalleng" +
                "eMethod\":\"S256\",\"additionalParameters\":{\"vtr\":\"[\\\"Cl.Cm.P0\\\"]\"}},\"st" +
                "ate\":\"ZSP4n9sNkWAEEBwcIcMKHg\",\"code\":\"eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsIm" +
                "tpZCI6IjE2ZGI2NTg3LTU0NDUtNDVkNi1hN2Q5LTk4NzgxZWJkZjkzZCJ9.eyJpc3MiOiJodHRwczovL3R" +
                "va2VuLmJ1aWxkLmFjY291bnQuZ292LnVrIiwic3ViIjoiM2MyMTY0MjYtZTZkZC00YmY5LTg0ZWEtZjJlZ" +
                "DRlM2EyY2ZkIiwiYXVkIjoiaHR0cHM6Ly90b2tlbi5idWlsZC5hY2NvdW50Lmdvdi51ayIsImlhdCI6MTc" +
                "yMzAzMDYxNywiZXhwIjoxNzIzMDMwNzk3fQ.TuZnb9qOHSqd82G0190fHsLKcfr2QlV8ZvkmZNjbX6UK2v" +
                "cz7KlIUolbjZtqZMjlWzZ3csrVTvYAUYyrTY2tGg\",\"additional_parameters\":{}}"
    }
}
