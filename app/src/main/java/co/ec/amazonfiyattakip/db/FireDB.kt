package co.ec.amazonfiyattakip.db

import androidx.annotation.Keep
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.autoToString
import co.ec.helper.AppLogger
import co.ec.helper.Async
import co.ec.helper.utils.unix
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlin.concurrent.thread


object FireDB {

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
            val priceList = (serverPriceList + localPriceList)
                .distinctBy { it.first() }
                .sortedBy { it.first() }
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
            latestUpdate= unix()
        }
    }


    fun addProduct(asin: String, then: () -> Unit = {}) {
        //first get product
        val productRef = Firebase.firestore.collection("products").document(asin)
        Async.run({
            val product = AppDatabase.getDatabase().product().getByAsin(asin)
            product?.let { product ->
                val prices = AppDatabase.getDatabase().priceInfo().getPricesByProduct(product.id)

                productRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        //exists update it
                        snapshot.toObject(ProductRecord::class.java)?.let {
                            it.sync(product, prices)
                            productRef.set(it)
                            AppLogger.d("Firebase $asin updated ${it.autoToString()}")
                        }
                    } else {
                        //generate record
                        val record = ProductRecord(product, prices)
                        productRef.set(record)
                        AppLogger.d("Firebase $asin inserted ${record.autoToString()}")
                    }
                    then()
                }.addOnFailureListener {
                    AppLogger.d("Firebase error ${it.message}")
                }
            }

        })

    }
}