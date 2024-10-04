package co.ec.amazonfiyattakip.ui.screen.add

import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.App
import co.ec.helper.AppLogger
import co.ec.helper.AppSharedSettings
import co.ec.helper.Async
import com.fleeksoft.ksoup.KsoupEngineInstance.init

class AddScreenModel : ViewModel() {

    val product = MutableLiveData<Product?>(null)

    init {

    }

    fun recordFromShareUrl() {
        val url =AppSharedSettings.get().getString("sharedUrl") ?: ""
        if (url != "") {
            AmznScrape().scrapeFromUrl(url, { scraped ->
                AppLogger.d(scraped.toString())
                product.value = scraped
            },{
                it.printStackTrace()
            })
        } else {
            Toast.makeText(App.context(), "Url bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordProductFromAsin(asin: String) {

        AmznScrape().scrapeFromAsin(asin, {
            Async.run({
                AppDatabase.getDatabase().product().insert(it)
            })
        })
    }

    fun emulate() {
        //product.value=Product()
    }


}