package uk.gov.android.localauth.ui.optout

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

class BioOptOutAnalyticsViewModel(
    context: Context,
    private val analyticsLogger: AnalyticsLogger,
) : ViewModel() {

    private val screenEvent = makeScreenEvent(context)
    private val closeIconEvent = makeCloseBackEvent(context)
    private val biometricsBtnEvent = makeButtonEvent(
        context,
        R.string.app_optOutBiometricsButton,
    )
    private val backBtnEvent = makeBackButtonEvent(context)

    fun trackBioOptOutScreen() {
        analyticsLogger.logEventV3Dot1(screenEvent)
    }

    fun trackBiometricsButton() {
        analyticsLogger.logEventV3Dot1(biometricsBtnEvent)
    }

    fun trackCloseIconButton() {
        analyticsLogger.logEventV3Dot1(closeIconEvent)
    }

    fun trackBackButton() {
        analyticsLogger.logEventV3Dot1(backBtnEvent)
    }

    companion object {
        internal fun makeScreenEvent(context: Context) = with(context) {
            ViewEvent.Error(
                name = getEnglishString(R.string.app_optOutBiometricsTitle),
                id = getEnglishString(R.string.bio_opt_out_screen_page_id),
                endpoint = "",
                status = "",
                reason = getString(R.string.app_optOutBiometricsErrorReason),
                params = requiredParams,
            )
        }

        internal fun makeButtonEvent(context: Context, text: Int) = with(context) {
            TrackEvent.Button(
                text = getEnglishString(text),
                params = requiredParams,
            )
        }

        internal fun makeCloseBackEvent(context: Context) = with(context) {
            TrackEvent.Icon(
                text = getEnglishString(uk.gov.android.ui.componentsv2.R.string.close_button),
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
            taxonomyLevel2 = TaxonomyLevel2.WALLET,
            taxonomyLevel3 = TaxonomyLevel3.BIOMETRICS,
        )
    }
}
