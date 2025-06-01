package uk.gov.android.authentication.login

typealias AppAuthTokenResponse = net.openid.appauth.TokenResponse

private const val NULL_TOKEN_TYPE_MESSAGE = "Token type must not be empty"
private const val NULL_ACCESS_TOKEN_MESSAGE = "Access token must not be empty"
private const val NULL_ID_TOKEN_MESSAGE = "ID token must not be empty"
private const val NULL_TOKEN_EXPIRY_MESSAGE = "Token expiry must not be empty"

internal fun AppAuthTokenResponse.toTokenResponse(): TokenResponse =
    TokenResponse(
        tokenType = requireNotNull(tokenType) { NULL_TOKEN_TYPE_MESSAGE },
        accessToken = requireNotNull(accessToken) { NULL_ACCESS_TOKEN_MESSAGE },
        accessTokenExpirationTime =
            requireNotNull(accessTokenExpirationTime) {
                NULL_TOKEN_EXPIRY_MESSAGE
            },
        idToken = requireNotNull(idToken) { NULL_ID_TOKEN_MESSAGE },
        refreshToken = refreshToken,
    )
