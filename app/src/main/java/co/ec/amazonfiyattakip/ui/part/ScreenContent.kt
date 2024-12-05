package co.ec.amazonfiyattakip.ui.part

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.screen.add.AddScreen
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen


@Composable
fun ScreenContent(
    startDestination: String = "main"
) {
    val navController = LocalNavigation.current
    NavHost(navController = navController, startDestination = startDestination) {
        composable("main") {
            MainScreen()
            AppModel.setFab(Icons.Filled.Add) {
                navController.navigate("add")
            }
        }
        composable("add") {
            AddScreen()
        }
    }
}