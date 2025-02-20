package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.await
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.AsinId
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.price_info.PriceInfoDao
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.AppLogger
import co.ec.helper.AppSharedSettings
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class DeleteOldProducts(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "DeleteOldProductJob"

        fun setupJob() {

            val manager = WorkManager.getInstance(App.context())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()


            val deleteOldProducts =
                PeriodicWorkRequestBuilder<DeleteOldProducts>(1440, TimeUnit.MINUTES)
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    .setConstraints(constraints)
                    .build()

            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            //add jobs
            manager.enqueue(deleteOldProducts)

            val oneTimeWorkRequest =
                OneTimeWorkRequestBuilder<PriceUpdate>()
                    .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    .setConstraints(constraints)
                    .build()
            manager.enqueue(oneTimeWorkRequest)

            AppLogger.d("$JOBTAG is started", "Job")

        }


    }

    override suspend fun doWork(): Result {
        //delete old records
        FireDB.deleteOldRecords()
        return Result.success()
    }

}