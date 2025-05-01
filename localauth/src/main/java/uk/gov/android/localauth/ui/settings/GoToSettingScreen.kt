package uk.gov.android.localauth.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.componentsv2.list.ListTitle
import uk.gov.android.ui.componentsv2.list.TitleType
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenBodyContent
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenButton
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.errorscreen.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@Composable
fun GoToSettingsScreen(
    analyticsLogger: AnalyticsLogger,
    onBack: () -> Unit,
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val analyticsViewModel = GoToSettingsAnalyticsViewModel(
        context,
        analyticsLogger,
    )
    analyticsViewModel.trackScreen()
    FullScreenDialogue(
        onDismissRequest = {
            analyticsViewModel.trackBackButton()
            onBack()
            onDismiss()
        },
        title = "",
        onBack = {
            analyticsViewModel.trackBackButton()
            onBack()
            onDismiss()
        },
        content = {
            GoToSettingsContent {
                analyticsViewModel.trackPrimaryButton()
                onGoToSettings()
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                context.startActivity(intent)
                onDismiss()
            }
        },
    )
}

@Composable
private fun GoToSettingsContent(
    onGoToSettings: () -> Unit,
) {
    val body1 = stringResource(R.string.app_localAuthManagerErrorBody1)
    val body2 = stringResource(R.string.app_localAuthManagerErrorBody2)
    val numberedListTitle = stringResource(R.string.app_localAuthManagerErrorBody3)
    val numberedListStep1 = R.string.app_localAuthManagerErrorNumberedList1
    val numberedListStep2 = stringResource(R.string.app_localAuthManagerErrorNumberedList2)
    val numberedListStep3 = stringResource(R.string.app_localAuthManagerErrorNumberedList3)
    ErrorScreen(
        icon = ErrorScreenIcon.ErrorIcon,
        title = stringResource(R.string.app_localAuthManagerErrorTitle),
        modifier = Modifier.fillMaxSize(),
        body =
        persistentListOf(
            CentreAlignedScreenBodyContent.Text(body1),
            CentreAlignedScreenBodyContent.Text(body2),
            CentreAlignedScreenBodyContent.NumberedList(
                title = ListTitle(numberedListTitle, TitleType.Text),
                items = persistentListOf(
                    ListItem(spannableText = numberedListStep1),
                    ListItem(numberedListStep2),
                    ListItem(numberedListStep3),
                ),
            ),
        ),
        primaryButton =
        CentreAlignedScreenButton(
            text = stringResource(R.string.app_localAuthManagerErrorGoToSettingsButton),
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
