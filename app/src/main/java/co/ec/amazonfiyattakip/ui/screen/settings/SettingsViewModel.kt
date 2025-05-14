package co.ec.amazonfiyattakip.ui.screen.settings

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.service.job.PriceUpdate.Companion.JOBTAG
import co.ec.helper.helpers.SettingsHelper

class SettingsViewModel : ViewModel() {

    private val workManager = WorkManager.getInstance(App.context())
    var map = MutableLiveData<Map<String, Any?>>(mapOf())

    private var shared = SettingsHelper.get()

    private val _nextWorkTime = MutableLiveData<Long?>()
    val nextWorkTime: LiveData<Long?> get() = _nextWorkTime
    private val workObserver = Observer<List<WorkInfo>> { workInfos ->
        val nextRun = workInfos.firstOrNull()?.nextScheduleTimeMillis
        _nextWorkTime.value = nextRun
    }


    private var booleanKeys = listOf("dynamicTheme","showBasketTotal","showServerProducts")
    private var intKeys = listOf("queryTime","colorContrast")

    fun startWatch() {
        val localMap = map.value?.toMutableMap() ?: mutableMapOf()
        booleanKeys.forEach {
            localMap[it] = shared.getBoolean(it)
        }
        intKeys.forEach {
            localMap[it] = shared.getInt(it)
        }
        map.value = localMap
    }

    fun set(key: String, value: Any) {
        try {
            if (value is Boolean) {
                shared.putBoolean(key, value)
            }
            if (value is Int) {
                shared.putInt(key, value)
            }
            val localMap = map.value?.toMutableMap() ?: mutableMapOf()
            localMap[key] = value
            map.value = localMap

        } catch (e: Throwable) {
            e.message?.let { App.snack(it) }
        }


    }




    fun collectJobRuns() {
        workManager.getWorkInfosByTagLiveData(JOBTAG).observeForever(workObserver)
    }

    override fun onCleared() {
        super.onCleared()
        workManager.getWorkInfosByTagLiveData(JOBTAG).removeObserver(workObserver)
    }
}