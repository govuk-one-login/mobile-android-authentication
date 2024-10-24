package uk.gov.android.authentication.login

typealias AppAuthTokenResponse = net.openid.appauth.TokenResponse

private const val NullTokenTypeMessage = "Token type must not be empty"
private const val NullAccessTokenMessage = "Access token must not be empty"
private const val NullTokenExpiryMessage = "Token expiry must not be empty"

internal fun AppAuthTokenResponse.toTokenResponse(): TokenResponse = TokenResponse(
    tokenType = requireNotNull(tokenType) { NullTokenTypeMessage },
    accessToken = requireNotNull(accessToken) { NullAccessTokenMessage },
    accessTokenExpirationTime = requireNotNull(accessTokenExpirationTime) { NullTokenExpiryMessage },
    idToken = idToken,
    refreshToken = refreshToken
)
