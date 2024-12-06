package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.AsinId
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.AppLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class PriceUpdate(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val scraper = AmznScrape()
    private val priceInfoDao = AppDatabase.getDatabase().priceInfo()
    private val productDao = AppDatabase.getDatabase().product()

    companion object {

        fun setupJob() {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()


            val updatePriceRequest =
                PeriodicWorkRequestBuilder<PriceUpdate>(15, TimeUnit.MINUTES)
                    .setInitialDelay(15,TimeUnit.MINUTES)
                    .addTag("PriceUpdateJob")
                    .setConstraints(constraints)
                    .build()
            val manager = WorkManager.getInstance(App.context())

            //clear all jobs
            manager.cancelAllWorkByTag("PriceUpdateJob")
            manager.pruneWork()
            //add jobs
            manager.enqueue(updatePriceRequest)

            //run one time
            val updateNow = OneTimeWorkRequestBuilder<PriceUpdate>()
                .addTag("PriceUpdateJob")
                .setConstraints(constraints)
                .build()
            manager.enqueue(updateNow)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            coroutineScope {
                val asinList = productDao.getScrapeWaitingAsinCodes()
                val responseList = asinList.map {
                    return@map async { collectDayInfo(it) }
                }.awaitAll()
                val outputData = Data.Builder()
                    .putString("output", "$responseList")
                    .build()
                //mark error stop if access to limit
                productDao.markErrorStop()
                Result.success(outputData)
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }


    /**
     * collect price and save to database
     */
    private suspend fun collectDayInfo(asin: AsinId): Pair<String, Int> {

        val deferred = CompletableDeferred<Pair<String, Int>>()

        //collect one asin
        scraper.scrapeFromAsin(asin.asin, { product ->
            //insert into database
            thread {
                val priceInfo = product.toPriceInfo(asin.id)
                priceInfoDao.insert(priceInfo)
                //add next run time
                productDao.updateProductNextRun(asin.id,priceInfo.price)
            }
            //complete defer with correct price
            deferred.complete(Pair(asin.asin, product.price))
        }, {
            AppLogger.e(it.localizedMessage ?: it.message ?: "", it)
            //complete defer with correct -1 because of error
            thread { productDao.addErrorCount(asin.id) }
            deferred.complete(Pair(asin.asin, -1))
        })
        //return response
        return deferred.await()


    }
}