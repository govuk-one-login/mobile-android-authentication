package uk.gov.android.localauth.ui.optin

import android.content.Context
import androidx.lifecycle.ViewModel
import uk.gov.android.authentication.localauth.R
import uk.gov.logging.api.analytics.extensions.getEnglishString
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel3
import uk.gov.logging.api.v3dot1.logger.logEventV3Dot1
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent

class BioOptInAnalyticsViewModel(
    context: Context,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {
    private val screenEvent = makeScreenEvent(context)
    private val biometricsBtnEvent = makeButtonEvent(
        context,
        R.string.bio_opt_in_bio_button,
    )
    private val passcodeBtnEvent =
        makeButtonEvent(
            context,
            R.string.bio_opt_in_passcode_button,
        )
    private val backBtnEvent = makeBackButtonEvent(context)

    fun trackBioOptInScreen() {
        analyticsLogger.logEventV3Dot1(screenEvent)
    }

    fun trackBiometricsButton() {
        analyticsLogger.logEventV3Dot1(biometricsBtnEvent)
    }

    fun trackPasscodeButton() {
        analyticsLogger.logEventV3Dot1(passcodeBtnEvent)
    }

    fun trackBackButton() {
        analyticsLogger.logEventV3Dot1(backBtnEvent)
    }

    companion object {
        internal fun makeScreenEvent(context: Context) = with(context) {
            ViewEvent.Screen(
                name = getEnglishString(R.string.bio_opt_in_title),
                id = getEnglishString(R.string.bio_opt_in_screen_page_id),
                params = requiredParams,
            )
        }

        internal fun makeButtonEvent(context: Context, text: Int) = with(context) {
            TrackEvent.Button(
                text = getEnglishString(text),
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
            taxonomyLevel2 = TaxonomyLevel2.LOGIN,
            taxonomyLevel3 = TaxonomyLevel3.BIOMETRICS,
        )
    }
}
