package uk.gov.android.localauth

data class LocalAuthManagerCallbackHandler(
    val onSuccess: () -> Unit,
    val onFailure: () -> Unit
)
