package uk.gov.android.localauth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.fragment.app.FragmentActivity
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.ui.optin.BioOptInScreenTest.Companion.makeCloseBackEvent
import uk.gov.android.localauth.ui.optin.BioOptInScreenTest.Companion.makeNoWalletScreenEvent
import uk.gov.android.localauth.ui.optin.BioOptInScreenTest.Companion.makeWalletScreenEvent
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.android.localauth.utils.TestActivity
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BiometricsUiManagerTest : FragmentActivityTestCase(true) {
    private var onBack = false
    private var onBioOptIn = false
    private var onBioOptOut = false
    private var onGoToSettings = false
    private lateinit var analyticsLogger: AnalyticsLogger
    private lateinit var activity: FragmentActivity
    private lateinit var uiManager: DialogUiManager

    @Before
    fun setup() {
        analyticsLogger = mock()
        activity = TestActivity()
        uiManager = BiometricsUiManager(analyticsLogger)
    }

    @Test
    fun `display bio opt in screen with wallet`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = true,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_wallet_enableBiometricsBody1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_wallet_enableBiometricsBullet1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_wallet_enableBiometricsBullet2),
            ).performScrollTo().assertExists()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            )

            onNodeWithTag(
                context.getString(R.string.app_enableBiometricsImageTestTag),
            ).assertIsDisplayed()
        }

        verify(analyticsLogger).logEventV3Dot1(makeWalletScreenEvent(context))
    }

    @Test
    fun `display bio opt in screen without wallet`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = false,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )
            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsBody1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsBody2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsBody3),
            ).performScrollTo().assertExists()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            )

            onNodeWithTag(
                context.getString(R.string.app_enableBiometricsImageTestTag),
            ).assertIsDisplayed()
        }

        verify(analyticsLogger).logEventV3Dot1(makeNoWalletScreenEvent(context))
    }

    @Test
    fun `test bio opt in button`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = false,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsNotDisplayed()

            assertTrue(onBioOptIn)
        }
    }

    @Test
    fun `test passcode opt in button`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = false,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsNotDisplayed()

            assertTrue(onBioOptOut)
        }
    }

    @Test
    fun `test back press on bio opt in`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = false,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsDisplayed()

            Espresso.pressBack()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsNotDisplayed()

            assertTrue(onBack)
        }
    }

    @Test
    fun `test close button`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                walletEnabled = false,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithContentDescription(
                context.getString(uk.gov.android.ui.componentsv2.R.string.close_button),
            ).assertIsDisplayed().performClick()
        }

        assertTrue(onBioOptOut)
        verify(analyticsLogger).logEventV3Dot1(makeCloseBackEvent(context))
    }

    @Test
    fun `display go to settings screen`() {
        composeTestRule.apply {
            uiManager.displayGoToSettingsPage(
                activity,
                onBack = { onBack = !onBack },
                onGoToSettings = { onGoToSettings = !onGoToSettings },
            )

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorTitle),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorBody1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorBody2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorBody3),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorNumberedList1),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorNumberedList2),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorNumberedList3),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorGoToSettingsButton),
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test go to settings button`() {
        composeTestRule.apply {
            uiManager.displayGoToSettingsPage(
                activity,
                onBack = { onBack = !onBack },
                onGoToSettings = { onGoToSettings = !onGoToSettings },
            )

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorGoToSettingsButton),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorTitle),
            ).assertIsNotDisplayed()

            assertTrue(onGoToSettings)
        }
    }

    @Test
    fun `test back press on go to settings screen`() {
        composeTestRule.apply {
            uiManager.displayGoToSettingsPage(
                activity,
                onBack = { onBack = !onBack },
                onGoToSettings = { onGoToSettings = !onGoToSettings },
            )

            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorTitle),
            ).assertIsDisplayed()

            Espresso.pressBack()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsTitle),
            ).assertIsNotDisplayed()

            assertTrue(onBack)
        }
    }
}
