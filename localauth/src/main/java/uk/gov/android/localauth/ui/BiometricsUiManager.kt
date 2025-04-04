package uk.gov.android.localauth.ui

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity

interface DialogManager {
    fun displayBioOptIn(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
        onBiometricsOptOut: () -> Unit,
    )

    fun displayGoToSettingsPage(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onGoToSettings: () -> Unit,
    )
}

class BiometricsUiManager(
    // TODO: require analytics logger
) : DialogManager {
    override fun displayBioOptIn(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
        onBiometricsOptOut: () -> Unit,
    ) {
        val dialogView =
            ComposeView(activity).apply {
                setContent {
                    BioOptInScreen(onBack, onBiometricsOptIn, onBiometricsOptOut) {
                        (parent as? ViewGroup)?.removeView(this)
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
                    GoToSettingsScreen(onBack, onGoToSettings) {
                        (parent as? ViewGroup)?.removeView(this)
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
