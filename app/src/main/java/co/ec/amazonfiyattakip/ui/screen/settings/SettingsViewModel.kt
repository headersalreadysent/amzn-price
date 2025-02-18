package co.ec.amazonfiyattakip.ui.screen.settings

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.helper.AppSharedSettings

class SettingsViewModel : ViewModel() {

    var map = MutableLiveData<Map<String, Any?>>(mapOf())

    private var shared: AppSharedSettings? = null


    private var booleanKeys = listOf("dynamicTheme")
    private var intKeys = listOf("")

    fun startWatch(settings: AppSharedSettings) {
        shared = settings
        readValues()
    }

    private fun readValues() {
        val localMap = map.value?.toMutableMap() ?: mutableMapOf()
        shared
        booleanKeys.forEach {
            localMap[it] = shared?.getBoolean(it)
        }
        intKeys.forEach {
            localMap[it] = shared?.getInt(it)
        }
        map.value = localMap
    }

    fun set(key: String, value: Any) {
        try {
            var format: Any = false
            if(value is Boolean){
                shared?.putBoolean(key,value)
            }
            if(value is Int){
                shared?.putInt(key,value)
            }
            val localMap = map.value?.toMutableMap() ?: mutableMapOf()
            localMap[key] = format
            map.value = localMap

        } catch (e: Throwable) {
        }


    }


}