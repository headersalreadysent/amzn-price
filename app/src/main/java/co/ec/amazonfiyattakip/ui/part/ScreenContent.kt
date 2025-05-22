package co.ec.amazonfiyattakip.ui.part

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.joblog.JobLogScreen
import co.ec.amazonfiyattakip.ui.screen.add.AddScreen
import co.ec.amazonfiyattakip.ui.screen.detail.DetailScreen
import co.ec.amazonfiyattakip.ui.screen.find.FindScreen
import co.ec.amazonfiyattakip.ui.screen.lowpriced.LowPricedScreen
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
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainScreen()
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
        composable("lowpriced") {
            LowPricedScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("joblog") {
            JobLogScreen()
        }
    }
    DisposableEffect(Unit) {
        if (startDestination != "main") {
            navController.navigate(startDestination)
        }
        onDispose {

        }
    }
}