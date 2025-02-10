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
import co.ec.amazonfiyattakip.db.price_info.PriceInfoDao
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.AppLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class PriceUpdate(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private val productDao = AppDatabase.getDatabase().product()

    companion object {

        val JOBTAG = "PriceUpdateJob"

        @OptIn(DelicateCoroutinesApi::class)
        fun setupJob() {

            val manager = WorkManager.getInstance(App.context())
            //test if work cancelled restart it
            val list = manager.getWorkInfosByTag(JOBTAG)
            list.addListener(
                {
                    val workInfos = list.get()
                    AppLogger.d("$JOBTAG is $workInfos", "Job")
                    if (workInfos != null && workInfos.isEmpty()) {
                        startJob()
                        return@addListener
                    }
                    workInfos?.forEach { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.CANCELLED -> {
                                AppLogger.d("$JOBTAG is cancelled", "Job")
                                startJob()
                            }

                            WorkInfo.State.FAILED -> {
                                AppLogger.d("$JOBTAG is failed", "Job")
                                startJob()
                            }

                            else -> {
                                AppLogger.d("$JOBTAG state is ${workInfo.state}", "job")
                            }
                        }
                    }
                },
                ContextCompat.getMainExecutor(App.context())
            )
            GlobalScope.launch {
              //  collectPrices()
            }
        }

        fun startJob() {
            val manager = WorkManager.getInstance(App.context())
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()


            val updatePriceRequest =
                PeriodicWorkRequestBuilder<PriceUpdate>(15, TimeUnit.MINUTES)
                    // .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    // .setConstraints(constraints)
                    .build()

            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            //add jobs
            manager.enqueue(updatePriceRequest)


            val oneTimeWorkRequest =
                OneTimeWorkRequestBuilder<PriceUpdate>()
                    // .setInitialDelay(1, TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    // .setConstraints(constraints)
                    .build()
            manager.enqueue(oneTimeWorkRequest)

            AppLogger.d("$JOBTAG is started", "Job")

        }

        private suspend fun collectPrices() : Result{
            val productDao = AppDatabase.getDatabase().product()
            return try {
                coroutineScope {
                    val asinList = productDao.getScrapeWaitingAsinCodes()
                    AppLogger.d("$JOBTAG products: $asinList", "Job")
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
                AppLogger.e("$JOBTAG ${e.localizedMessage}", e, "Job")
                Result.failure()
            }
        }



        /**
         * collect price and save to database
         */
        private suspend fun collectDayInfo(asin: AsinId): Pair<String, Int> {

            val productDao = AppDatabase.getDatabase().product()
            AppLogger.d("$JOBTAG product $asin", "Job")
            val deferred = CompletableDeferred<Pair<String, Int>>()

            val scraper = AmznScrape()
            //collect one asin
            scraper.scrapeFromAsin(asin.asin, { update ->

                //insert into database
                thread {
                    val product = update.copy(
                        id = asin.id
                    )
                    PriceInfoDao.insertNewUpdate(product)
                    //add next run time
                    productDao.updateProductInfoAndNextRun(
                        product.id,
                        product.price,
                        product.star,
                        product.comment
                    )

                    AppLogger.d("$JOBTAG ${product.price} : ${product.title}", "Job")
                }
                //complete defer with correct price
                deferred.complete(Pair(asin.asin, update.price))
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

    override suspend fun doWork(): Result {
        return collectPrices()
    }

}