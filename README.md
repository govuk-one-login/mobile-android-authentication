# mobile-android-authentication

Implementation of Authentication package

## Installation

To use Authentication in an Android Project:

1. Add the following to the settings.gradle.kts

```kotlin
dependencyResolutionManagement {
    ...
    
    repositories {
        
        ...
        
        maven("https://maven.pkg.github.com/govuk-one-login/mobile-android-authentication") {
            if (file("${rootProject.projectDir.path}/github.properties").exists()) {
                val propsFile = File("${rootProject.projectDir.path}/github.properties")
                val props = Properties().also { it.load(FileInputStream(propsFile)) }
                val ghUsername = props["ghUsername"] as String?
                val ghToken = props["ghToken"] as String?

                credentials {
                    username = ghUsername
                    password = ghToken
                }
            } else {
                credentials {
                    username = System.getenv("USERNAME")
                    password = System.getenv("TOKEN")
                }
            }
        }
    }
}
```

2. For local development, ensure you have a `github.properties` in the project's root which includes your username and an access token. **Do not commit this file to Version Control**
3. Add `implementation("uk.gov.android:authentication:_")` for latest version. Check packages for version information

## Package description

The Authentication package comprises three sub-packages:
1. login - authenticates a users details and enables them to log into their account securely. This is done by providing them with a login session and token.
2. integrity - checks the app integrity and provides a ClientAttestation and ProofOfPossession that will be used for retrieving an authentication token response.
3. jwt - provides ability to verify a JWT with a public key adhering to the JWK specifications.

