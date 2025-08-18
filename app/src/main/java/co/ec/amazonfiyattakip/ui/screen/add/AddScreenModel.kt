package co.ec.amazonfiyattakip.ui.screen.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)

    val recordedPrices = MutableLiveData<List<PriceInfo>?>(null)


    /**
     * scrape from shared or url
     */
    fun scrapeFromSharedUrl(asinCode: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val settings = SettingsHelper.get()
            val scraper = AmznScrape().cache()
            val asinScrapeUrl = asinCode ?: settings.getString("sharedUrl")
            asinScrapeUrl?.let {
                scraper.scrape(asinScrapeUrl, { scraped ->
                    viewModelScope.launch {
                        setupProduct(scraped)
                    }
                }, {
                    App.snack("Ürün bilgisi bulunamadı.")
                })
            }
            //remove in every time
            settings.remove("sharedUrl")
        }

    }


    private fun setupProduct(loaded: Product) {

        product.value = loaded
        viewModelScope.launch {
            FireDB.getByAsin(loaded.asin)?.let {
                recordedPrices.value = it.priceInfoList
            }
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