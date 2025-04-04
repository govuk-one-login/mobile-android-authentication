package uk.gov.android.localauth.preference

interface LocalAuthPreferenceRepository {
    fun setLocalAuthPref(pref: LocalAuthPreference)

    fun getLocalAuthPref(): LocalAuthPreference?
}
