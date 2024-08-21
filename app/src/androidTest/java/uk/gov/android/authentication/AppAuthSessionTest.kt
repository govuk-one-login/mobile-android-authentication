package uk.gov.android.authentication

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationService.TokenResponseCallback
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Ignore
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
        mockContext
    )

    @Ignore("Will be fixed in refactor")
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
            tokenEndpoint = tokenUri,
            persistentSessionId = "persistentId"
        )

        val expectedIntent = Intent(Intent.ACTION_VIEW)
        whenever(mockAuthService.getAuthorizationRequestIntent(any()))
            .thenReturn(expectedIntent)

        loginSession.present(mockLauncher, loginSessionConfiguration)

        verify(mockLauncher).launch(expectedIntent)
    }

    @Ignore("Will be fixed in refactor")
    @Test
    fun unsuccessfulFinaliseCallbackResponse() {
        val error = assertThrows(AuthenticationError::class.java) {
            loginSession.finalise(Intent()) {}
        }

        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals("Auth response was null", error.message)
    }

    @Ignore("Will be fixed in refactor")
    @Test
    fun semiSuccessfulFinaliseCallbackResponse_nullTokenResponse() {
        val intent = Intent().setData(Uri.parse("https://mobile.build.account.gov.uk/re" +
            "direct?state=OpHOXMt1jUmnkEKLNqnH8Q&code=eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsIm" +
            "tpZCI6IjE2ZGI2NTg3LTU0NDUtNDVkNi1hN2Q5LTk4NzgxZWJkZjkzZCJ9.eyJpc3MiOiJodHRwczo" +
            "vL3Rva2VuLmJ1aWxkLmFjY291bnQuZ292LnVrIiwic3ViIjoiYjA1YzQ2MmMtNmE3OS00NDgzLWJk" +
            "YmYtMTEyMjIzYzZjNjI2IiwiYXVkIjoiaHR0cHM6Ly90b2tlbi5idWlsZC5hY2NvdW50Lmdvdi51ay" +
            "IsImlhdCI6MTcyMzAyNjczMCwiZXhwIjoxNzIzMDI2OTEwfQ.NjKxaPm29CNtrUPIDPkjlTQX4bB" +
            "1_JfMFduCTIjjcNY_VUCJmr0sjA08xYaq1cTY92F7umuk4oQjgtYV2gUfwg"))
        intent.putExtra("net.openid.appauth.AuthorizationResponse", "{\"request\":" +
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
            "cz7KlIUolbjZtqZMjlWzZ3csrVTvYAUYyrTY2tGg\",\"additional_parameters\":{}}")
        whenever(mockAuthService.performTokenRequest(any(), any())).thenAnswer {
            (it.arguments[1] as TokenResponseCallback).onTokenRequestCompleted(
                null,
                null
            )
        }

        val error = assertThrows(AuthenticationError::class.java) {
            loginSession.finalise(intent) {}
        }

        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals("Failed token request", error.message)
    }
}
