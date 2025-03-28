package uk.gov.android.localauth

sealed class LocalAuthPreference {
    data object Disabled : LocalAuthPreference()

    data class Enabled(
        val biometricsEnabled: Boolean,
    ) : LocalAuthPreference()
}
