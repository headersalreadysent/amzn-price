package co.ec.amazonfiyattakip

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.job.DeleteOldProducts
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.CnsynApp
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.EventBus
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.unix
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : CnsynApp() {

    private lateinit var sharedSettings: SettingsHelper
    lateinit var firebaseAnalytics: FirebaseAnalytics


    companion object {

        private lateinit var instance: App

        private var hostState: SnackbarHostState? = null

        fun context() = CnsynApp.context()

        fun snack(text: String, duration: SnackbarDuration = SnackbarDuration.Short) {
            hostState?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    hostState?.showSnackbar(text, duration = duration)
                }
            }
        }

        fun setupSnackbar(state: SnackbarHostState) {
            hostState = state
        }

        /**
         * record event on actions
         */
        fun event(event: String, params: Map<String, Any>) {
            instance.firebaseAnalytics.logEvent(event) {
                params.forEach {
                    if (it.value is Long) {
                        param(it.key, it.value as Long)
                    }
                    if (it.value is Int) {
                        param(it.key, (it.value as Int).toLong())
                    }
                    if (it.value is String) {
                        param(it.key, it.value as String)
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this

        setupSharedSettings()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        AppDatabase.getDatabase()
        CacheHelper(this, "globalCache")

        GlobalScope.launch {
            EventBus.subscribe<SettingsHelper.SettingsChange> {
                if (it.name == "queryTime") {
                    PriceUpdate.setupJob()
                }
            }
        }
        PriceUpdate.setupJob()
        DeleteOldProducts.setupJob()
    }


    private fun setupSharedSettings() {
        //activate or deactivate collection
        sharedSettings = SettingsHelper(applicationContext)
        //set run times
        if (sharedSettings.getBoolean("firstRun", true)) {
            sharedSettings.putBoolean("firstRun", false)
            sharedSettings.putInt("appSetup", unix().toInt())
        }
        sharedSettings.putInt("appLastStart", unix().toInt())
        sharedSettings.apply {
            putInt("queryTime", getInt("queryTime", 15))
            putBoolean("dynamicTheme", getBoolean("dynamicTheme", false))
            putInt("colorContrast", getInt("colorContrast", 1))
        }

    }
}