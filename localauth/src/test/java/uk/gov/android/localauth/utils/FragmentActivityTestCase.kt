package uk.gov.android.localauth.utils

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import org.junit.Rule

abstract class FragmentActivityTestCase(setContent: Boolean) {
    @get:Rule(order = 1)
    val composeTestRule = if (setContent) {
        createAndroidComposeRule<TestActivity>()
    } else {
        createAndroidComposeRule<TestActivityNoContent>()
    }

    protected val context: Context = ApplicationProvider.getApplicationContext()
}
