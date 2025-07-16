package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.helper.PermissionHelper
import co.ec.helper.helpers.LogHelper
import java.util.concurrent.TimeUnit

class DBBackup(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "BACKUP_DATABASE"

        /**
         * setup job on phone
         */
        fun setupJob(active: Boolean) {
            val manager = WorkManager.getInstance(App.context())
            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            if (active) {
                //add with time diff
                manager.enqueue(
                    PeriodicWorkRequestBuilder<DBBackup>(1, TimeUnit.DAYS)
                        .addTag(JOBTAG)
                        .build()
                )
                LogHelper.d("Job is started", JOBTAG)
            }

        }


    }

    /**
     * backup database
     */
    override suspend fun doWork(): Result {
        val backupFolder = App.settings().getString("backupLocation")
        return backupFolder?.let {
            if (PermissionHelper.checkBackupAccess()) {
                AppDatabase.backup(it, then = {
                    LogHelper.d("$it backup generated",JOBTAG)
                })
                Result.success()
            } else {
                null
            }
        } ?: Result.failure()

    }

}

