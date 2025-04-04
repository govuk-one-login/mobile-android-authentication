package uk.gov.android.localauth.utils

import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity

class TestActivity : FragmentActivity() {
    override fun onStart() {
        super.onStart()
        setContent { }
    }
}
