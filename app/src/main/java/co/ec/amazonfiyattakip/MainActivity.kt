package co.ec.amazonfiyattakip

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.amazonfiyattakip.ui.AppProviders
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.BottomCardContent
import co.ec.amazonfiyattakip.ui.part.ScreenContent
import co.ec.helper.helpers.EventBus
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import co.ec.amazonfiyattakip.helper.isLight
import co.ec.amazonfiyattakip.ui.SetIconColorEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transparent = Color.Transparent.toArgb()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(transparent, transparent),
            navigationBarStyle = SystemBarStyle.light(transparent, transparent)
        )
        val destination = intent?.getStringExtra("destination") ?: "main"
        setContent {
            AppProviders {
                val darkTheme = !MaterialTheme.colorScheme.secondaryContainer.isLight()
                val surfaceIsDark = !MaterialTheme.colorScheme.surfaceContainer.isLight()
                val view = LocalView.current
                val window = (view.context as? ComponentActivity)?.window
                val scope = rememberCoroutineScope()
                LaunchedEffect(Unit) {
                    window?.let {
                        WindowCompat.getInsetsController(
                            window,
                            view
                        ).isAppearanceLightNavigationBars = !darkTheme
                    }
                    scope.launch {
                        EventBus.subscribe<SetIconColorEvent> { event ->
                            //change color of icons by value or by surface
                            var iconAppearance =
                                if (event.color == null) !surfaceIsDark else event.color!!.isLight()
                            window?.let {
                                WindowCompat.getInsetsController(
                                    window,
                                    view
                                ).isAppearanceLightStatusBars = iconAppearance
                            }
                        }
                    }

                }
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

    val surface = MaterialTheme.colorScheme.surfaceContainer

    val coroutineScope = rememberCoroutineScope()
    App.setupSnackbar(LocalSnackbar.current)

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
    var settingsClick by remember { mutableIntStateOf(0) }
    val settings = LocalSettings.current
    var developerActive by remember {
        mutableStateOf(
            settings.getBoolean(
                "developerActive",
                false
            )
        )
    }
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    LaunchedEffect(Unit) {
        App.settings().putInt("primaryColor", primaryColor)
        EventBus.subscribe<SettingsHelper.SettingsChange> {
            if (it.name == "developerActive") {
                developerActive = it.value as Boolean
            }
        }
    }
    LaunchedEffect(Unit) {
        while (true) {
            settingsClick = 0
            delay(2000)
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
                        navigator.navigate("lowpriced")
                    }) {
                        Icon(Icons.Default.Insights, contentDescription = "Stat")
                    }
                    IconButton(onClick = {
                        settingsClick++
                        if (settingsClick == 5) {
                            settings.putBoolean("developerActive", true)
                        }
                        if (navigator.currentDestination?.route !== "settings") {
                            navigator.navigate("settings")
                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    if (developerActive) {
                        IconButton(onClick = {
                            navigator.navigate("joblog")
                        }) {
                            Icon(Icons.Default.DeviceThermostat, contentDescription = "Menu")
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    App.snack("Fiyatlar Güncelleniyor")
                                    PriceUpdate.run(true)
                                }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Settings")
                        }
                    }
                    Spacer(Modifier.weight(1f, true))
                },
                modifier = Modifier
                    .fillMaxWidth()
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
        var cutCardHeight by remember { mutableIntStateOf(0) }
        var screenHeight by remember { mutableIntStateOf(0) }
        var size by remember { mutableStateOf(Size.Unspecified) }
        val density = LocalDensity.current
        with(density) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = (screen.calculateBottomPadding().value - 5).dp
                    )
                    .onSizeChanged {
                        size = it.toSize()
                        screenHeight = it.height
                    }
            ) {

                val view = LocalView.current
                ScreenContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((screenHeight - cutCardHeight + 30.dp.toPx()).toDp()),
                    startDestination = startDestination
                )
                val content by appModel.cutCardContent.observeAsState(null)
                BottomCardContent(content, {
                    cutCardHeight = it
                })
            }

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