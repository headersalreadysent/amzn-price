package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.FireDB
import java.util.concurrent.TimeUnit

class DeleteOldProducts(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "DeleteOldProductJob"

        fun setupJob() {
            val manager = WorkManager.getInstance(App.context())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            //add jobs
            val deleteOldProducts =
                PeriodicWorkRequestBuilder<DeleteOldProducts>(1440, TimeUnit.MINUTES)
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    .setConstraints(constraints)
                    .build()
            manager.enqueue(deleteOldProducts)
        }


    }

    override suspend fun doWork(): Result {
        //delete old records
        FireDB.deleteOldRecords()
        return Result.success()
    }

}