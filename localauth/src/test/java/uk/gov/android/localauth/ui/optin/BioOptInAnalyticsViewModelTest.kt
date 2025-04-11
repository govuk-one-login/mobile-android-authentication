package uk.gov.android.localauth.ui.optin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
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
class BioOptInAnalyticsViewModelTest {
    private lateinit var name: String
    private lateinit var id: String
    private lateinit var passcodeBtn: String
    private lateinit var biometricsBtn: String
    private lateinit var backBtn: String
    private lateinit var requiredParameters: RequiredParameters
    private lateinit var logger: AnalyticsLogger
    private lateinit var viewModel: BioOptInAnalyticsViewModel

    @Before
    fun setup() {
        val context: Context = ApplicationProvider.getApplicationContext()
        logger = mock()
        requiredParameters = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.LOGIN,
            taxonomyLevel3 = TaxonomyLevel3.BIOMETRICS,
        )
        name = context.getEnglishString(R.string.bio_opt_in_title)
        id = context.getEnglishString(R.string.bio_opt_in_screen_page_id)
        passcodeBtn = context.getEnglishString(R.string.bio_opt_in_passcode_button)
        biometricsBtn = context.getEnglishString(R.string.bio_opt_in_bio_button)
        backBtn = context.getEnglishString(R.string.system_backButton)
        viewModel = BioOptInAnalyticsViewModel(context, logger)
    }

    @Test
    fun trackBioOptInScreen() {
        val event = ViewEvent.Screen(
            name = name,
            id = id,
            params = requiredParameters,
        )

        viewModel.trackBioOptInScreen()

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
                    viewModel.trackPasscodeButton()
                },
                text = passcodeBtn,
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
            name = name,
            id = id,
            params = requiredParameters,
        )

        viewModel.trackBioOptInScreen()

        verify(logger).logEventV3Dot1(event)
    }
}
