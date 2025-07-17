package co.ec.amazonfiyattakip

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.helper.PermissionHelper
import co.ec.amazonfiyattakip.service.job.Cleanup
import co.ec.amazonfiyattakip.service.job.DeleteOldProducts
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.CnsynApp
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.EventBus
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.unix
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class App : CnsynApp() {

    private lateinit var sharedSettings: SettingsHelper
    private lateinit var cacheHelper: CacheHelper
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

        fun settings(): SettingsHelper {
            return instance.sharedSettings
        }

        fun cache(): CacheHelper {
            return instance.cacheHelper
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

    override fun onCreate() {
        super.onCreate()
        instance = this
        AppDatabase.getDatabase()
        setupSharedSettings()
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        cacheHelper = CacheHelper(this, "globalCache")

        //setup jobs
        PriceUpdate.setupJob()
        DeleteOldProducts.setupJob()
        Cleanup.setupJob()
        //setup listen
        CoroutineScope(Dispatchers.IO).launch {
            EventBus.subscribe<SettingsHelper.SettingsChange> {
                if (it.name == "queryTime") {
                    PriceUpdate.setupJob()
                }
            }
        }
    }


    private fun setupSharedSettings() {
        //activate or deactivate collection
        sharedSettings = SettingsHelper(applicationContext)
        sharedSettings.apply {
            LogHelper.d("restore ${getBoolean("firstRun", true)}")
            PermissionHelper.checkBackupAccess()
            if (getBoolean("firstRun", true)) {
                //if first run try to restore data
                putBoolean("firstRun", false)
                putInt("appSetup", unix().toInt())
            }
            putInt("appLastStart", unix().toInt())
            putInt("queryTime", getInt("queryTime", 15))
            putBoolean("dynamicTheme", getBoolean("dynamicTheme", false))
            putInt("colorContrast", getInt("colorContrast", 1))
            putBoolean("showBasketTotal", getBoolean("showBasketTotal", false))
            putBoolean("showServerProducts", getBoolean("showServerProducts", true))
            putBoolean("showDealsInfo", getBoolean("showDealsInfo", true))
            putBoolean("expertMode", getBoolean("expertMode", false))
            putBoolean("developerActive", getBoolean("developerActive", false))
            val developerActive = PermissionHelper.checkDeveloperFile()
            if (developerActive) {

                putBoolean("developerActive", true)
            }
        }

    }
}