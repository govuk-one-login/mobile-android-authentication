package uk.gov.android.localauth

import kotlin.test.assertTrue
import org.junit.jupiter.api.Test

// Remove this test after Impl is added on this https://govukverify.atlassian.net/browse/DCMAW-12057
class LocalAuthManagerCallbackHandlerTest {
    private var success = false
    private var failure = false
    private val handler = LocalAuthManagerCallbackHandler(
        onSuccess = { success = true },
        onFailure = { failure = true }
    )

    @Test
    fun testSuccess() {
        handler.onSuccess()
        assertTrue(success)
    }

    @Test
    fun testFailure() {
        handler.onFailure()
        assertTrue(failure)
    }
}
