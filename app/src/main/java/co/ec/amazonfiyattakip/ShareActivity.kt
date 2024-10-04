package co.ec.amazonfiyattakip

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import co.ec.amazonfiyattakip.ui.theme.AmazonFiyatTakipTheme
import kotlinx.coroutines.launch


import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.AmznRequest
import co.ec.amazonfiyattakip.ui.AppProviders
import co.ec.amazonfiyattakip.ui.LocalDB
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ModalPart
import co.ec.amazonfiyattakip.ui.part.ScreenContent
import co.ec.amazonfiyattakip.ui.part.ScreenOptions
import co.ec.helper.AppLogger
import co.ec.helper.AppSharedSettings

class ShareActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        var sharedSettings = AppSharedSettings.get()

        // Check if the activity was started by a share intent
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (sharedText != null) {
                val regex = "(https?://[\\w-]+(\\.[\\w-]+)+(/[\\w-./?%&=]*)?)".toRegex()
                val url = regex.find(sharedText)?.value
                if (url != null) {
                    AmznRequest.getRealUrl(url, { real ->
                        sharedSettings.putString("sharedUrl", real)
                        redirect()
                    }, {
                        sharedSettings.putString("sharedUrl", "")
                        redirect()
                    })
                } else {
                    sharedSettings.putString("sharedUrl", "")

                    redirect()
                }

            } else {
                redirect()
            }
        }
    }

    private fun redirect() {

        //redirect to main
        val redirectIntent = Intent(this, MainActivity::class.java)
        redirectIntent.putExtra("destination", "add")
        startActivity(redirectIntent)
    }
}

