package uk.gov.android.localauth.ui

import android.content.res.Configuration
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import kotlinx.collections.immutable.persistentListOf
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.bulletedlist.BulletedListTitle
import uk.gov.android.ui.componentsv2.bulletedlist.TitleType
import uk.gov.android.ui.componentsv2.button.ButtonType
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.images.GdsVectorImage
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenBodyContent
import uk.gov.android.ui.patterns.centrealignedscreen.CentreAlignedScreenButton
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.patterns.errorscreen.ErrorScreen
import uk.gov.android.ui.patterns.errorscreen.ErrorScreenIcon
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.android.ui.theme.smallPadding
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI

class DialogManager {
    fun displayBioOptIn(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onBiometricsOptIn: () -> Unit,
        onBiometricsOptOut: () -> Unit,
    ) {
        val dialogView =
            ComposeView(activity).apply {
                setContent {
                    BackHandler {
                        onBack()
                    }
                    FullScreenDialogue(
                        onDismissRequest = { },
                        topAppBar = {
                            // Not needed
                        },
                        content = {
                            BioOptInContent(onBiometricsOptIn, onBiometricsOptOut)
                        },
                    )
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

    fun displayGoToSettingsPage(
        activity: FragmentActivity,
        onBack: () -> Unit,
        onGoToSettings: () -> Unit,
    ) {
        val dialogView =
            ComposeView(activity).apply {
                setContent {
                    BackHandler {
                        onBack()
                    }
                    FullScreenDialogue(
                        onDismissRequest = { },
                        content = {
                            GoToSettingsContent { onGoToSettings() }
                        },
                    )
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

@Composable
private fun BioOptInContent(
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
            BioOptInText()
        }
        BioOptInButtons(onBiometricsOptIn, onBiometricsOptOut)
    }
}

@Composable
private fun GoToSettingsContent(onGoToSettings: () -> Unit) {
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
            onClick = onGoToSettings,
        ),
    )
}

@OptIn(UnstableDesignSystemAPI::class)
@Composable
private fun BioOptInText() {
    GdsVectorImage(
        image = ImageVector.vectorResource(R.drawable.bio_opt_in),
        contentDescription = stringResource(R.string.bio_opt_in_image_content_description),
        color = MaterialTheme.colorScheme.onBackground,
        scale = ContentScale.Fit,
        modifier = Modifier.padding(vertical = smallPadding),
    )
    GdsHeading(
        text = stringResource(R.string.bio_opt_in_image_title),
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = smallPadding),
    )
    CustomText(text = stringResource(R.string.bio_opt_in_image_body1))
    CustomText(text = stringResource(R.string.bio_opt_in_image_body2))
    CustomText(text = stringResource(R.string.bio_opt_in_image_body3))
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
            text = stringResource(R.string.bio_opt_in_image_bio_button),
            buttonType = ButtonType.Primary,
            onClick = onBiometricsOptIn,
            modifier = Modifier.fillMaxWidth(),
        )
        GdsButton(
            text = stringResource(R.string.bio_opt_in_image_passcode_button),
            buttonType = ButtonType.Quaternary,
            onClick = onBiometricsOptOut,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CustomText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(bottom = smallPadding),
    )
}

@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BioOptInPreview() {
    GdsTheme {
        BioOptInContent({}, {})
    }
}

@ScreenPreview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun GoToSettingsPreview() {
    GdsTheme {
        GoToSettingsContent {}
    }
}
