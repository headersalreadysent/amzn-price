package co.ec.amazonfiyattakip

import androidx.compose.material3.SnackbarHostState
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.service.AmznRequest.sharedSettings
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.App
import co.ec.helper.AppEventBus
import co.ec.helper.AppSharedSettings
import co.ec.helper.utils.unix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import co.ec.helper.App as Application

class App : Application() {

    private lateinit var sharedSettings:AppSharedSettings

    companion object {

        private lateinit var instance: Application

        fun context() = Application.context()

        fun contextCheck() = Application.contextCheck()

        private var snackOptions: Pair<SnackbarHostState, CoroutineScope>? = null
        lateinit var INSTANCE: App

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
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        setupSharedSettings()


        AppDatabase.getDatabase()

        GlobalScope.launch {
            AppEventBus.subscribe<AppSharedSettings.SettingsChange> {
                if(it.name=="queryTime"){
                    PriceUpdate.setupJob(it.value as Int)

                }
            }
        }
    }


    private fun setupSharedSettings() {
        //activate or deactivate collection
        sharedSettings= AppSharedSettings(App.context())
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