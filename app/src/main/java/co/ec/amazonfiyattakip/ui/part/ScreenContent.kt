package co.ec.amazonfiyattakip.ui.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen

data class ScreenOptions(
    val title: String = "",
    val fab: Pair<ImageVector, () -> Unit>? = null,
    val main: Boolean = false
)

@Composable
fun ScreenContent(
    optionsChanged: (res: ScreenOptions) -> Unit = { _ -> },
) {
    val navController = LocalNavigation.current
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen()
            optionsChanged(
                ScreenOptions(
                    title = "Amazon Fiyat Takibi",
                    fab = Pair(Icons.Filled.Add, {}),
                    main = true
                )
            )
        }
    }
}