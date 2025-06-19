package uk.gov.android.localauth.ui.optout

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenBodyContent
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenButton
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.dialog.FullScreenDialogueTopAppBar
import uk.gov.android.ui.patterns.errorscreen.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioOptOutScreen(
    analyticsLogger: AnalyticsLogger,
    onBack: () -> Unit,
    onBiometricsOptIn: () -> Unit,
    onDismiss: () -> Unit,
) {
    val analyticsViewModel = BioOptOutAnalyticsViewModel(LocalContext.current, analyticsLogger)
    analyticsViewModel.trackBioOptOutScreen()
    FullScreenDialogue(
        onDismissRequest = onDismiss,
        topAppBar = {
            FullScreenDialogueTopAppBar(
                onCloseClick = {
                    onBack()
                    analyticsViewModel.trackCloseIconButton()
                    onDismiss()
                },
            ) {
                // Nothing here (no title)
            }
        },
        onBack = {
            onBack()
            analyticsViewModel.trackBackButton()
            onDismiss()
        },
        content = {
            BioOptOutContent(onBiometricsOptIn = {
                onBiometricsOptIn()
                analyticsViewModel.trackBiometricsButton()
                onDismiss()
            })
        },
    )
}

@Composable
private fun BioOptOutContent(
    onBiometricsOptIn: () -> Unit,
) {
    ErrorScreen(
        icon = ErrorScreenIcon.ErrorIcon,
        title = stringResource(R.string.app_optOutBiometricsTitle),
        body = persistentListOf(
            CentreAlignedScreenBodyContent.Text(stringResource(R.string.app_optOutBiometricsBody1)),
            CentreAlignedScreenBodyContent.Text(stringResource(R.string.app_optOutBiometricsBody2)),
            CentreAlignedScreenBodyContent.Text(stringResource(R.string.app_optOutBiometricsBody3)),
        ),
        primaryButton = CentreAlignedScreenButton(
            text = stringResource(R.string.app_optOutBiometricsButton),
            onClick = onBiometricsOptIn,
            showIcon = false,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BioOptOutPreview() {
    GdsTheme {
        FullScreenDialogue(
            onDismissRequest = {},
            topAppBar = {
                FullScreenDialogueTopAppBar({}) {
                    // Nothing here
                }
            },
            onBack = {},
            content = {
                BioOptOutContent(onBiometricsOptIn = {})
            },
        )
    }
}
