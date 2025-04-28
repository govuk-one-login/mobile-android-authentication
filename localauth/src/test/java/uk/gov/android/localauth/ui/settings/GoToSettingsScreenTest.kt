package uk.gov.android.localauth.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
class GoToSettingsScreenTest : FragmentActivityTestCase(false) {
    private val analyticsLogger: AnalyticsLogger = mock()
    private var onBack = false
    private var onGoToSettings = false
    private var onDismiss = 0

    @Test
    fun `test UI`() {
        setup()
        composeTestRule.apply {
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
    fun `test bio opt in button`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorGoToSettingsButton),
            ).performClick()

            assertTrue(onGoToSettings)
        }
    }

    @Test
    fun `test back press`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorTitle),
            ).assertIsDisplayed()

            Espresso.pressBack()

            assertTrue(onBack)
        }
    }

    @Test
    fun `test preview`() {
        composeTestRule.apply {
            setContent {
                GoToSettingsPreview()
            }

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

    private fun setup() {
        composeTestRule.setContent {
            GoToSettingsScreen(
                analyticsLogger = analyticsLogger,
                onBack = { onBack = !onBack },
                onGoToSettings = { onGoToSettings = !onGoToSettings },
                onDismiss = { onDismiss++ },
            )
        }
    }
}
