package uk.gov.android.localauth

import kotlin.test.assertEquals
import org.junit.jupiter.api.Test

// Remove this test after Impl is added on this https://govukverify.atlassian.net/browse/DCMAW-12057
class LocalAuthPreferenceTest {
    @Test
    fun testDisabled() {
        val expected = LocalAuthPreference.Disabled
        assertEquals(LocalAuthPreference.Disabled, expected)
    }

    @Test
    fun testEnabledBiometrics() {
        val expected = LocalAuthPreference.Enabled(true)
        assertEquals(LocalAuthPreference.Enabled(true), expected)
    }

    @Test
    fun testEnabledNoBiometrics() {
        val expected = LocalAuthPreference.Enabled(false)
        assertEquals(LocalAuthPreference.Enabled(false), expected)
    }
}
