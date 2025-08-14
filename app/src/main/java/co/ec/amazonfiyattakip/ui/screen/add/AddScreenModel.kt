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
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.launch
import kotlin.random.Random

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)

    val recordedPrices = MutableLiveData<List<PriceInfo>?>(null)


    fun scrapeFromSharedUrl(asinCode: String? = null) {
        val settings = SettingsHelper.get()

        if (asinCode != null) {
            //if asin code exists
            App.cache().get("storeProduct")?.let {
                val cache = Product.decode(it)
                if (cache.asin == asinCode) {
                    LogHelper.d("storedProduct ${cache.encode()}", "AddModel")
                    return this.setupProduct(cache)
                }
            }
            AmznScrape().scrapeFromAsin(asinCode, { scraped ->
                LogHelper.d("scraped from asin ${scraped.encode()}", "AddModel")
                this.setupProduct(scraped)
            }, {
                App.snack("Ürün bilgisi bulunamadı.")
            })
        } else {

            //look for url on shared
            settings.getString("sharedUrl")?.let {
                settings.remove("sharedUrl")
                AmznScrape().scrapeFromUrl(it, { scraped ->
                    LogHelper.d("scraped from url ${scraped.encode()}", "AddModel")
                    this.setupProduct(scraped)
                }, {
                    App.snack("Ürün bilgisi bulunamadı.")
                })
            }
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