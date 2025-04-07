package uk.gov.android.localauth.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.android.localauth.utils.TestActivity
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
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
    fun `display bio opt in screen`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )
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
            uiManager.displayBioOptIn(
                activity,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.bio_opt_in_bio_button),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsNotDisplayed()

            assertTrue(onBioOptIn)
        }
    }

    @Test
    fun `test passcode opt in button`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.bio_opt_in_passcode_button),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsNotDisplayed()

            assertTrue(onBioOptOut)
        }
    }

    @Test
    fun `test back press on bio opt in`() {
        composeTestRule.apply {
            uiManager.displayBioOptIn(
                activity,
                onBack = { onBack = !onBack },
                onBiometricsOptIn = { onBioOptIn = !onBioOptIn },
                onBiometricsOptOut = { onBioOptOut = !onBioOptOut },
            )

            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsDisplayed()

            activityRule.scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }

            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsNotDisplayed()

            assertTrue(onBack)
        }
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
                context.getString(R.string.go_to_settings_title),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_body1),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_body2),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_numbered_list_title),
            ).assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_numbered_list_step1),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_numbered_list_step2),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_numbered_list_step3),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_numbered_list_step4),
            ).performScrollTo().assertIsDisplayed()

            onNodeWithText(
                context.getString(R.string.go_to_settings_button),
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
                context.getString(R.string.go_to_settings_button),
            ).performClick()

            onNodeWithText(
                context.getString(R.string.go_to_settings_title),
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
                context.getString(R.string.go_to_settings_title),
            ).assertIsDisplayed()

            activityRule.scenario.onActivity { activity ->
                activity.onBackPressedDispatcher.onBackPressed()
            }

            onNodeWithText(
                context.getString(R.string.bio_opt_in_title),
            ).assertIsNotDisplayed()

            assertTrue(onBack)
        }
    }
}
