package uk.gov.android.authentication.extensions

import com.android.build.api.dsl.LibraryExtension

const val BASE_NAMESPACE = "uk.gov.android.authentication"

fun LibraryExtension.setNamespace(suffix: String) {
    namespace = "$BASE_NAMESPACE$suffix"
}
