package uk.gov.android.authentication

import android.content.Context
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
        configuration: LoginSessionConfiguration
    )

    /**
     * Call after creation to create the [net.openid.appauth.AuthorizationService] instance
     *
     * @param context Used to create the AuthorizationService
     */
    fun init(context: Context): LoginSession

    /**
     * Callback function to handle intent from the activity result started by [present]
     *
     * @param intent The intent from the login activity result
     * @param callback Method to extract and handle local token usage/storage
     */
    fun finalise(intent: Intent, callback: (tokens: TokenResponse) -> Unit)
}
