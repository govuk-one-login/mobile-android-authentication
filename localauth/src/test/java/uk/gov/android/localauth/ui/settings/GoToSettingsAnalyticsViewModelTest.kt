package uk.gov.android.localauth.ui.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.GAUtils
import uk.gov.android.localauth.utils.GAUtils.IS_ERROR_REASON_TRUE
import uk.gov.android.localauth.utils.GAUtils.TRUE
import uk.gov.android.localauth.utils.TestUtils
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent

@RunWith(AndroidJUnit4::class)
class GoToSettingsAnalyticsViewModelTest {
    private lateinit var name: String
    private lateinit var id: String
    private lateinit var reason: String
    private lateinit var primaryBtn: String
    private lateinit var backBtn: String
    private lateinit var requiredParameters: RequiredParameters
    private lateinit var logger: AnalyticsLogger
    private lateinit var viewModel: GoToSettingsAnalyticsViewModel

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        logger = mock()
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.ONBOARDING,
            taxonomyLevel3 = TaxonomyLevel3.UNDEFINED,
        )
        name = context.getEnglishString(R.string.app_localAuthManagerErrorTitle)
        id = context.getEnglishString(R.string.go_settings_screen_page_id)
        primaryBtn = context.getEnglishString(R.string.app_localAuthManagerErrorGoToSettingsButton)
        backBtn = context.getEnglishString(R.string.system_backButton)
        reason = context.getString(R.string.app_localAuthManagerErrorReason)
        viewModel = GoToSettingsAnalyticsViewModel(context, logger)
    }

    @Test
    fun trackScreen() {
        val event = ViewEvent.Error(
            name = name,
            id = id,
            endpoint = "",
            status = "",
            reason = reason,
            params = requiredParameters,
        )

        viewModel.trackScreen()

        verify(logger).logEventV3Dot1(event)

        assertThat(
            IS_ERROR_REASON_TRUE,
            GAUtils.containsIsError(event, TRUE),
        )
    }

    @Test
    fun trackButtons() {
        listOf(
            TestUtils.TrackEventTestCase.Button(
                trackFunction = {
                    viewModel.trackPrimaryButton()
                },
                text = primaryBtn,
            ),
            TestUtils.TrackEventTestCase.Button(
                trackFunction = {
                    viewModel.trackBackButton()
                },
                text = backBtn,
            ),
        ).forEach {
            val result = TestUtils.executeTrackEventTestCase(it, requiredParameters)

            verify(logger).logEventV3Dot1(result)
        }
    }
}
