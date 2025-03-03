package co.ec.amazonfiyattakip.ui.part

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.joblog.JobLogScreen
import co.ec.amazonfiyattakip.ui.screen.add.AddScreen
import co.ec.amazonfiyattakip.ui.screen.detail.DetailScreen
import co.ec.amazonfiyattakip.ui.screen.find.FindScreen
import co.ec.amazonfiyattakip.ui.screen.list.ListScreen
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen
import co.ec.amazonfiyattakip.ui.screen.settings.SettingsScreen


@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    startDestination: String = "main"
) {
    val navController = LocalNavigation.current
    NavHost(
        modifier = modifier,
        navController = navController, startDestination = startDestination
    ) {
        composable("main") {
            MainScreen()
            AppModel.setFab(Icons.Filled.Search) {
                navController.navigate("find")
            }
        }
        composable("add") {
            AddScreen()
        }
        composable(
            "add/{asin}",
            arguments = listOf(navArgument("asin") { type = NavType.StringType })
        ) { backStackEntry ->
            val asin = backStackEntry.arguments?.getString("asin")
            AddScreen(asin = asin)
        }
        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            DetailScreen(id) // Pass the id to your DetailScreen
        }
        composable("find") {
            FindScreen()
        }
        composable("list") {
            ListScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("joblog") {
            JobLogScreen()
        }
    }
}