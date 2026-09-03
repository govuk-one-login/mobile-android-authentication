package uk.gov.android.authentication.login

import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import uk.gov.android.authentication.login.openid.TestValues
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenResponseUtilTest {
    private val tokenType = "tokenType"
    private val accessToken = "accessToken"
    private val accessTokenExpiryTime = 1000L
    private val idToken = "idToken"
    private val refreshToken = "idToken"
    private lateinit var responseBuilder: TokenResponse.Builder

    @BeforeTest
    fun setUp() {
        val request =
            TokenRequest
                .Builder(TestValues.testServiceConfig, TestValues.TEST_CLIENT_ID)
                .setAuthorizationCode(TestValues.TEST_AUTH_CODE)
                .setRedirectUri(TestValues.TEST_APP_REDIRECT_URI)
                .build()
        responseBuilder = TokenResponse.Builder(request)
    }

    @Test
    fun toTokenResponseMapsAppAuthTokenResponseToTokenResponse() {
        // Given a net.openid.appauth.TokenResponse object
        val appAuthTokenResponse =
            responseBuilder
                .setTokenType(tokenType)
                .setAccessToken(accessToken)
                .setAccessTokenExpirationTime(accessTokenExpiryTime)
                .setIdToken(idToken)
                .setRefreshToken(refreshToken)
                .build()
        // When calling toTokenResponse
        val actual = appAuthTokenResponse.toTokenResponse()
        // Then map the values in net.openid.appauth.TokenResponse object to their equivalents in TokenResponse
        assertEquals(tokenType, actual.tokenType)
        assertEquals(accessToken, actual.accessToken)
        assertEquals(accessTokenExpiryTime, actual.accessTokenExpirationTime)
        assertEquals(idToken, actual.idToken)
        assertEquals(refreshToken, actual.refreshToken)
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullTokenType() {
        // Given a net.openid.appauth.TokenResponse object without a TokenType
        val appAuthTokenResponse =
            responseBuilder
                .setAccessToken(accessToken)
                .setAccessTokenExpirationTime(accessTokenExpiryTime)
                .setIdToken(idToken)
                .setRefreshToken(refreshToken)
                .build()
        // When calling toTokenResponse
        appAuthTokenResponse.toTokenResponse()
        // Then throw IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullAccessToken() {
        // Given a net.openid.appauth.TokenResponse object without an AccessToken
        val appAuthTokenResponse =
            responseBuilder
                .setTokenType(tokenType)
                .setAccessTokenExpirationTime(accessTokenExpiryTime)
                .setIdToken(idToken)
                .setRefreshToken(refreshToken)
                .build()
        // When calling toTokenResponse
        appAuthTokenResponse.toTokenResponse()
        // Then throw IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException::class)
    fun toTokenResponseThrowsIllegalArgumentExceptionForNullAccessTokenExpiryTime() {
        // Given a net.openid.appauth.TokenResponse object without an AccessTokenExpiryTime
        val appAuthTokenResponse =
            responseBuilder
                .setTokenType(tokenType)
                .setAccessToken(accessToken)
                .setIdToken(idToken)
                .setRefreshToken(refreshToken)
                .build()
        // When calling toTokenResponse
        appAuthTokenResponse.toTokenResponse()
        // Then throw IllegalArgumentException
    }
}
