package co.ec.amazonfiyattakip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.ui.AppProviders
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ModalPart
import co.ec.amazonfiyattakip.ui.part.ScreenContent
import co.ec.amazonfiyattakip.ui.part.ScreenOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    startDestination: String = "main",
    appModel: AppModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var screenOptions by remember { mutableStateOf(ScreenOptions()) }
    LaunchedEffect(screenOptions) {
        AppModel.setFabClick(screenOptions.fab?.second ?: {})
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalPart(drawerState)
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),

            snackbarHost = {
                val snackbarHostState = LocalSnackbar.current
                SnackbarHost(hostState = snackbarHostState)
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        IconButton(onClick = { /* Handle click */ }) {
                            Icon(Icons.Default.Home, contentDescription = "Menu")
                        }
                        IconButton(onClick = { /* Handle click */ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        Spacer(Modifier.weight(1f, true))
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    contentColor = MaterialTheme.colorScheme.primary,
                    floatingActionButton = {
                        screenOptions.fab?.let { fit ->
                            val fabClick by appModel.fabClick.observeAsState({})
                            FloatingActionButton(
                                onClick = fabClick
                            ) {
                                Icon(fit.first, contentDescription = "")
                            }
                        }
                    },
                )
            },

        ) { screen ->
            Box() {

                ScreenContent(
                    optionsChanged = {
                        screenOptions = it
                    },
                    startDestination = startDestination
                )
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