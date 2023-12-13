plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "uk.gov.android.authentication"
    compileSdk = 34

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    listOf(
        libs.appauth
    ).forEach(::api)

    listOf(
        libs.androidx.test.ext.junit,
        libs.espresso.core
    ).forEach(::androidTestImplementation)

    listOf(
        libs.androidx.core.core.ktx,
        libs.appauth,
        libs.appcompat,
        libs.material
    ).forEach(::implementation)

    listOf(
        libs.junit
    ).forEach(::testImplementation)
}
