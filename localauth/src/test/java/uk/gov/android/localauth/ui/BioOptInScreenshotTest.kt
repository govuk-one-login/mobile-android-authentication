package uk.gov.android.localauth.ui

import androidx.compose.runtime.Composable
import com.android.resources.NightMode
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import uk.gov.android.localauth.utils.BaseScreenshotTest
import kotlin.test.Ignore

@Ignore
@RunWith(Parameterized::class)
class BioOptInScreenshotTest(
    private val parameters: Pair<() -> Unit, NightMode>,
) : BaseScreenshotTest(parameters.second) {

    override val generateComposeLayout: @Composable () -> Unit = {
        val parameters = parameters.first
        BioOptInScreen(parameters, parameters, parameters, parameters)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index} BioOptInScreen")
        fun values(): List<Pair<() -> Unit, NightMode>> {
            val result: MutableList<Pair<() -> Unit, NightMode>> = mutableListOf()

            listOf({}).forEach(applyNightMode(result))

            return result
        }
    }
}
