package uk.gov.android.localauth.test

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.launch
import uk.gov.android.localauth.LocalAuthManager
import uk.gov.android.localauth.LocalAuthManagerCallbackHandler

@VisibleForTesting
@Composable
internal fun TestingScreen(
    localAuthRequired: Boolean,
    localAuthManager: LocalAuthManager,
    callbackHandler: LocalAuthManagerCallbackHandler,
) {
    val activity: FragmentActivity = LocalContext.current as FragmentActivity
    val coroutineScope = rememberCoroutineScope()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize(),
    ) {
        Button(onClick = {
            coroutineScope.launch {
                localAuthManager.enforceAndSet(
                    localAuthRequired,
                    activity,
                    callbackHandler,
                )
            }
        }) {
            Text(TEST_BUTTON)
        }
    }
}

const val TEST_BUTTON = "Set and enforce local auth"
