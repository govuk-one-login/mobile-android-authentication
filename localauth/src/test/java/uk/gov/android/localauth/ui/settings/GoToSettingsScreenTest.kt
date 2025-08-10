package uk.gov.android.localauth.ui.settings

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
import uk.gov.android.localauth.utils.FragmentActivityTestCase
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import kotlin.test.assertTrue
import uk.gov.android.ui.componentsv2.R as ComponentsR

@RunWith(AndroidJUnit4::class)
class GoToSettingsScreenTest : FragmentActivityTestCase(false) {
    private val analyticsLogger: AnalyticsLogger = mock()
    private var onBack = false
    private var onGoToSettings = false
    private var onDismiss = false

    private val requiredParams = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.LOCAL_AUTH_MANAGER,
        taxonomyLevel3 = TaxonomyLevel3.UNDEFINED,
    )

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
        verify(analyticsLogger).logEventV3Dot1(
            ViewEvent.Error(
                name = context.getString(R.string.app_localAuthManagerErrorTitle),
                id = context.getString(R.string.go_settings_screen_page_id),
                endpoint = "",
                status = "",
                reason = context.getString(R.string.app_localAuthManagerErrorReason),
                params = requiredParams,
            ),
        )
    }

    @Test
    fun `test primary button`() {
        setup()
        composeTestRule.apply {
            onNodeWithText(
                context.getString(R.string.app_localAuthManagerErrorGoToSettingsButton),
            ).performClick()

            assertTrue(onGoToSettings)
        }
        verify(analyticsLogger).logEventV3Dot1(
            TrackEvent.Button(
                text = context.getString(R.string.app_localAuthManagerErrorGoToSettingsButton),
                params = requiredParams,
            ),
        )
    }

    @Test
    fun `test close button`() {
        setup()
        composeTestRule.apply {
            onNodeWithContentDescription(
                context.getString(ComponentsR.string.close_button),
            ).performClick()

            assertTrue(onBack)
            assertTrue(onDismiss)
        }
        verify(analyticsLogger).logEventV3Dot1(
            TrackEvent.Button(
                text = context.getString(R.string.system_backButton),
                params = requiredParams,
            ),
        )
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
            assertTrue(onDismiss)
        }
        verify(analyticsLogger).logEventV3Dot1(
            TrackEvent.Button(
                text = context.getString(R.string.system_backButton),
                params = requiredParams,
            ),
        )
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
                onDismiss = { onDismiss = !onDismiss },
            )
        }
    }
}
