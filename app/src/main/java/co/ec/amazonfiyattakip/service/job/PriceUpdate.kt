package co.ec.amazonfiyattakip.service.job

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.R
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.job_log.JobLog
import co.ec.amazonfiyattakip.db.noprice.NoPrice
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.view.DailyPrice
import co.ec.amazonfiyattakip.helper.NotificationHelper
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.unix
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class PriceUpdate(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {


    companion object {

        const val JOBTAG = "PriceUpdateJob"

        /**
         * setup job on phone
         */
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
                                LogHelper.d("$JOBTAG is cancelled", JOBTAG)
                                startJob()
                            }

                            WorkInfo.State.FAILED -> {
                                LogHelper.d("$JOBTAG is failed", JOBTAG)
                                startJob()
                            }

                            else -> {
                                LogHelper.d("$JOBTAG state is ${workInfo.state}", JOBTAG)
                            }
                        }
                    }
                },
                ContextCompat.getMainExecutor(App.context())
            )
        }

        /**
         * start job
         */
        private fun startJob() {
            val queryTime = SettingsHelper.get().getInt("queryTime", 15)
            val manager = WorkManager.getInstance(App.context())
            //clear all jobs
            manager.cancelAllWorkByTag(JOBTAG)
            manager.pruneWork()
            //add with time diff
            manager.enqueue(
                PeriodicWorkRequestBuilder<PriceUpdate>(queryTime.toLong(), TimeUnit.MINUTES)
                    .addTag(JOBTAG)
                    .build()
            )
            LogHelper.d("Job is started", JOBTAG)

        }

        suspend fun run(override: Boolean = false): Result {
            LogHelper.d("price update running", JOBTAG)
            val productDao = AppDatabase.getDatabase().product()
            val jobLogDao = AppDatabase.getDatabase().jobLog()
            return try {
                coroutineScope {
                    //get suitable products
                    val asinList = productDao.getScrapeWaitingAsinCodes(
                        //if override setted add one day
                        if (override) unix() + 86400 else unix()
                    )
                    val outputData = Data.Builder()
                    if (asinList.isNotEmpty()) {
                        LogHelper.d("Updating products: ${asinList.map { it.asin }}", JOBTAG)
                        val responseList = asinList.map {
                            return@map async { collectProduct(it) }
                        }.awaitAll()
                        outputData.putString("output", "$responseList")
                        //insert job log
                        jobLogDao.insert(
                            JobLog(
                                asin = asinList.joinToString(", ") { it.asin },
                                date = unix(),
                                detail = responseList.joinToString("\n") {
                                    "${it.first} => ${it.second.price()}"
                                },
                            )
                        )
                        //mark error stop if access to limit
                        productDao.markErrorStop()
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        var allProductsData = AppDatabase.getDatabase().product().getAllProducts()
                        CacheHelper.get()
                            .put("allProducts", Json.encodeToString(allProductsData), 43200)
                    }

                    Result.success(outputData.build())
                }
            } catch (e: Exception) {
                LogHelper.e(e.localizedMessage ?: e.message ?: "", e, JOBTAG)
                Result.failure()
            }
        }


        /**
         * collect price and save to database
         */
        suspend fun collectProduct(product: Product): Pair<String, Int> {

            val productDao = AppDatabase.getDatabase().product()
            val priceDao = AppDatabase.getDatabase().priceInfo()
            val deferred = CompletableDeferred<Pair<String, Int>>()

            val scraper = AmznScrape()
            //scrape from amazn
            scraper.scrapeFromAsin(product.asin, { update ->
                //log update info
                LogHelper.d(
                    "${update.asin} (${update.shortTitle()}) : ${update.price()}",
                    JOBTAG
                )
                if (update.price == 0) {
                    //price is zero is very bad
                    LogHelper.d("Product ${product.asin}:${product.title} price error", JOBTAG)
                    NoPrice.add(product.asin, productId = product.id)
                    deferred.complete(Pair(product.asin, -1))
                    return@scrapeFromAsin
                }
                CoroutineScope(Dispatchers.IO).launch {
                    //set id to new
                    val product = update.copy(id = product.id)
                    //delete old no product
                    NoPrice.remove(product.id)
                    //get latest update
                    val latestPrice = priceDao.getLatestPrice(product.id)
                    val priceInfo = PriceInfo(
                        id = 0, productId = product.id, asin = product.asin, date = unix(),
                        price = product.price, star = product.star, comment = product.comment,
                        priceChanged = product.price - (latestPrice?.price ?: 0)
                    )
                    latestPrice?.let {
                        //thereis older price
                        if (priceInfo.price != it.price) {
                            //price changed in this query let test with average
                            val latestAverage = priceDao.getLatestAverage(product.id)
                            checkNotification(product, priceInfo, it, latestAverage)
                        }
                    }
                    //insert new price
                    priceDao.insert(priceInfo)

                    //add next run time
                    productDao.updateProductInfoAndNextRun(
                        product.id, product.price, product.star, product.comment
                    )
                    //send to analytics for stats
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
                //complete defer with correct price
                deferred.complete(Pair(product.asin, update.price))
            }, {
                LogHelper.e(it.localizedMessage ?: it.message ?: "", it)
                CoroutineScope(Dispatchers.IO).launch {
                    productDao.addErrorCount(product.id)
                    FireDB.syncProduct(product)
                }
                deferred.complete(Pair(product.asin, -1))
            })
            //return response
            return deferred.await()
        }

        /**
         * check if notification required
         */
        private fun checkNotification(
            product: Product,
            priceInfo: PriceInfo,
            latestPrice: PriceInfo,
            latestAverage: DailyPrice
        ) {
            if (priceInfo.price < latestAverage.avgPrice) {
                LogHelper.d("${product.title} price dropped", JOBTAG)
                NotificationHelper.showNotification(
                    product,
                    "Fiyat düştü. ${priceInfo.price.price()} ${product.title} fiyatı ortalamanın altına düştü.",
                    "Son ortalama fiyat ${latestAverage.avgPrice.price()}",
                    R.drawable.trending_down
                )
            }
            if (priceInfo.price > latestAverage.avgPrice) {
                LogHelper.d("${product.title} price increased", JOBTAG)
                NotificationHelper.showNotification(
                    product,
                    "Fiyat yükseldi. ${priceInfo.price.price()} ${product.title} fiyatı ortalamanın üstüne yükseldi.",
                    "Son ortalama fiyat ${latestAverage.avgPrice.price()}",
                    R.drawable.trending_up
                )
            }
        }

    }



    override suspend fun doWork(): Result {
        return run()
    }

}

