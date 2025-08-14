package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.LogHelper
import java.util.concurrent.TimeUnit

class DealCollector(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "DEAL_COLLECTOR"

        /**
         * setup job on phone
         */
        fun setupJob() {
            val manager = WorkManager.getInstance(App.context())
            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            //add job to collect
            manager.enqueue(
                PeriodicWorkRequestBuilder<DealCollector>(6, TimeUnit.HOURS).setInitialDelay(
                    1,
                    TimeUnit.HOURS
                ).addTag(JOBTAG).build()
            )
            LogHelper.d("Job is started", JOBTAG)
        }


    }

    /**
     * collect jobs
     */
    override suspend fun doWork(): Result {
        AmznScrape().cache(43200).bestsellers { }
        return Result.success()
    }

}

