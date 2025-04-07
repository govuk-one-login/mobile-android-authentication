package uk.gov.android.localauth.ui

import android.content.res.Configuration
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import uk.gov.android.authentication.localauth.R
import uk.gov.android.ui.componentsv2.button.ButtonType
import uk.gov.android.ui.componentsv2.button.GdsButton
import uk.gov.android.ui.componentsv2.heading.GdsHeading
import uk.gov.android.ui.componentsv2.images.GdsVectorImage
import uk.gov.android.ui.patterns.dialog.FullScreenDialogue
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.meta.ScreenPreview
import uk.gov.android.ui.theme.smallPadding
import uk.gov.android.ui.theme.util.UnstableDesignSystemAPI

@Composable
fun BioOptInScreen(
    onBack: () -> Unit,
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
    onDismiss: () -> Unit,
) {
    BackHandler {
        onBack()
        onDismiss()
    }
    FullScreenDialogue(
        onDismissRequest = onDismiss,
        topAppBar = {
            // Not needed
        },
        content = {
            BioOptInContent(onBiometricsOptIn, onBiometricsOptOut, onDismiss)
        },
    )
}

@Composable
private fun BioOptInContent(
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
    onDismissRequest: () -> Unit,
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
        BioOptInButtons(onBiometricsOptIn, onBiometricsOptOut, onDismissRequest)
    }
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
        text = stringResource(R.string.bio_opt_in_title),
        style = MaterialTheme.typography.displaySmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = smallPadding),
    )
    CustomText(text = stringResource(R.string.bio_opt_in_body1))
    CustomText(text = stringResource(R.string.bio_opt_in_body2))
    CustomText(text = stringResource(R.string.bio_opt_in_body3))
}

@Composable
private fun BioOptInButtons(
    onBiometricsOptIn: () -> Unit,
    onBiometricsOptOut: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.padding(bottom = smallPadding),
    ) {
        GdsButton(
            text = stringResource(R.string.bio_opt_in_bio_button),
            buttonType = ButtonType.Primary,
            onClick = {
                onBiometricsOptIn()
                onDismissRequest()
            },
            modifier = Modifier.fillMaxWidth(),
        )
        GdsButton(
            text = stringResource(R.string.bio_opt_in_passcode_button),
            buttonType = ButtonType.Quaternary,
            onClick = {
                onBiometricsOptOut()
                onDismissRequest()
            },
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
        BioOptInContent({}, {}, {})
    }
}
