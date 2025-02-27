package co.ec.amazonfiyattakip

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.helper.topOuterShadow
import co.ec.amazonfiyattakip.ui.AppProviders
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ScreenContent
import co.ec.helper.helpers.LogHelper
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.delay

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

    val container = MaterialTheme.colorScheme.secondaryContainer
    val surface = MaterialTheme.colorScheme.surfaceContainer
    SideEffect {
        uiController.setNavigationBarColor(
            color = container,
            darkIcons = ColorUtils.calculateLuminance(container.toArgb()) > 0.5
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
    var settingsClick by remember { mutableIntStateOf(0) }
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

                        settingsClick++
                        LogHelper.d("settingsClick $settingsClick")
                        if (settingsClick == 5) {
                            settingsClick = 0
                            navigator.navigate("joblog")
                        } else {
                            if (navigator.currentDestination?.route !== "settings") {
                                navigator.navigate("settings")
                            }

                        }
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
        Box(
            modifier = Modifier.padding(
                bottom = (screen.calculateBottomPadding().value - 5).dp
            )
        ) {
            ScreenContent(startDestination = startDestination)
            val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
            val content by appModel.cutCardContent.observeAsState(null)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .topOuterShadow(8.dp, 30.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
                    .clip(cutCorner)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .then(content?.second ?: Modifier)
                ) {
                    Crossfade(
                        targetState = content,
                        label = "ContentTransition"
                    ) { composable ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            composable?.first?.invoke()
                        }
                    }
                }
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