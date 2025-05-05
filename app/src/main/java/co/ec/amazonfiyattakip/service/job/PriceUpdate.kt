package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.amazonfiyattakip.db.price_info.PriceInfoDao
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.unix
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread


class PriceUpdate(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "PriceUpdateJob"
        fun setupJob() {

            val manager = WorkManager.getInstance(App.context())
            //test if work cancelled restart it
            val list = manager.getWorkInfosByTag(JOBTAG)
            if (list.get().isEmpty()) {
                startJob()
            }

            list.addListener(
                {
                    val workInfos = list.get()
                    if (workInfos != null && workInfos.isEmpty()) {
                        startJob()
                        return@addListener
                    }
                    workInfos?.forEach { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.CANCELLED -> {
                                LogHelper.d("$JOBTAG is cancelled", "Job")
                                startJob()
                            }

                            WorkInfo.State.FAILED -> {
                                LogHelper.d("$JOBTAG is failed", "Job")
                                startJob()
                            }

                            else -> {
                                LogHelper.d("$JOBTAG state is ${workInfo.state}", "job")
                            }
                        }
                    }
                },
                ContextCompat.getMainExecutor(App.context())
            )
        }

        private fun startJob() {
            val queryTime = SettingsHelper.get().getInt("queryTime", 15)
            val manager = WorkManager.getInstance(App.context())


            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()


            manager.enqueue(
                PeriodicWorkRequestBuilder<PriceUpdate>(queryTime.toLong(), TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    .build()
            )


            manager.enqueue(
                OneTimeWorkRequestBuilder<PriceUpdate>()
                    .addTag(JOBTAG)
                    .build()
            )

            LogHelper.d("$JOBTAG is started", "Job")

        }

        suspend fun collectPrices(override:Boolean=false): Result {
            val productDao = AppDatabase.getDatabase().product()
            val jobLog = AppDatabase.getDatabase().jobLog()
            val now = unix()
            return try {
                coroutineScope {
                    //get suitable products
                    val asinList = productDao.getScrapeWaitingAsinCodes(
                        //if override setted add one day
                        if(override) now+86400 else now
                    )
                    val outputData = Data.Builder()
                    if (asinList.isNotEmpty()) {
                        LogHelper.d("$JOBTAG products: $asinList", "Job")
                        val responseList = asinList.map {
                            return@map async { collectDayInfo(it) }
                        }.awaitAll()
                        outputData.putString("output", "$responseList")
                        //insert job log
                        jobLog.insert(
                            JobLog(
                                asin = asinList.map { it.asin }.joinToString(", "),
                                date = now,
                                detail = responseList.map {
                                    "${it.first} => ${it.second.price()}"
                                }.joinToString("\n")
                            )
                        )
                        //mark error stop if access to limit
                        productDao.markErrorStop()
                    }
                    Result.success(outputData.build())
                }
            } catch (e: Exception) {
                LogHelper.e("$JOBTAG ${e.localizedMessage}", e, "Job")
                Result.failure()
            }
        }


        /**
         * collect price and save to database
         */
        private suspend fun collectDayInfo(product: Product): Pair<String, Int> {

            val productDao = AppDatabase.getDatabase().product()
            LogHelper.d("$JOBTAG product ${product.asin}", "Job")
            val deferred = CompletableDeferred<Pair<String, Int>>()

            val scraper = AmznScrape()
            //collect one asin
            scraper.scrapeFromAsin(product.asin, { update ->
                if (update.price == 0) {
                    LogHelper.d("product ${product.asin} price is 0")
                } else {
                    //insert into database
                    thread {
                        val product = update.copy(
                            id = product.id
                        )
                        PriceInfoDao.insertNewUpdate(product)
                        //add next run time
                        productDao.updateProductInfoAndNextRun(
                            product.id,
                            product.price,
                            product.star,
                            product.comment
                        )

                        LogHelper.d("$JOBTAG ${product.price} : ${product.title}", "Job")

                        App.event(
                            "price_update", mapOf(
                                "productAsin" to product.asin,
                                "productTitle" to product.title,
                                "productPrice" to product.price,
                                "productStar" to product.star.toString(),
                                "productComment" to product.comment.toString()
                            )
                        )
                    }
                }

                //complete defer with correct price
                deferred.complete(Pair(product.asin, update.price))
            }, {
                LogHelper.e(it.localizedMessage ?: it.message ?: "", it)
                //complete defer with correct -1 because of error
                thread { productDao.addErrorCount(product.id) }
                deferred.complete(Pair(product.asin, -1))
                FireDB.syncProduct(product)
            })
            //return response
            return deferred.await()


        }

    }

    override suspend fun doWork(): Result {
        return collectPrices()
    }

}