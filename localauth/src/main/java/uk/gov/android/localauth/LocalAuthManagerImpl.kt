package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.devicesecurity.DeviceSecurityManager
import uk.gov.android.localauth.devicesecurity.DeviceSecurityStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceHandler
import uk.gov.android.localauth.ui.DialogManager

class LocalAuthManagerImpl(
    private val localAuthPrefHandler: LocalAuthPreferenceHandler,
    private val deviceSecurityManager: DeviceSecurityManager,
) : LocalAuthManager {
    private val uiManager = DialogManager()
    private var _localAuthPreference: LocalAuthPreference? = localAuthPrefHandler.getBioPref()
    override val localAuthPreference: LocalAuthPreference?
        get() = _localAuthPreference

    override suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        // Check is local auth preference is not set yet
        if (localAuthPreference == null) {
            localAuthFlowRequired(callbackHandler, activity, localAuhRequired)
        } else {
            // Checks to ensure that if local auth is required and set differently previously (e.g.
            // disabled) but now is required to perform an action, it would enforce the behaviour
            if (localAuthPreference == LocalAuthPreference.Disabled && localAuhRequired) {
                localAuthFlowRequired(callbackHandler, activity, false)
            } else {
                callbackHandler.onSuccess()
            }
        }
    }

    private fun localAuthFlowRequired(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
        localAuhRequired: Boolean,
    ) {
        // Check if device is secure (any passcode and/ or any biometrics)
        if (deviceSecurityManager.isDeviceSecure()) {
            // Check biometric status (available/ enabled) on device
            handleSecureDevice(callbackHandler, activity)
        } else {
            // When device does not have any passcode/ biometrics/ pattern/ etc
            // Check if device requires security
            handleUnsecuredDevice(localAuhRequired, activity, callbackHandler)
        }
    }

    private fun handleUnsecuredDevice(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        if (localAuhRequired) {
            // Request/ Direct user to set some level of security
            uiManager.displayGoToSettingsPage(
                activity = activity,
                onBack = {
                    // TODO: At the moment it will be disabled because we are overriding the BackHandler with this function
                },
                onGoToSettings = { callbackHandler.onFailure() },
            )
        } else {
            // This is treated as success as it's not needed for the acton to be performed
            localAuthPrefHandler.setBioPref(LocalAuthPreference.Disabled)
            callbackHandler.onSuccess()
        }
    }

    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
    ) {
        when (deviceSecurityManager.getCredentialStatus()) {
            DeviceSecurityStatus.SUCCESS -> {
                uiManager.displayBioOptIn(
                    activity = activity,
                    onBack = {
                        // TODO: At the moment it will be disabled because we are overriding the BackHandler with this function
                    },
                    onBiometricsOptIn = {
                        localAuthPrefHandler.setBioPref(LocalAuthPreference.Enabled(true))
                    },
                    onBiometricsOptOut = {
                        localAuthPrefHandler.setBioPref(LocalAuthPreference.Enabled(false))
                    },
                )
                callbackHandler.onSuccess()
            }

            else -> {
                // Check if local auth has NOT already been set to biometrics enabled
                // This checks for null, Disabled and Enabled(false) - and overrides if device
                // is secure now (ensures the user has passcode as default if enabled on device)
                if (localAuthPreference != LocalAuthPreference.Enabled(true)) {
                    // Set passcode as default (this is enabled because of the .isDeviceSecure()
                    // called above
                    localAuthPrefHandler
                        .setBioPref(LocalAuthPreference.Enabled(false))
                    callbackHandler.onSuccess()
                }
            }
        }
    }
}
