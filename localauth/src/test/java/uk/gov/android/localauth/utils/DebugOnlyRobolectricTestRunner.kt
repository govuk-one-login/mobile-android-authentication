package uk.gov.android.localauth.utils

import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import uk.gov.logging.api.BuildConfig

/**
 * A custom RobolectricTestRunner to run tests only in debug builds.
 *
 * This wrapper is necessary because `androidx.compose.ui:ui-test-manifest` is available only
 * in debug builds. Running Robolectric tests in non-debug configurations could lead to missing
 * dependencies and test failures.
 */
class DebugOnlyRobolectricTestRunner(
    testClass: Class<*>,
) : RobolectricTestRunner(testClass) {
    override fun isIgnored(child: FrameworkMethod?): Boolean = !BuildConfig.DEBUG || super.isIgnored(child)
}
