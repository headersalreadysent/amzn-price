package co.ec.amazonfiyattakip.ui.screen.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlin.math.asin
import kotlin.random.Random

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)

    val recordedPrices = MutableLiveData<List<PriceInfo>?>(null)


    fun scrapeFromSharedUrl(asinCode: String? = null) {
        val url = asinCode ?: SettingsHelper.get().getString("sharedUrl")
        if (url != null) {
            //if url not null
            var page = url
            if (!url.startsWith("http")) {
                page = AmznScrape.urlFromAsin(url)
            }
            AmznScrape().scrapeFromUrl(page, { scraped ->
                LogHelper.d(scraped.toString())
                product.value = scraped
            }, {
                it.printStackTrace()
            })
            asinCode?.let { asin ->
                asyncRun({
                    return@asyncRun FireDB.getByAsin(asin)
                }, {
                    it?.let {
                        recordedPrices.value = it.priceInfoList
                    }
                })
            }
        } else {
            App.snack("Ürün bağlantısı bulunamadı.")
        }
    }

    /**
     * update time span
     */
    fun updateTimeSpan(it: Int) {
        product.value = product.value?.copy(
            timeSpan = it * 60
        )
    }


    /**
     * save to database
     */
    fun saveProduct(then: (id: Int) -> Unit = {}) {
        product.value?.let { record ->
            asyncRun({
                val id = AppDatabase.getDatabase().product().insert(record)
                val price = record.toPriceInfo(id.toInt(), record.price)
                AppDatabase.getDatabase().priceInfo().insert(price)
                return@asyncRun id
            }, { id ->
                product.value = record.copy(
                    id = id.toInt()
                )
                App.snack("${record.title} kaydedildi.")
                App.event(
                    "product_add", mapOf(
                        "productTitle" to record.title,
                        "productAsin" to record.asin,
                        "productPrice" to record.price
                    )
                )
                then(id.toInt())
            })
        }

        recordedPrices.value = (0..30).map {
            val price = Random.nextFloat() * 20000 + product.value?.price!!
            PriceInfo(
                id = it,
                productId = 1,
                asin = product.value?.asin ?: "",
                date = unix() - (10 - it) * 86400,
                price = price.toInt()
            )
        }

    }

    fun emulate() {
        product.value = Product.fake()
        recordedPrices.value = (0..30).map {
            val price = Random.nextFloat() * 20000 + product.value?.price!!
            PriceInfo(
                id = it,
                productId = 1,
                asin = product.value?.asin ?: "",
                date = unix() - (10 - it) * 86400,
                price = price.toInt()
            )
        }
    }


}