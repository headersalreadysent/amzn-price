package co.ec.amazonfiyattakip.ui.screen.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.LogHelper
import co.ec.helper.helpers.SettingsHelper
import co.ec.helper.utils.asyncRun

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)


    fun recordFromShareUrl(asinCode: String? = null) {
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
        } else {
            App.snack("Ürün bağlantısı bulunamadı.")
        }
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

    }

    fun emulate() {
        product.value = Product.fake()
    }


}