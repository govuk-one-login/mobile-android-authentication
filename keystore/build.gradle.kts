import uk.gov.android.authentication.extensions.setNamespace

plugins {
    id("uk.gov.android.authentication.android-lib-config")
}

android {
    setNamespace(".keystore")
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.bouncy.castle)

    androidTestImplementation(projects.json)
    androidTestImplementation(libs.kotlinx.serialization.json)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Authentication Keystore utilities",
        )
    }
}
