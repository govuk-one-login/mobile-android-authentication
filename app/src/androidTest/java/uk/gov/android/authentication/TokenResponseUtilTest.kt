package uk.gov.android.authentication

import net.openid.appauth.TokenRequest
import org.junit.Before
import org.junit.Test
import uk.gov.android.authentication.openid.TestValues
import kotlin.test.assertEquals

class TokenResponseUtilTest {
    private val tokenType = "tokenType"
    private val accessToken = "accessToken"
    private val accessTokenExpiryTime = 1000L
    private val idToken = "idToken"
    private val refreshToken = "idToken"
    private lateinit var responseBuilder: net.openid.appauth.TokenResponse.Builder

    @Before
    fun setUp() {
        val request = TokenRequest.Builder(TestValues.testServiceConfig, TestValues.TEST_CLIENT_ID)
            .setAuthorizationCode(TestValues.TEST_AUTH_CODE)
            .setRedirectUri(TestValues.TEST_APP_REDIRECT_URI)
            .build()
        responseBuilder = net.openid.appauth.TokenResponse.Builder(request)
    }

    @Test
    fun toTokenResponseMapsAppAuthTokenResponseToTokenResponse() {
        // GIVEN a net.openid.appauth.TokenResponse object
        val appAuthTokenResponse = responseBuilder
            .setTokenType(tokenType)
            .setAccessToken(accessToken)
            .setAccessTokenExpirationTime(accessTokenExpiryTime)
            .setIdToken(idToken)
            .setRefreshToken(refreshToken)
            .build()
        // WHEN toTokenResponse is called
        val actual = appAuthTokenResponse.toTokenResponse()
        // THEN the values in the net.openid.appauth.TokenResponse object are mapped to their equivalents in TokenResponse
        assertEquals(tokenType, actual.tokenType)
        assertEquals(accessToken, actual.accessToken)
        assertEquals(accessTokenExpiryTime, actual.accessTokenExpirationTime)
        assertEquals(idToken, actual.idToken)
        assertEquals(refreshToken, actual.refreshToken)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullTokenType() {
        // GIVEN a net.openid.appauth.TokenResponse object without tokenType
        val appAuthTokenResponse = responseBuilder
            .setAccessToken(accessToken)
            .setAccessTokenExpirationTime(accessTokenExpiryTime)
            .setIdToken(idToken)
            .setRefreshToken(refreshToken)
            .build()
        // WHEN toTokenResponse is called
        appAuthTokenResponse.toTokenResponse()
        // THEN IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullAccessToken() {
        // GIVEN a net.openid.appauth.TokenResponse object without accessToken
        val appAuthTokenResponse = responseBuilder
            .setTokenType(tokenType)
            .setAccessTokenExpirationTime(accessTokenExpiryTime)
            .setIdToken(idToken)
            .setRefreshToken(refreshToken)
            .build()
        // WHEN toTokenResponse is called
        appAuthTokenResponse.toTokenResponse()
        // THEN IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullAccessTokenExpiryTime() {
        // GIVEN a net.openid.appauth.TokenResponse object without accessTokenExpiryTime
        val appAuthTokenResponse = responseBuilder
            .setTokenType(tokenType)
            .setAccessToken(accessToken)
            .setIdToken(idToken)
            .setRefreshToken(refreshToken)
            .build()
        // WHEN toTokenResponse is called
        appAuthTokenResponse.toTokenResponse()
        // THEN IllegalArgumentException is thrown
    }
}
