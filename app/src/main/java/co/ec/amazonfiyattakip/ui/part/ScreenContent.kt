package co.ec.amazonfiyattakip.ui.part

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.screen.add.AddScreen
import co.ec.amazonfiyattakip.ui.screen.detail.DetailScreen
import co.ec.amazonfiyattakip.ui.screen.devtool.DevtoolsScreen
import co.ec.amazonfiyattakip.ui.screen.find.FindScreen
import co.ec.amazonfiyattakip.ui.screen.lowpriced.LowPricedScreen
import co.ec.amazonfiyattakip.ui.screen.main.MainScreen
import co.ec.amazonfiyattakip.ui.screen.settings.SettingsScreen
import co.ec.amazonfiyattakip.ui.screen.tracked.TrackedScreen

@Composable
fun ScreenContent(
    modifier: Modifier = Modifier,
    startDestination: String = "main"
) {
    //if contains slash start from main
    var destination = if (startDestination.contains("/")) "main" else startDestination
    val navController = LocalNavigation.current
    DisposableEffect(Unit) {
        if (startDestination != "main" && startDestination.contains("/")) {
            //not main and has slash in it
            navController.navigate(startDestination)
        }
        onDispose {
        }
    }
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = destination
    ) {
        composable("main") { MainScreen() }
        composable("add") { AddScreen() }
        composable("find") { FindScreen() }
        composable("lowpriced") { LowPricedScreen() }
        composable("settings") { SettingsScreen() }
        composable("joblog") { DevtoolsScreen() }
        composable("tracked") { TrackedScreen() }
        composable(
            "add/{asin}",
            arguments = listOf(navArgument("asin") { type = NavType.StringType })
        ) { backStackEntry ->
            AddScreen(asin = backStackEntry.arguments?.getString("asin"))
        }
        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            DetailScreen(productId = backStackEntry.arguments?.getInt("id")) // Pass the id to your DetailScreen
        }
    }

}