package uk.gov.android.localauth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceRepository
import uk.gov.android.localauth.ui.BiometricsUiManager
import uk.gov.android.localauth.ui.optin.BioOptInAnalyticsViewModel
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@Suppress("ForbiddenComment")
class LocalAuthManagerImpl(
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
                (localAuthPreference is LocalAuthPreference.Enabled) -> callbackHandler.onSuccess()
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
            // TODO: GA4 analytics for screen event to be implemented
            // Set pref to Disabled since there's no local auth and the user would now become a
            // returning user
            uiManager.displayGoToSettingsPage(
                activity = activity,
                onBack = {
                    // TODO: Additional behaviour (if required) + Analytics
                    callbackHandler.onBack()
                    callbackHandler.onFailure()
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
                onGoToSettings = {
                    callbackHandler.onFailure()
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
            )
        } else {
            // This is treated as success as it's not needed for the acton to be performed
            localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
            callbackHandler.onSuccess()
        }
    }

    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
    ) {
        when (deviceBiometricsManager.getCredentialStatus()) {
            DeviceBiometricsStatus.SUCCESS -> {
                val analyticsViewModel = BioOptInAnalyticsViewModel(
                    activity as Context,
                    analyticsLogger,
                )
                // Track GA4 analytics screen event
                analyticsViewModel.trackBioOptInScreen()
                uiManager.displayBioOptIn(
                    activity = activity,
                    onBack = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(false),
                        )
                        callbackHandler.onSuccess()
                        callbackHandler.onBack()
                        analyticsViewModel.trackBackButton()
                    },
                    onBiometricsOptIn = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(true),
                        )
                        callbackHandler.onSuccess()
                        analyticsViewModel.trackBiometricsButton()
                    },
                    onBiometricsOptOut = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(false),
                        )
                        callbackHandler.onSuccess()
                        analyticsViewModel.trackPasscodeButton()
                    },
                )
            }

            else -> {
                // Set passcode as default (this is enabled because of the .isDeviceSecure()
                // called above
                localAuthPrefRepo
                    .setLocalAuthPref(LocalAuthPreference.Enabled(false))
                callbackHandler.onSuccess()
            }
        }
    }
}
