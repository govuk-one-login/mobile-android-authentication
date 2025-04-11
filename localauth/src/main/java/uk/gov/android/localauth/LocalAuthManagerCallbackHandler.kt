package uk.gov.android.localauth

/**
 * This interface allows the consumer to pass in logic of how to handle the result when
 * using [LocalAuthManager.enforceAndSet].
 *
 * **There is no concrete implementation available as this is more specific to the consumer.**
 */
interface LocalAuthManagerCallbackHandler {
    /**
     * Method when the [LocalAuthManager.enforceAndSet] outcome is successful.
     *
     * @param backButtonPressed allows for the consumer to know if the back button has been pressed when landing on either screen managed by the [LocalAuthManager]
     */
    fun onSuccess(backButtonPressed: Boolean)

    /**
     * Method when the [LocalAuthManager.enforceAndSet] outcome has failed.
     *
     * @param backButtonPressed allows for the consumer to know if the back button has been pressed when landing on either screen managed by the [LocalAuthManager]
     */
    fun onFailure(backButtonPressed: Boolean)
}
