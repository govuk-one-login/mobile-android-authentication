package uk.gov.android.localauth.ui.optout

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
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
import uk.gov.android.localauth.ui.optout.BioOptOutAnalyticsViewModel.Companion.makeBackButtonEvent
import uk.gov.android.localauth.ui.optout.BioOptOutAnalyticsViewModel.Companion.makeButtonEvent
import uk.gov.android.localauth.ui.optout.BioOptOutAnalyticsViewModel.Companion.makeCloseBackEvent
import uk.gov.android.localauth.ui.optout.BioOptOutAnalyticsViewModel.Companion.makeScreenEvent
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class BioOptOutScreenTest : FragmentActivityTestCase(false) {
    private val analyticsLogger: AnalyticsLogger = mock()
    private var onBack = false
    private var onBioOptIn = false
    private var onDismiss = 0

    @Test
    fun `test UI with wallet`() {
        setupScreen()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsTitle),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody3),
            ).performScrollTo().assertExists()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsButton),
            ).assertIsDisplayed()
        }

        verify(analyticsLogger).logEventV3Dot1(makeScreenEvent(context))
    }

    @Test
    fun `test bio opt in button`() {
        setupScreen()
        val text = R.string.app_optOutBiometricsButton
        composeTestRule.apply {
            onNodeWithText(
                context.getString(text),
            ).performClick()

            assertTrue(onBioOptIn)
        }

        verify(analyticsLogger).logEventV3Dot1(makeButtonEvent(context, text))
    }

    @Test
    fun `test close button`() {
        setupScreen()
        composeTestRule.apply {
            onNodeWithContentDescription(
                context.getString(uk.gov.android.ui.componentsv2.R.string.close_icon_button),
            ).assertIsDisplayed().performClick()
        }

        assertEquals(1, onDismiss)
        verify(analyticsLogger).logEventV3Dot1(makeCloseBackEvent(context))
    }

    @Test
    fun `test back press`() {
        setupScreen()
        composeTestRule.apply {
            Espresso.pressBack()

            assertTrue(onBack)
            verify(analyticsLogger).logEventV3Dot1(makeBackButtonEvent(context))
        }
    }

    @Test
    fun `test copy preview`() {
        composeTestRule.apply {
            setContent {
                BioOptOutPreview()
            }
            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsTitle),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsBody3),
            ).performScrollTo().assertExists()

            onNodeWithText(
                context.getString(R.string.app_optOutBiometricsButton),
            ).assertIsDisplayed()
        }
    }

    private fun setupScreen() {
        composeTestRule.setContent {
            BioOptOutScreen(
                analyticsLogger = analyticsLogger,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onDismiss = { onDismiss++ },
            )
        }
    }
}
