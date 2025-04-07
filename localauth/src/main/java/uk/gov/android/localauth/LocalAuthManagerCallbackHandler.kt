package uk.gov.android.localauth

abstract class LocalAuthManagerCallbackHandler {
    abstract fun onSuccess(): Unit

    abstract fun onFailure(): Unit
}
