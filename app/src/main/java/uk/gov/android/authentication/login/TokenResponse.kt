package uk.gov.android.authentication.login

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val idToken: String? = null,
    val refreshToken: String? = null
)
