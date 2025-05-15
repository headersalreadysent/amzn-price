package co.ec.amazonfiyattakip.ui.screen.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.service.job.PriceUpdate.Companion.JOBTAG

class SettingsViewModel : ViewModel() {

    private val workManager = WorkManager.getInstance(App.context())

    private val _nextWorkTime = MutableLiveData<Long?>()
    val nextWorkTime: LiveData<Long?> get() = _nextWorkTime
    private val workObserver = Observer<List<WorkInfo>> { workInfos ->
        val nextRun = workInfos.firstOrNull()?.nextScheduleTimeMillis
        _nextWorkTime.value = nextRun
    }

    fun collectJobRuns() {
        workManager.getWorkInfosByTagLiveData(JOBTAG).observeForever(workObserver)
    }

    override fun onCleared() {
        super.onCleared()
        workManager.getWorkInfosByTagLiveData(JOBTAG).removeObserver(workObserver)
    }
}