The package integrates [openID](https://openid.net/developers/how-connect-works/) AppAuth and conforms to its standards, documentation can be found here [AppAuth](https://github.com/openid/AppAuth-Android)

## Login
### Types

#### LoginSessionConfiguration

Handles creating the `config` found in `LoginSession`. It requires the following to be initialised:

```kotlin
val authorizeEndpoint: Uri
val clientId: String
val redirectUri: Uri
val scopes: List<Scope>
val tokenEndpoint: Uri

// Default values
val locale: Locale = Locale.EN
val prefersEphemeralWebSession: Boolean = true
val responseType: ResponseType = ResponseType.CODE
val vectorsOfTrust: String = "[\"Cl.Cm.P0\"]"
```

#### TokenResponse

Holds the returned token values

```kotlin
val tokenType: String
val accessToken: String
val accessTokenExpirationTime: Long
val idToken: String
val refreshToken: String?
val scope: String
```

#### AuthenticationError

Custom error extending [Error](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/error.html)

```kotlin
val message: String
val type: ErrorType
```

#### AppAuthSession

A class to handle the login flow with the given auth provider and conforms to the `LoginSession` protocol. 

`present` takes configuration, which comes from `LoginSessionConfiguration`, as a parameter and contains the login information to make the request. It will start an Activity for Result

`finalise` takes the `Intent` received from the Activity started by `present` and provides the `TokenResponse` via a callback

## Example Implementation

### How to use the Authentication package

```kotlin
import uk.gov.android.authentication.LoginSession

...

val loginSession: LoginSession = AppAuthSession(context)
val configuration = LoginSessionConfiguration(
    authorizeEndpoint = uri,
    clietId = "clientId",
    redirectUri = uri,
    scopes = "scopes",
    tokenEdnpoint = uri
)

loginSession.present(configuration)


```

Ensure the request code has been registered by the Activity to handle the ActivityResult and call `finalise`

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == LoginSession.REQUEST_CODE_AUTH) {
        try {
            loginSession.finalise(intent, appIntegrityParameters) { tokens ->
                // Do what you like with the tokens!
                // ...
            }
        } catch (e: Error) {
            // handle error
        }
    }
}
```

## Integrity

### JWK.JsonWebKey

Creates a JWK that adheres to the backend format requirements:

```json
{ 
  "jwk": {
    "kty": "EC",
    "use": "sig",
    "crv": "P-256",
    "x": "18wHLeIgW9wVN6VD1Txgpqy2LszYkMf6J8njVAibvhM",
    "y": "-V4dS4UaLMgP_4fY4j8ir7cl1TXlFdAgcx55o7TkcSA"
  }
}
```

This will be used to verify the PoP when authenticating.

```kotlin
val jwk = JWK.makeJWK(
    x = "<ECPoint_x_inBase64UrlEncoded>",
    y = "<ECPoint_y_inBase64UrlEncoded>"
)
```

### AppChecker and AppCheckToken

The AppChecker is an interface that allows for a custom implementation on client side to provide a AppCheckToken.
The AppCheckToken is a wrapper that allows for the AppCheckerInterface to be used with different implementations. It contains a JWT provided by a backend service.

**Implementation - this is a Firebase specific implementation**
```kotlin
class AppCheckImpl @Inject constructor(
    appCheckFactory: AppCheckProviderFactory,
    context: Context
) : AppChecker {
    private val appCheck = Firebase.appCheck

    init {
        Firebase.appCheck.installAppCheckProviderFactory(
            appCheckFactory
        )
        Firebase.initialize(context)
    }

    override suspend fun getAppCheckToken(): Result<AppCheckToken> {
        return try {
            Result.success(
                AppCheckToken(appCheck.limitedUseAppCheckToken.await().token)
            )
        } catch (e: FirebaseException) {
            Result.failure(e)
        }
    }
}
```

### AttestationCaller and AttestationResponse

The AttestationCaller is an interface that allows for custom implementation on checking the token provided from the AppChecker and returning an AttestationResponse.
The AttestationResponse provides an attestation in JWT format and an expiry time. These will be use to confirm if a new AppCheck is required when logging in and in the process of obtaining access tokens.

```kotlin
class AttestationCallerImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val httpClient: GenericHttpClient 
) : AttestationCaller {
    override suspend fun call(
        token: String,
        jwk: JWK.JsonWebKey
    ): AttestationResponse {
        // The token and any additional parameters can be amended accordingly and provided where needed, this is just an example of a possible implementation
        val request = ApiRequest.Post(
            url = "https://attestation-url.co.uk/endpoint",
            body = jwk,
            headers = listOf(
                "appCheckToken" to token,
                AttestationCaller.CONTENT_TYPE to AttestationCaller.CONTENT_TYPE_VALUE
            )
        )
        return when (val apiResponse = httpClient.makeRequest(request)) {
            is ApiResponse.Success<*> -> { 
                // Handle successful attestation response 
            }
            is ApiResponse.Failure -> AttestationResponse.Failure(
                apiResponse.error.message ?: NETWORK_ERROR,
                apiResponse.error
            )
            // e.g. Offline
            else -> AttestationResponse.Failure(NETWORK_ERROR)
        }
    }
    
    companion object {
        const val NETWORK_ERROR = "Network error"
    }
}
```

### ProofOfPossessionGenerator and SignedPoP

The ProofOfPossessionGenerator object creates a Proof of Possession (PoP) that will be used in the authentication call, as part of a header. It adheres to the following scheme and it is a signed JWT contained within the SignedPoP. This will be used and verified by the backend to ensure the app is genuine.
It adheres to the following requirements:
_Header_
```json
{
  "alg": "ES256"
}
```
_Body_
```json
{
  "iss": "<OAuth client ID",
  "aud": "https://token.account.gov.uk",
  "exp": 1234567890,
  "jti": "d3e8e382-4691-4b1f-83c1-4454f75bd930"
}
```

**Implementation**

```kotlin
val pop = ProofOfPossessionGenerator.createBase64PoP(iss, aud)
```

### AppIntegrityManager and AppIntegrityConfiguration

The AppIntegrityManager combines the structures explained above and creates a provides the functionality of these into a service that retrieves a ClientAttestation and creates a Proof of Possession.
The AppIntegrityConfiguration provides the AttestationCaller, AppChecker, and KeyStoreManager specific implementation to be provided to the AppIntegrityManager.

An example of the AppIntegrityManager and a possible implementation is available in the [FirebaseAppIntegrityManager](app/src/main/java/uk/gov/android/authentication/integrity/FirebaseAppIntegrityManager.kt)
