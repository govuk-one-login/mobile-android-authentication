package uk.gov.android.localauth.ui.optin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BioOptInScreenTest : FragmentActivityTestCase(false) {
    private val analyticsLogger: AnalyticsLogger = mock()
    private var onBack = false
    private var onBioOptIn = false
    private var onBioOptOut = false
    private var onDismiss = 0

    @Test
    fun `test UI`() {
        setup()
        composeTestRule.apply {
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
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test bio opt in button`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).performClick()

            assertTrue(onBioOptIn)
        }
    }

    @Test
    fun `test passcode opt in button`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            ).performClick()

            assertTrue(onBioOptOut)
        }
    }

    @Test
    fun `test back press`() {
        setup()
        composeTestRule.apply {
            Espresso.pressBack()

            assertTrue(onBack)
        }
    }

    @Test
    fun `test preview`() {
        composeTestRule.apply {
            setContent {
                BioOptInPreview()
            }
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
            ).assertExists()

            onNodeWithText(
                context.getString(R.string.app_enableBiometricsButton),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_enablePasscodeOrPatternButton),
            ).assertIsDisplayed()
        }
    }

    private fun setup() {
        composeTestRule.setContent {
            BioOptInScreen(
                analyticsLogger = analyticsLogger,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
                onDismiss = { onDismiss++ },
            )
        }
    }
}
