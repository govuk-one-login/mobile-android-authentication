package uk.gov.android.localauth.ui.optout

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import uk.gov.android.authentication.localauth.R
import uk.gov.android.localauth.utils.TestUtils
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent

@RunWith(AndroidJUnit4::class)
class BioOptOutAnalyticsViewModelTest {
    private lateinit var title: String
    private lateinit var id: String
    private lateinit var biometricsBtn: String
    private lateinit var backBtn: String
    private lateinit var requiredParameters: RequiredParameters
    private lateinit var logger: AnalyticsLogger
    private lateinit var viewModel: BioOptOutAnalyticsViewModel

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        logger = mock()
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.LOCAL_AUTH_MANAGER,
            taxonomyLevel3 = TaxonomyLevel3.UNDEFINED,
        )
        title = context.getEnglishString(R.string.app_optOutBiometricsTitle)
        id = context.getEnglishString(R.string.bio_opt_out_screen_page_id)
        biometricsBtn = context.getEnglishString(R.string.app_optOutBiometricsButton)
        backBtn = context.getEnglishString(R.string.system_backButton)
        viewModel = BioOptOutAnalyticsViewModel(context, logger)
    }

    @Test
    fun trackBioOptOutScreenEvent() {
        val event = ViewEvent.Screen(
            name = title,
            id = id,
            params = requiredParameters,
        )
        viewModel.trackBioOptOutScreen()

        verify(logger).logEventV3Dot1(event)
    }

    @Test
    fun trackButtons() {
        listOf(
            TestUtils.TrackEventTestCase.Button(
                trackFunction = {
                    viewModel.trackBiometricsButton()
                },
                text = biometricsBtn,
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

    @Test
    fun trackBackButton() {
        val event = ViewEvent.Screen(
            name = title,
            id = id,
            params = requiredParameters,
        )

        viewModel.trackBioOptOutScreen()

        verify(logger).logEventV3Dot1(event)
    }
}
