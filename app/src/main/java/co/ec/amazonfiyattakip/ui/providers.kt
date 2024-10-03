package co.ec.amazonfiyattakip.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.mock.MockSettings
import co.ec.amazonfiyattakip.ui.theme.AmazonFiyatTakipTheme
import co.ec.helper.AppSharedSettings

val LocalDB = compositionLocalOf<AppDatabase> { error("No DB provided") }
val LocalNavigation = compositionLocalOf<NavHostController> { error("No navcontroller provided") }
val LocalSnackbar = compositionLocalOf<SnackbarHostState> { error("No snackbarhost provided") }
val LocalSettings = compositionLocalOf<AppSharedSettings> { error("No settings provided") }

@Composable
fun AppProviders(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val db = AppDatabase.getDatabase()
    val snackbarHostState = SnackbarHostState()
    val settings = AppSharedSettings(context)
    CompositionLocalProvider(
        LocalNavigation provides navController,
        LocalDB provides db,
        LocalSnackbar provides snackbarHostState,
        LocalSettings provides settings
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
    CompositionLocalProvider(
        LocalSettings provides settings
    ) {
        AmazonFiyatTakipTheme {
            content()
        }
    }
}
