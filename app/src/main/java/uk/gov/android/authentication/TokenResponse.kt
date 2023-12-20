package uk.gov.android.authentication

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val idToken: String,
    val refreshToken: String?,
    val scope: String
) {

    fun jsonSerializeString(): String {
        return Json.encodeToString(this)
    }
}
