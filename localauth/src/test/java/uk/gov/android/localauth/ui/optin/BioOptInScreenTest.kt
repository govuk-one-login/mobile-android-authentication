package uk.gov.android.localauth.ui.optin

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import uk.gov.android.ui.componentsv2.R as componentsR

@RunWith(AndroidJUnit4::class)
class BioOptInScreenTest : FragmentActivityTestCase(false) {
    private val analyticsLogger: AnalyticsLogger = mock()
    private var onBack = false
    private var onBioOptIn = false
    private var onBioOptOut = false
    private var onDismiss = 0

    @Test
    fun `test UI with wallet`() {
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
    fun `test bio opt in button`() {
        setup()
        val text = R.string.app_enableBiometricsButton
        composeTestRule.apply {
            onNodeWithText(
                context.getString(text),
            ).performClick()

            assertTrue(onBioOptIn)
        }

        verify(analyticsLogger).logEventV3Dot1(makeButtonEvent(context, text))
    }

    @Test
    fun `test opt out button`() {
        setup()
        val text = R.string.app_enablePasscodeOrPatternButton
        composeTestRule.apply {
            onNodeWithText(context.getString(text)).performClick()

            assertTrue(onBioOptOut)
        }

        verify(analyticsLogger).logEventV3Dot1(makeButtonEvent(context, text))
    }

    @Test
    fun `test close button`() {
        setup()
        composeTestRule.apply {
            onNodeWithContentDescription(
                context.getString(componentsR.string.close_icon_button),
            ).assertIsDisplayed().performClick()
        }

        assertEquals(1, onDismiss)
        verify(analyticsLogger).logEventV3Dot1(makeCloseBackEvent(context))
    }

    @Test
    fun `test back press`() {
        setup()
        composeTestRule.apply {
            Espresso.pressBack()

            assertTrue(onBack)
            verify(analyticsLogger).logEventV3Dot1(makeBackButtonEvent(context))
        }
    }

    @Test
    fun `DEPRECATED - test UI with wallet`() {
        deprecatedSetupWallet()
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
    fun `DEPRECATED - test UI without wallet`() {
        deprecatedSetupNoWallet()
        composeTestRule.apply {
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
    fun `DEPRECATED - test bio opt in button`() {
        deprecatedSetupWallet()
        val text = R.string.app_enableBiometricsButton
        composeTestRule.apply {
            onNodeWithText(
                context.getString(text),
            ).performClick()

            assertTrue(onBioOptIn)
        }

        verify(analyticsLogger).logEventV3Dot1(makeButtonEvent(context, text))
    }

    @Test
    fun `DEPRECATED - test opt out button`() {
        deprecatedSetupWallet()
        val text = R.string.app_enablePasscodeOrPatternButton
        composeTestRule.apply {
            onNodeWithText(context.getString(text)).performClick()

            assertTrue(onBioOptOut)
        }

        verify(analyticsLogger).logEventV3Dot1(makeButtonEvent(context, text))
    }

    @Test
    fun `DEPRECATED - test close button`() {
        deprecatedSetupWallet()
        composeTestRule.apply {
            onNodeWithContentDescription(
                context.getString(componentsR.string.close_icon_button),
            ).assertIsDisplayed().performClick()
        }

        assertEquals(1, onDismiss)
        verify(analyticsLogger).logEventV3Dot1(makeCloseBackEvent(context))
    }

    @Test
    fun `DEPRECATED - test back press`() {
        deprecatedSetupWallet()
        composeTestRule.apply {
            Espresso.pressBack()

            assertTrue(onBack)
            verify(analyticsLogger).logEventV3Dot1(makeBackButtonEvent(context))
        }
    }

    @Test
    fun `DEPRECATED - test wallet copy preview`() {
        composeTestRule.apply {
            setContent {
                DeprecatedBioOptInPreviewWallet()
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

            onNodeWithTag(
                context.getString(R.string.app_enableBiometricsImageTestTag),
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `DEPRECATED - test no wallet copy preview`() {
        composeTestRule.apply {
            setContent {
                DeprecatedBioOptInPreview()
            }
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

            onNodeWithTag(
                context.getString(R.string.app_enableBiometricsImageTestTag),
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

    private fun deprecatedSetupWallet() {
        composeTestRule.setContent {
            BioOptInScreen(
                analyticsLogger = analyticsLogger,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
                onDismiss = { onDismiss++ },
                walletEnabled = true,
            )
        }
    }

    private fun deprecatedSetupNoWallet() {
        composeTestRule.setContent {
            BioOptInScreen(
                analyticsLogger = analyticsLogger,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
                onDismiss = { onDismiss++ },
                walletEnabled = false,
            )
        }
    }

    companion object {
        internal fun makeWalletScreenEvent(context: Context) = with(context) {
            ViewEvent.Screen(
                name = getEnglishString(R.string.app_enableBiometricsTitle),
                id = getEnglishString(R.string.bio_opt_in_screen_wallet_page_id),
                params = requiredParams,
            )
        }

        internal fun makeNoWalletScreenEvent(context: Context) = with(context) {
            ViewEvent.Screen(
                name = getEnglishString(R.string.app_enableBiometricsTitle),
                id = getEnglishString(R.string.bio_opt_in_screen_no_wallet_page_id),
                params = requiredParams,
            )
        }

        internal fun makeButtonEvent(context: Context, text: Int) = with(context) {
            TrackEvent.Button(
                text = getEnglishString(text),
                params = requiredParams,
            )
        }

        internal fun makeCloseBackEvent(context: Context) = with(context) {
            TrackEvent.Icon(
                text = getEnglishString(uk.gov.android.ui.componentsv2.R.string.close_icon_button),
                params = requiredParams,
            )
        }

        internal fun makeBackButtonEvent(context: Context) = with(context) {
            TrackEvent.Button(
                text = getEnglishString(R.string.system_backButton),
                params = requiredParams,
            )
        }

        private val requiredParams = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.LOCAL_AUTH_MANAGER,
            taxonomyLevel3 = TaxonomyLevel3.UNDEFINED,
        )
    }
}
