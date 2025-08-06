package co.ec.amazonfiyattakip.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.mock.MockSettings
import co.ec.amazonfiyattakip.ui.theme.AmazonFiyatTakipTheme
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.SettingsHelper

val LocalDB = compositionLocalOf<AppDatabase> { error("No DB provided") }
val LocalNavigation = compositionLocalOf<NavHostController> { error("No navcontroller provided") }
val LocalSnackbar = compositionLocalOf<SnackbarHostState> { error("No snackbarhost provided") }
val LocalSettings = compositionLocalOf<SettingsHelper> { error("No settings provided") }
val LocalCache = compositionLocalOf<CacheHelper> { error("No cache provided") }
val ExpertMode = compositionLocalOf<Boolean> { error("No cache provided") }

@Composable
fun AppProviders(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val db = AppDatabase.getDatabase()
    val snackbarHostState = SnackbarHostState()
    val settings = SettingsHelper(context)
    val cache = CacheHelper(context)
    val expertMode = settings.getBoolean("expertMode",false)
    CompositionLocalProvider(
        LocalNavigation provides navController,
        LocalDB provides db,
        LocalSnackbar provides snackbarHostState,
        LocalSettings provides settings,
        LocalCache provides cache,
        ExpertMode provides expertMode,
    ) {
        AmazonFiyatTakipTheme {
            content()
        }
    }
}

@Composable
fun PreviewProviders(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val settings = MockSettings(context)
    val navController = rememberNavController()
    val snackbarHostState = SnackbarHostState()
    CompositionLocalProvider(
        LocalSettings provides settings,
        LocalNavigation provides navController,
        LocalSnackbar provides snackbarHostState,
        ExpertMode provides settings.getBoolean("expertMode",true),
    ) {
        AmazonFiyatTakipTheme() {
            content()
        }
    }
}

data class SetIconColorEvent(var color: Color?){

}