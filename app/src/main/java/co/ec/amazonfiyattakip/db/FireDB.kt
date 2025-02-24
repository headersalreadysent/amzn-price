package co.ec.amazonfiyattakip.db

import android.util.Log
import androidx.annotation.Keep
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.helper.autoToString
import co.ec.helper.helpers.EventBus
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.unix
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.concurrent.thread


object FireDB {

    class ProductSync(var asin: String)

    @Keep
    class ProductRecord() {
        var asin: String = ""
        var title: String = ""
        var description: String = ""
        var image: String = ""
        var extras: String = ""
        var prices: List<String>? = null
        var latestUpdate: Long = unix()

        constructor(product: Product, priceList: List<PriceInfo>) : this() {
            asin = product.asin
            title = product.title
            description = product.description
            image = product.image
            extras = product.extras
            prices = priceList.map { "${it.date}|${it.price}|${it.star}|${it.comment}" }
        }

        /**
         * merge local prices with remote
         */
        @OptIn(DelicateCoroutinesApi::class)
        fun sync(product: Product, localPrices: List<PriceInfo>) {
            //merge prices

            val existedDates = localPrices.map { it.date.toString() }

            val serverPriceList = prices.orEmpty().map { it.split("|") }
            val remotePrices = serverPriceList
                .filter { !existedDates.contains(it[0]) }
                .map {
                    PriceInfo(
                        id = 0,
                        productId = product.id,
                        asin = product.asin,
                        date = it[0].toLong(),
                        price = it[1].toInt(),
                        star = it[2].toDouble(),
                        comment = it[3].toInt(),
                        priceChanged = 0,
                    )
                }
            //generate local list
            val localPriceList = localPrices.map {
                listOf(
                    it.date.toString(),
                    it.price.toString(),
                    it.star.toString(),
                    it.comment.toString()
                )
            }
            //merge lists and
            var lastDate = 0
            val priceList = (serverPriceList + localPriceList)
                .distinctBy { it.first() }
                .sortedBy { it.first() }
                .filter {
                    val date = it[0].toInt()
                    if (date - lastDate < 60 * 3) {
                        //if this is too close the last one
                        return@filter false
                    }
                    lastDate = date
                    return@filter true
                }
            prices = priceList.map { it.joinToString("|") }
            //update local product
            priceList.lastOrNull()?.let {
                product.price = it[1].toInt()
                product.star = it[2].toDouble()
                product.comment = it[3].toInt()
            }
            // insert remote prices and update local product
            thread {
                AppDatabase.getDatabase().priceInfo().insertAll(remotePrices)
                AppDatabase.getDatabase().product().update(product)
            }
            latestUpdate = unix()
            GlobalScope.launch {
                EventBus.publish(ProductSync(product.asin))
            }
        }
    }


    fun syncProduct(product: Product) {
        try {
            GlobalScope.launch {
                //first get product
                val productRef = Firebase.firestore.collection("products").document(product.asin)
                val prices = AppDatabase.getDatabase().priceInfo().getPricesByProduct(product.id)
                val snapshot = productRef.get().await()
                if (snapshot.exists()) {
                    snapshot.toObject(ProductRecord::class.java)?.let {
                        it.sync(product, prices)
                        productRef.set(it)
                        LogHelper.d("Firebase ${product.asin} updated ${it.autoToString()}")
                    }
                } else {
                    val record = ProductRecord(product, prices)
                    productRef.set(record)
                    LogHelper.d("Firebase ${product.asin} inserted ${record.autoToString()}")
                }
            }

        } catch (e: Throwable) {

            LogHelper.d("Firebase error ${e.message}")
        }
    }


    suspend fun collect(): List<Product> {

        //first get product
        return try {
            val snapshot = Firebase.firestore.collection("products").get().await()
            snapshot.documents.mapNotNull { it.toObject(ProductRecord::class.java) }.map {
                val price = (it.prices?.lastOrNull() ?: "0|0|0|0|").split("|")
                Product(
                    id = 0,
                    asin = it.asin,
                    date = it.latestUpdate,
                    title = it.title,
                    description = it.description,
                    price = price[1].toInt(),
                    star = price[2].toDouble(),
                    comment = price[3].toInt(),
                    image = it.image,
                    extras = it.extras,
                    nextRunTime = unix(),
                    timeSpan = 60,
                    errorCount = 0,
                    status = ProductStatus.ACTIVE
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteOldRecords() {
        val thirtyDaysAgo = unix() - (30 * 24 * 60 * 60) // 30 days in seconds
        try {
            val snapshot = Firebase.firestore.collection("products")
                .whereLessThan("latestUpdate", thirtyDaysAgo)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }
        } catch (e: Exception) {
            LogHelper.d("Firebase error while deleting old records ${e.message}")
        }
    }
}