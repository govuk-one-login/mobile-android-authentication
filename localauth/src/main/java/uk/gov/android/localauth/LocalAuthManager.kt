package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity

interface LocalAuthManager {
    val localAuthPreference: LocalAuthPreference

    suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    )
}
