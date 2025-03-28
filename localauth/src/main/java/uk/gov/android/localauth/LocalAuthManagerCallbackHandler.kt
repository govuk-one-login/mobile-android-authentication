package uk.gov.android.localauth

interface  LocalAuthManagerCallbackHandler {
    fun onSuccess(): Unit
    fun onFailure(): Unit
}
