package uk.gov.android.localauth.ui.optout

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
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
import uk.gov.android.ui.componentsv2.topappbar.GdsTopAppBar
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.errorscreen.v2.ErrorScreen
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.logging.api.analytics.logging.AnalyticsLogger
import uk.gov.android.ui.patterns.R as patternsR

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
        topAppBar = {
            GdsTopAppBar(
                onClick = {
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
    val body = persistentListOf(
        stringResource(R.string.app_optOutBiometricsBody1),
        stringResource(R.string.app_optOutBiometricsBody2),
        stringResource(R.string.app_optOutBiometricsBody3),
    )
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
                text = stringResource(R.string.app_optOutBiometricsTitle),
                modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),

            )
        },
        body = { horizontalPadding ->
            items(body.size) { index ->
                Text(
                    text = body[index],
                    modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        primaryButton = {
            GdsButton(
                text = stringResource(R.string.app_optOutBiometricsButton),
                buttonType = ButtonTypeV2.Primary(),
                onClick = onBiometricsOptIn,
                modifier = Modifier.fillMaxWidth(),
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BioOptOutPreview() {
    GdsTheme {
        FullScreenDialogue(
            topAppBar = {
                GdsTopAppBar(onClick = { /* Nothing to do */ })
            },
            onBack = {},
            content = {
                BioOptOutContent(onBiometricsOptIn = {})
            },
        )
    }
}
