package uk.gov.android.localauth.ui.settings

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.bulletedlist.BulletedListTitle
import uk.gov.android.ui.componentsv2.bulletedlist.TitleType
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenBodyContent
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenButton
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.errorscreen.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@Composable
@Suppress("UnusedParameter")
fun GoToSettingsScreen(
    analyticsLogger: AnalyticsLogger,
    onBack: () -> Unit,
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler {
        onBack()
        onDismiss()
    }
    FullScreenDialogue(
        onDismissRequest = onDismiss,
        title = "",
        content = {
            GoToSettingsContent {
                onGoToSettings()
                // Add the intent to open settings here
                onDismiss()
            }
        },
    )
}

@Composable
private fun GoToSettingsContent(
    onGoToSettings: () -> Unit,
) {
    val body1 = stringResource(R.string.go_to_settings_body1)
    val body2 = stringResource(R.string.go_to_settings_body2)
    val numberedListTitle = stringResource(R.string.go_to_settings_numbered_list_title)
    val numberedListStep1 = stringResource(R.string.go_to_settings_numbered_list_step1)
    val numberedListStep2 = stringResource(R.string.go_to_settings_numbered_list_step2)
    val numberedListStep3 = stringResource(R.string.go_to_settings_numbered_list_step3)
    val numberedListStep4 = stringResource(R.string.go_to_settings_numbered_list_step4)
    ErrorScreen(
        icon = ErrorScreenIcon.ErrorIcon,
        title = stringResource(R.string.go_to_settings_title),
        modifier = Modifier.fillMaxSize(),
        body =
        persistentListOf(
            CentreAlignedScreenBodyContent.Text(body1),
            CentreAlignedScreenBodyContent.Text(body2),
            CentreAlignedScreenBodyContent.BulletList(
                title = BulletedListTitle(numberedListTitle, TitleType.Text),
                items =
                persistentListOf(
                    numberedListStep1,
                    numberedListStep2,
                    numberedListStep3,
                    numberedListStep4,
                ),
            ),
        ),
        primaryButton =
        CentreAlignedScreenButton(
            text = stringResource(R.string.go_to_settings_button),
            onClick = {
                onGoToSettings()
            },
        ),
    )
}

@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun GoToSettingsPreview() {
    GdsTheme {
        GoToSettingsContent {}
    }
}
