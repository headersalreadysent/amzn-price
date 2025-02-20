package co.ec.amazonfiyattakip

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.job.DeleteOldProducts
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.AppEventBus
import co.ec.helper.AppSharedSettings
import co.ec.helper.utils.unix
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : co.ec.helper.App() {

    private lateinit var sharedSettings:AppSharedSettings
    lateinit var firebaseAnalytics: FirebaseAnalytics


    companion object {

        private lateinit var instance: App

        private var snackOptions: Pair<SnackbarHostState, CoroutineScope>? = null

        fun context() = co.ec.helper.App.context()

        fun snack(text: String) {
            snackOptions?.let {
                it.second.launch {
                    it.first.showSnackbar(text)
                }
            }
        }

        fun setupSnackbar(current: SnackbarHostState, coroutineScope: CoroutineScope) {
            snackOptions = Pair(current, coroutineScope)
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
        instance=this

        setupSharedSettings()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        AppDatabase.getDatabase()

        GlobalScope.launch {
            AppEventBus.subscribe<AppSharedSettings.SettingsChange> {
                if(it.name=="queryTime"){
                    PriceUpdate.setupJob()

                }
            }
        }
        PriceUpdate.setupJob()
        DeleteOldProducts.setupJob()
    }


    private fun setupSharedSettings() {
        //activate or deactivate collection
        sharedSettings= AppSharedSettings(applicationContext)
        //set run times
        if (sharedSettings.getBoolean("firstRun", true)) {
            sharedSettings.putBoolean("firstRun", false)
            sharedSettings.putInt("appSetup", unix().toInt())
        }
        sharedSettings.putInt("appLastStart", unix().toInt())
        sharedSettings.apply {
            putInt("queryTime", getInt("queryTime",15))
            putBoolean("dynamicTheme", getBoolean("dynamicTheme",false))

        }

    }
}