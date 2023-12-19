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

2. For local development, ensure you have a `github.properties` in the project's root which includes your username and an access token
3. Add `implementation("uk.gov.android:authentication:_")` for latest version. Check packages for version information

## Package description

The Authentication package authenticates a users details and enables them to log into their account securely. This is done by providing them with a login session and token.

The package integrates [openID](https://openid.net/developers/how-connect-works/) AppAuth and conforms to its standards, documentation can be found here [AppAuth](https://github.com/openid/AppAuth-Android)

### Types

#### LoginSessionConfiguration

Handles creating the `config` found in `LoginSession`. It requires the following to be initialised:

```kotlin
val authorizeEndpoint: Uri
val clientId: String
val redirectUri: Uri
val scopes: String
val tokenEndpoint: Uri

// Default values
val locale: String = "en"
val prefersEphemeralWebSession: Boolean = true
val responseType: String = ResponseTypeValues.CODE
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

#### AppAuthSession

A class to handle the login flow with the given auth provider and conforms to the `LoginSession` protocol. 

`present` takes configuration, which comes from `LoginSessionConfiguration`, as a parameter and contains the login information to make the request. It will start an Activity for Result

`finalise` takes the `Intent` received from the Activity started by `present` and provides the `TokenResponse` via a callback

## Example Implementation

### How to use the Authentication package

Don't forget to call `init` with a context before use!

```kotlin
import uk.gov.android.authentication.LoginSession

...

val loginSession: LoginSession = AppAuthSession()
val configuration = LoginSessionConfiguration(
    authorizeEndpoint = uri,
    clietId = "clientId",
    redirectUri = uri,
    scopes = "scopes",
    tokenEdnpoint = uri
)

loginSession
    .init(context)
    .present(configuration)


```

Ensure the request code has been registered by the Activity to handle the ActivityResult and call `finalise`

```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == LoginSession.REQUEST_CODE_AUTH) {
        try {
            loginSession.finalise(intent) { tokens ->
                // Do what you like with the tokens!
                // ...
            }
        } catch (e: Error) {
            // handle error
        }
    }
}
```
