package uk.gov.android.localauth

import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceRepository
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.android.localauth.utils.TestActivity
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@RunWith(AndroidJUnit4::class)
class LocalAuthManagerTest : FragmentActivityTestCase(true) {
    private lateinit var localAuthPreferenceRepository: LocalAuthPreferenceRepository
    private lateinit var deviceBiometricsManager: DeviceBiometricsManager
    private lateinit var callbackHandler: LocalAuthManagerCallbackHandler
    private lateinit var localAuthManager: LocalAuthManagerImpl
    private lateinit var analyticsLogger: AnalyticsLogger
    private lateinit var activity: FragmentActivity

    @Before
    fun setup() {
        localAuthPreferenceRepository = mock()
        deviceBiometricsManager = mock()
        callbackHandler = mock()
        analyticsLogger = mock()
        localAuthManager = LocalAuthManagerImpl(
            localAuthPreferenceRepository,
            deviceBiometricsManager,
            analyticsLogger,
        )
        activity = TestActivity()
    }

    @Test
    fun `device secure - first time user - biometrics enabled - opt in bio - not required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(false, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_bio_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(true))
        }

    @Test
    fun `device secure - first time user - biometrics enabled - opt in bio - required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(true, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_bio_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(true))
        }

    @Test
    fun `device secure - first time user - biometrics enabled - opt out bio - not required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(false, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_passcode_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - biometrics enabled - opt out bio - required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(true, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_passcode_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - biometrics enabled - back press - not required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(false, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                Espresso.pressBack()
            }

            verify(callbackHandler).onSuccess(true)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - biometrics enabled - back press - required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(true, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                Espresso.pressBack()
            }

            verify(callbackHandler).onSuccess(true)
            verify(localAuthPreferenceRepository).getLocalAuthPref()
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - DeviceBiometricsStatus == NO_HARDWARE`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.NO_HARDWARE)

            localAuthManager.enforceAndSet(false, activity, callbackHandler)

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - DeviceBiometricsStatus == HARDWARE_UNAVAILABLE`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.HARDWARE_UNAVAILABLE)

            localAuthManager.enforceAndSet(false, activity, callbackHandler)

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - DeviceBiometricsStatus == NOT_ENROLLED`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.NOT_ENROLLED)

            localAuthManager.enforceAndSet(false, activity, callbackHandler)

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - first time user - DeviceBiometricsStatus == UNKNOWN`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.UNKNOWN)

            localAuthManager.enforceAndSet(false, activity, callbackHandler)

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - returning user - no local auth & bio enabled - required local auth`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)
            whenever(localAuthPreferenceRepository.getLocalAuthPref())
                .thenReturn(LocalAuthPreference.Disabled)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(true, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_passcode_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - returning user - no local auth & bio enabled - not required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.SUCCESS)
            whenever(localAuthPreferenceRepository.getLocalAuthPref())
                .thenReturn(LocalAuthPreference.Disabled)

            composeTestRule.apply {
                localAuthManager.enforceAndSet(false, activity, callbackHandler)
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_title),
                ).isDisplayed()
                onNodeWithText(
                    context.getString(R.string.bio_opt_in_passcode_button),
                ).performClick()
            }

            verify(callbackHandler).onSuccess(false)
            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
        }

    @Test
    fun `device secure - returning user - no local auth & passcode enabled - required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.NOT_ENROLLED)
            whenever(localAuthPreferenceRepository.getLocalAuthPref())
                .thenReturn(LocalAuthPreference.Disabled)

            localAuthManager.enforceAndSet(true, activity, callbackHandler)

            verify(localAuthPreferenceRepository)
                .setLocalAuthPref(LocalAuthPreference.Enabled(false))
            verify(callbackHandler).onSuccess(false)
        }

    @Test
    fun `device secure - returning user - no local auth - local auth not required`() =
        runBlocking {
            whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(true)
            whenever(deviceBiometricsManager.getCredentialStatus())
                .thenReturn(DeviceBiometricsStatus.NOT_ENROLLED)
            whenever(localAuthPreferenceRepository.getLocalAuthPref())
                .thenReturn(LocalAuthPreference.Disabled)

            localAuthManager.enforceAndSet(false, activity, callbackHandler)

            verify(callbackHandler).onSuccess(false)
        }

    @Test
    fun `device unsecure - first time user - local auth not required`() = runBlocking {
        whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(false)
        whenever(localAuthPreferenceRepository.getLocalAuthPref()).thenReturn(null)
        localAuthManager.enforceAndSet(false, activity, callbackHandler)

        verify(callbackHandler).onSuccess(false)
        verify(localAuthPreferenceRepository).setLocalAuthPref(LocalAuthPreference.Disabled)
    }

    @Test
    fun `device unsecure - first time user - local auth required`() = runBlocking {
        whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(false)

        composeTestRule.apply {
            localAuthManager.enforceAndSet(true, activity, callbackHandler)
            onNodeWithText(
                context.getString(R.string.go_to_settings_title),
            ).isDisplayed()
            onNodeWithText(
                context.getString(R.string.go_to_settings_button),
            ).performClick()
        }

        verify(callbackHandler).onFailure(false)
        verify(localAuthPreferenceRepository).setLocalAuthPref(LocalAuthPreference.Disabled)
    }

    @Test
    fun `device unsecure - first time user - local auth required - back press`() = runBlocking {
        whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(false)

        composeTestRule.apply {
            localAuthManager.enforceAndSet(true, activity, callbackHandler)
            onNodeWithText(
                context.getString(R.string.go_to_settings_title),
            ).isDisplayed()
            Espresso.pressBack()
        }

        verify(callbackHandler).onFailure(true)
        verify(localAuthPreferenceRepository).setLocalAuthPref(LocalAuthPreference.Disabled)
    }

    @Test
    fun `device unsecure - returning user - local auth required - back press`() = runBlocking {
        whenever(deviceBiometricsManager.isDeviceSecure()).thenReturn(false)

        composeTestRule.apply {
            localAuthManager.enforceAndSet(true, activity, callbackHandler)
            onNodeWithText(
                context.getString(R.string.go_to_settings_title),
            ).isDisplayed()
            Espresso.pressBack()
        }

        verify(callbackHandler).onFailure(true)
        verify(localAuthPreferenceRepository).setLocalAuthPref(LocalAuthPreference.Disabled)
    }
}
