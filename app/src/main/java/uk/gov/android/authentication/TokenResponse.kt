package uk.gov.android.authentication

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class TokenResponse(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpirationTime: Long,
    val idToken: String? = null,
    val refreshToken: String? = null
) {
    fun jsonSerializeString(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun jsonDeserialize(text: String): TokenResponse? {
            return try {
                Json.decodeFromString(text)
            } catch (e: SerializationException) {
                Log.e(this::class.java.simpleName, e.message, e)
                null
            } catch (e: IllegalArgumentException) {
                Log.e(this::class.java.simpleName, e.message, e)
                null
            }
        }
    }
}
