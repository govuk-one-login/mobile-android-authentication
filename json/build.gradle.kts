import uk.gov.android.authentication.extensions.setNamespace

plugins {
    alias(libs.plugins.kotlin.serlialization)
    id("uk.gov.android.authentication.android-lib-config")
}

android {
    setNamespace(".json")
}

ktlint {
    version = libs.versions.ktlint.cli.get()
}

dependencies {
    implementation(libs.androidx.core.core.ktx)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jose4j)
    implementation(libs.gson)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "Authentication JSON utilities",
        )
    }
}
