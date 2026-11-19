import uk.gov.android.authentication.extensions.setNamespace

plugins {
    id("uk.gov.android.authentication.android-lib-config")
}

android {
    setNamespace(".keystore")
}

dependencies {
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.bouncy.castle)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Authentication Keystore utilities",
        )
    }
}
