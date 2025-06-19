package uk.gov.android.localauth.ui

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.ui.optin.BioOptInScreen
import uk.gov.android.localauth.ui.optout.BioOptOutScreen
import uk.gov.android.localauth.ui.settings.GoToSettingsScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

interface DialogUiManager {
    fun displayBioOptIn(
        activity: FragmentActivity,
        walletEnabled: Boolean,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
        onBiometricsOptOut: () -> Unit,
    )

    fun displayGoToSettingsPage(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onGoToSettings: () -> Unit,
    )

    fun displayBioOptOut(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
    )
}

class BiometricsUiManager(
    private val analyticsLogger: AnalyticsLogger,
) : DialogUiManager {
    override fun displayBioOptIn(
        activity: FragmentActivity,
        walletEnabled: Boolean,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
        onBiometricsOptOut: () -> Unit,
    ) {
        val dialogView =
            ComposeView(activity).apply {
                setContent {
                    GdsTheme {
                        BioOptInScreen(
                            analyticsLogger,
                            walletEnabled,
                            onBack,
                            onBiometricsOptIn,
                            onBiometricsOptOut,
                        ) {
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    }
                }
            }
        activity.addContentView(
            dialogView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    override fun displayGoToSettingsPage(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onGoToSettings: () -> Unit,
    ) {
        val dialogView =
            ComposeView(activity).apply {
                setContent {
                    GdsTheme {
                        GoToSettingsScreen(analyticsLogger, onBack, onGoToSettings) {
                            (parent as? ViewGroup)?.removeView(this)
                        }
                    }
                }
            }
        activity.addContentView(
            dialogView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }

    override fun displayBioOptOut(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
    ) {
        val dialogView = ComposeView(activity).apply {
            setContent {
                GdsTheme {
                    BioOptOutScreen(analyticsLogger, onBack, onBiometricsOptIn) {
                        (parent as? ViewGroup)?.removeView(this)
                    }
                }
            }
        }
        activity.addContentView(
            dialogView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
    }
}
