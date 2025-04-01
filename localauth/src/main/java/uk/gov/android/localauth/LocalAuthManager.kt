package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.preference.LocalAuthPreference

interface LocalAuthManager {
    val localAuthPreference: LocalAuthPreference?

    suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    )
}
