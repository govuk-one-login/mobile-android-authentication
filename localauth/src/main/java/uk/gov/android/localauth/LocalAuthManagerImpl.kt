package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceRepository
import uk.gov.android.localauth.ui.BiometricsUiManager
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

/**
 * [LocalAuthManagerImpl] checks if the device is secure and based on that maps the local authentication preferences accordingly
 * and stores them in memory.
 *
 * @param localAuthPrefRepo Stores the [localAuthPreference]
 * @param deviceBiometricsManager Checks for device is secured and/ or biometrics are available and enabled
 * @param analyticsLogger Provides analytics logger for GA4 events for the [BiometricsUiManager]
 *
 * * The functionality is based on the [enforceAndSet]:
 *
 *
 */
@Suppress("ForbiddenComment")
open class LocalAuthManagerImpl(
    private val localAuthPrefRepo: LocalAuthPreferenceRepository,
    private val deviceBiometricsManager: DeviceBiometricsManager,
    private val analyticsLogger: AnalyticsLogger,
) : LocalAuthManager {
    private val uiManager = BiometricsUiManager(analyticsLogger)
    override val localAuthPreference: LocalAuthPreference?
        get() = localAuthPrefRepo.getLocalAuthPref()

    override suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        // Check if device is secure (any passcode and/ or any biometrics)
        if (deviceBiometricsManager.isDeviceSecure()) {
            when {
                // LocalAuthPref already set and saved (passcode/ biometrics) -- continue onSuccess
                (localAuthPreference is LocalAuthPreference.Enabled)
                -> callbackHandler.onSuccess(false)
                else -> {
                    // Go through the local auth flow
                    handleSecureDevice(callbackHandler, activity)
                }
            }
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
            uiManager.displayGoToSettingsPage(
                activity = activity,
                onBack = {
                    callbackHandler.onFailure(true)
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
                onGoToSettings = {
                    callbackHandler.onFailure(false)
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
            )
        } else {
            // This is treated as success as it's not needed for the acton to be performed
            localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
            callbackHandler.onSuccess(false)
        }
    }

    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
    ) {
        when (deviceBiometricsManager.getCredentialStatus()) {
            DeviceBiometricsStatus.SUCCESS -> {
                uiManager.displayBioOptIn(
                    activity = activity,
                    onBack = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(false),
                        )
                        callbackHandler.onSuccess(true)
                    },
                    onBiometricsOptIn = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(true),
                        )
                        callbackHandler.onSuccess(false)
                    },
                    onBiometricsOptOut = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(false),
                        )
                        callbackHandler.onSuccess(false)
                    },
                )
            }

            else -> {
                // Set passcode as default (this is enabled because of the .isDeviceSecure()
                // called above
                localAuthPrefRepo
                    .setLocalAuthPref(LocalAuthPreference.Enabled(false))
                callbackHandler.onSuccess(false)
            }
        }
    }
}
