package uk.gov.android.authentication

import android.app.Activity
import android.content.Intent

/**
 * This class provides a wrapper for [net.openid.appauth](https://github.com/openid/AppAuth-Android)
 *
 * Use to make OAuth standard login
 */
interface LoginSession {
    /**
     * Present the login dialog using a Chrome Custom Tab
     *
     * @param configuration [LoginSessionConfiguration] containing necessary session configuration
     */
    fun present(
        activity: Activity,
        configuration: LoginSessionConfiguration,
    )

    /**
     * Callback function to handle intent from the activity result started by [present]
     *
     * @param intent The intent from the login activity result
     * @param callback Method to extract and handle local token usage/storage
     * @throws [AuthenticationError] if Authorization fails
     */
    @Throws(Exception::class)
    fun finalise(
        intent: Intent,
        callback: (tokens: TokenResponse) -> Unit,
    )
}
