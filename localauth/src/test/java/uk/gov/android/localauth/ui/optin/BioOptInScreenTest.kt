package uk.gov.android.localauth.ui.optin

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
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

    @Before
    fun setup() {
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

    @Test
    fun `test UI`() {
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_body1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_body2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_body3),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_bio_button),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_passcode_button),
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test bio opt in button`() {
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.bio_opt_in_bio_button),
            ).performClick()

            assertTrue(onBioOptIn)
        }
    }

    @Test
    fun `test passcode opt in button`() {
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.bio_opt_in_passcode_button),
            ).performClick()

            assertTrue(onBioOptOut)
        }
    }

    @Test
    fun `test back press`() {
        composeTestRule.apply {
            activityRule.scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }

            assertTrue(onBack)
        }
    }
}
