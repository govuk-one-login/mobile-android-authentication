package uk.gov.android.localauth.ui.settings

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.button.ButtonTypeV2
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.images.GdsIcon
import uk.gov.android.ui.componentsv2.list.GdsNumberedList
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.componentsv2.list.ListTitle
import uk.gov.android.ui.componentsv2.list.TitleType
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.android.ui.patterns.R as patternsR

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
    val body = persistentListOf(
        stringResource(R.string.app_localAuthManagerErrorBody1),
        stringResource(R.string.app_localAuthManagerErrorBody2),
    )
    val numberedListTitle = stringResource(R.string.app_localAuthManagerErrorBody3)
    val numberedListStep1 = stringResource(R.string.app_localAuthManagerErrorNumberedList1)
    val numberedListStep2 = stringResource(R.string.app_localAuthManagerErrorNumberedList2)
    val numberedListStep3 = stringResource(R.string.app_localAuthManagerErrorNumberedList3)
    ErrorScreen(
        icon = {
            GdsIcon(
                image = ImageVector.vectorResource(patternsR.drawable.ic_warning_error),
                contentDescription = stringResource(patternsR.string.error_icon_description),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        title = { horizontalPadding ->
            GdsHeading(
                text = stringResource(R.string.app_localAuthManagerErrorTitle),
                modifier = Modifier.padding(horizontal = horizontalPadding),
            )
        },
        modifier = Modifier.fillMaxSize(),
        body = { horizontalPadding ->
            items(body.size) { index ->
                Text(
                    text = body[index],
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item {
                GdsNumberedList(
                    numberedListItems = persistentListOf(
                        ListItem(numberedListStep1),
                        ListItem(numberedListStep2),
                        ListItem(numberedListStep3),
                    ),
                    title = ListTitle(numberedListTitle, TitleType.Heading),
                    modifier = Modifier.padding(horizontal = horizontalPadding),
                )
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.app_localAuthManagerErrorGoToSettingsButton),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onGoToSettings,
                modifier = Modifier.fillMaxWidth(),
            )
        },
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
