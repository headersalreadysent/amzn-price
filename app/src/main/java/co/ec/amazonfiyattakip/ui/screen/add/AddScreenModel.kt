package co.ec.amazonfiyattakip.ui.screen.add

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznRequest
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.AppLogger
import co.ec.helper.AppSharedSettings
import co.ec.helper.Async

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)



    fun recordFromShareUrl() {
        val url = AppSharedSettings.get().getString("sharedUrl") ?: ""
        if (url != "") {
            var page=url
            if(!url.startsWith("http")){
                page=AmznScrape.urlFromAsin(url)
            }
            AmznScrape().scrapeFromUrl(page, { scraped ->
                AppLogger.d(scraped.toString())
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
    fun saveProduct(then: (id:Int) -> Unit = {}) {
        product.value?.let { record ->
            Async.run({
                val id=AppDatabase.getDatabase().product().insert(record)
                val price=record.toPriceInfo(id.toInt(),record.price)
                AppDatabase.getDatabase().priceInfo().insert(price)
                return@run id
            }, { id ->
                product.value = record.copy(
                    id = id.toInt()
                )
                App.snack("${record.title} kaydedildi.")
                then(id.toInt())
            })
        }

    }

    fun emulate() {
        product.value = Product.fake()
    }


}