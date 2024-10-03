package co.ec.amazonfiyattakip

import android.content.Context
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.helper.App
import co.ec.helper.AppSharedSettings
import co.ec.helper.utils.unix
import co.ec.helper.App as Application

class App : Application() {


    companion object {

        private lateinit var instance: Application

        fun context() = Application.context()

        fun contextCheck() = Application.contextCheck()
    }

    override fun onCreate() {
        super.onCreate()
        AppSharedSettings(App.context())
        val settings = AppSharedSettings.get()
        //set run times
        if (settings.getBoolean("firstRun", true)) {
            settings.putInt("appSetup", unix().toInt())
            settings.putBoolean("firstRun", true)
        }
        settings.putInt("appLastStart", unix().toInt())

        AppDatabase.getDatabase()
    }

}