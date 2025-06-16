package co.ec.amazonfiyattakip.service.job

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.helper.helpers.LogHelper
import java.io.File
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri
import androidx.work.WorkInfo

class Cleanup(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "CleanupJob"

        fun setupJob() {
            val manager = WorkManager.getInstance(App.context())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val activeJob = manager.getWorkInfosByTag(JOBTAG).get().any {
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
            }
            if (activeJob == false) {
                //clear all jobs
                manager.cancelAllWorkByTag(JOBTAG)
                manager.pruneWork()
                //add jobs
                val cleanupJob =
                    PeriodicWorkRequestBuilder<Cleanup>(1, TimeUnit.DAYS)
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .addTag(JOBTAG)
                        .setConstraints(constraints)
                        .build()
                manager.enqueue(cleanupJob)
            }

        }


    }

    override suspend fun doWork(): Result {
        //delete old records
        val context = App.context()
        val imageDir = File(context.getExternalFilesDir(null), "images")
        if (imageDir.exists()) {
            val products = AppDatabase.getDatabase().product().getAll() // imageUrl içeren veri
            val usedImageNames = products.mapNotNull {
                it.image.toUri().lastPathSegment
            }.toSet()

            imageDir.listFiles()?.forEach { file ->
                if (file.name !in usedImageNames) file.delete()
            }
        }
        return Result.success()
    }

}