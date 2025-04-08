package uk.gov.android.localauth

interface LocalAuthManagerCallbackHandler {
    fun onSuccess()

    fun onFailure()

    fun onBack()
}
