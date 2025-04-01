package uk.gov.android.localauth.preference

interface LocalAuthPreferenceHandler {
    fun setBioPref(pref: LocalAuthPreference)

    fun getBioPref(): LocalAuthPreference
}
