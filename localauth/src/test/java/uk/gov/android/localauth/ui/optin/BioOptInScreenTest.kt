package uk.gov.android.localauth.ui.optin

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.DebugOnlyRobolectricTestRunner
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import kotlin.test.assertTrue

@RunWith(DebugOnlyRobolectricTestRunner::class)
class BioOptInScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()
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
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.bio_opt_in_bio_button),
            ).performClick()

            assertTrue(onBioOptIn)
        }
    }

    @Test
    fun `test passcode opt in button`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.bio_opt_in_passcode_button),
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
            ).assertExists()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_bio_button),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_passcode_button),
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
