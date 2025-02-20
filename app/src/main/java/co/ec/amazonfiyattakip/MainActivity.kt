package co.ec.amazonfiyattakip

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.ui.AppProviders
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ScreenContent
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.analytics.FirebaseAnalytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val destination = intent?.getStringExtra("destination") ?: "main"
        setContent {
            AppProviders {
                AppContent(
                    startDestination = destination
                )
            }
        }
    }

}

@Composable
fun AppContent(
    startDestination: String = "main", appModel: AppModel = viewModel()
) {
    val uiController = rememberSystemUiController()

    val surface = MaterialTheme.colorScheme.secondaryContainer
    SideEffect {
        uiController.setNavigationBarColor(
            color = surface,
            darkIcons = ColorUtils.calculateLuminance(surface.toArgb()) > 0.5
        )
        uiController.setStatusBarColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = ColorUtils.calculateLuminance(surface.toArgb()) > 0.5
        )
    }
    val coroutineScope = rememberCoroutineScope()
    App.setupSnackbar(LocalSnackbar.current, coroutineScope)
    val fabAction by appModel.fabAction.observeAsState()
    val navigator = LocalNavigation.current
    navigator.addOnDestinationChangedListener { _, destination, _ ->
        val screenName = destination.label?.toString() ?: destination.route
        screenName?.let {
            App.event(
                FirebaseAnalytics.Event.SCREEN_VIEW, mapOf(
                    FirebaseAnalytics.Param.SCREEN_NAME to screenName
                )
            )
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            val snackbarHostState = LocalSnackbar.current
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = {
                        navigator.navigate("main")
                    }) {
                        Icon(Icons.Default.Home, contentDescription = "Menu")
                    }
                    IconButton(onClick = {
                        navigator.navigate("settings")
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    Spacer(Modifier.weight(1f, true))
                },
                modifier = Modifier.fillMaxWidth()
                    .background(surface),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                floatingActionButton = {
                    fabAction?.let {
                        FloatingActionButton(
                            onClick = it.second,
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                            Icon(it.first, contentDescription = "")
                        }
                    }
                },
            )
        }


    ) { screen ->
        //make screen finish just below bottombar
        Box(
            modifier = Modifier.padding(
                bottom = (screen.calculateBottomPadding().value - 5).dp
            )
        ) {
            ScreenContent(startDestination = startDestination)
        }

    }


}


@Preview(showBackground = true)
@Composable
fun AppPreview() {
    PreviewProviders {
        AppContent()
    }
}