package uk.gov.android.localauth

/**
 * This interface allows the consumer to pass in logic of how to handle the result when
 * using [LocalAuthManager.enforceAndSet].
 *
 * **There is no concrete implementation available as this is more specific to teh consumer.**
 */
interface LocalAuthManagerCallbackHandler {
    fun onSuccess(backButtonPressed: Boolean)

    fun onFailure(backButtonPressed: Boolean)
}
