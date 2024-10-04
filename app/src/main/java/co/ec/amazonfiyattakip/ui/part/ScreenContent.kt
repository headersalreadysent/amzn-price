package co.ec.amazonfiyattakip.ui.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.screen.add.AddScreen
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen

data class ScreenOptions(
    val title: String = "",
    val fab: Pair<ImageVector, () -> Unit>? = null,
    val main: Boolean = false,
)


@Composable
fun ScreenContent(
    optionsChanged: (res: ScreenOptions) -> Unit = { _ -> },
    startDestination: String = "main"
) {
    val navController = LocalNavigation.current
    NavHost(navController = navController, startDestination = startDestination) {
        composable("main") {
            MainScreen()
            optionsChanged(
                ScreenOptions("Amazon Fiyat Takibi", Pair(Icons.Filled.Add) {
                    navController.navigate("add")
                }, true)
            )
        }
        composable("add") {
            AddScreen()
            optionsChanged(
                ScreenOptions("Takip Ekle", Pair(Icons.Filled.Add, {}), false)
            )
        }
    }
}