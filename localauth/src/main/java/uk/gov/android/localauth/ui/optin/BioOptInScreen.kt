package uk.gov.android.localauth.ui.optin

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.button.ButtonType
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.heading.GdsHeadingStyle
import uk.gov.android.ui.componentsv2.list.GdsBulletedList
import uk.gov.android.ui.componentsv2.list.ListItem
import uk.gov.android.ui.componentsv2.list.ListTitle
import uk.gov.android.ui.componentsv2.list.TitleType
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.dialog.FullScreenDialogueTopAppBar
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.android.ui.theme.smallPadding
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BioOptInScreen(
    analyticsLogger: AnalyticsLogger,
    walletEnabled: Boolean,
    onBack: () -> Unit,
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    val analyticsViewModel = BioOptInAnalyticsViewModel(LocalContext.current, analyticsLogger)
    if (walletEnabled) {
        analyticsViewModel.trackBioOptInWalletScreen()
    } else {
        analyticsViewModel.trackBioOptInNoWalletScreen()
    }
    FullScreenDialogue(
        onDismissRequest = onDismiss,
        topAppBar = {
            FullScreenDialogueTopAppBar(
                modifier = Modifier.semantics(true) {
                    this.traversalIndex = CLOSE_INDEX
                },
                onCloseClick = {
                    analyticsViewModel.trackCloseIconButton()
                    onBiometricsOptOut()
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
            BioOptInContent(
                walletEnabled = walletEnabled,
                onBiometricsOptIn = {
                    onBiometricsOptIn()
                    analyticsViewModel.trackBiometricsButton()
                    onDismiss()
                },
                onBiometricsOptOut = {
                    onBiometricsOptOut()
                    analyticsViewModel.trackPasscodeButton()
                    onDismiss()
                },
            )
        },
    )
}

@Composable
private fun BioOptInContent(
    walletEnabled: Boolean,
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
        Modifier
            .padding(smallPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier =
            Modifier
                .fillMaxHeight()
                .weight(1f),
        ) {
            if (walletEnabled) {
                WalletCopyText()
            } else {
                NoWalletCopyText()
            }
        }
        BioOptInButtons(onBiometricsOptIn, onBiometricsOptOut)
    }
}

@OptIn(UnstableDesignSystemAPI::class)
@Composable
private fun WalletCopyText() {
    val title = stringResource(R.string.app_wallet_enableBiometricsBody1)
    val bulletItemOne = stringResource(R.string.app_wallet_enableBiometricsBullet1)
    val bulletItemTwo = stringResource(R.string.app_wallet_enableBiometricsBullet2)
    Header()
    GdsBulletedList(
        title = ListTitle(text = title, titleType = TitleType.Text),
        bulletListItems = persistentListOf(
            ListItem(bulletItemOne),
            ListItem(bulletItemTwo),
        ),
        modifier = Modifier.padding(bottom = smallPadding),
        accessibilityIndex = LIST_INDEX,
    )
    CustomText(text = stringResource(R.string.app_enableBiometricsBody2), CONTENT1_INDEX)
    CustomText(text = stringResource(R.string.app_enableBiometricsBody3), CONTENT2_INDEX)
}

@OptIn(UnstableDesignSystemAPI::class)
@Composable
private fun NoWalletCopyText() {
    Header()
    CustomText(text = stringResource(R.string.app_enableBiometricsBody1), LIST_INDEX)
    CustomText(text = stringResource(R.string.app_enableBiometricsBody2), CONTENT1_INDEX)
    CustomText(text = stringResource(R.string.app_enableBiometricsBody3), CONTENT2_INDEX)
}

@Composable
@OptIn(UnstableDesignSystemAPI::class)
private fun Header() {
    Image(
        imageVector = ImageVector.vectorResource(R.drawable.bio_opt_in),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        modifier = Modifier
            .padding(vertical = smallPadding)
            .fillMaxWidth()
            .testTag(stringResource(R.string.app_enableBiometricsImageTestTag)),
    )
    GdsHeading(
        text = stringResource(R.string.app_enableBiometricsTitle),
        style = GdsHeadingStyle.Title1,
        modifier = Modifier
            .padding(bottom = smallPadding)
            .semantics { this.traversalIndex = TITLE_INDEX },
    )
}

@Composable
private fun BioOptInButtons(
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.padding(bottom = smallPadding),
    ) {
        GdsButton(
            text = stringResource(R.string.app_enableBiometricsButton),
            buttonType = ButtonType.Primary,
            onClick = onBiometricsOptIn,
            modifier = Modifier.fillMaxWidth(),
        )
        GdsButton(
            text = stringResource(R.string.app_enablePasscodeOrPatternButton),
            buttonType = ButtonType.Quaternary,
            onClick = onBiometricsOptOut,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CustomText(text: String, accessibilityIndex: Float) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = smallPadding)
            .fillMaxWidth()
            .semantics { this.traversalIndex = accessibilityIndex },
        textAlign = TextAlign.Center,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BioOptInPreviewWallet() {
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
                BioOptInContent(true, {}, {})
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BioOptInPreview() {
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
                BioOptInContent(false, {}, {})
            },
        )
    }
}

private const val CLOSE_INDEX = -15f
private const val TITLE_INDEX = -14f
private const val LIST_INDEX = -12f
private const val CONTENT1_INDEX = -11f
private const val CONTENT2_INDEX = -10f
