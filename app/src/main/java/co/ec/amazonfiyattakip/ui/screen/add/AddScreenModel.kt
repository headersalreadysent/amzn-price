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
        val url = AppSharedSettings.get().getString("sharedUrl") ?: ""
        if (url != "") {
            AmznScrape().scrapeFromUrl(url, { scraped ->
                AppLogger.d(scraped.toString())
                product.value = scraped
                AppLogger.d(scraped.toString())
            }, {
                it.printStackTrace()
            })
        } else {
            //Toast.makeText(App.context(), "Url bulunamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun recordProductFromAsin(asin: String) {

        AmznScrape().scrapeFromAsin(asin, {
            Async.run({
                AppDatabase.getDatabase().product().insert(it)
            })
        })
    }

    fun saveProductToDatabase() {
        product.value?.let {
            Async.run({
                return@run AppDatabase.getDatabase().product().insert(it)
            }, { id ->
                product.value = it.copy(
                    id = id.toInt()
                )
            })
        }

    }

    fun emulate() {
        product.value = Product(
            id = 0,
            asin = "B09JR8K6HJ",
            date = 1728308992,
            title = "Apple AirPods (3. nesil) ve MagSafe Şarj Kutusu, description=Sesin etrafınızı sarmasını sağlayan, dinamik kafa izleme özellikli uzamsal ses teknolojisi Müziği kulağınızın şekline göre otomatik olarak ayarlayan Adaptif EQ Konturlu hatlara sahip yepyeni tasarım Eğlenceyi kolayca kontrol etmenize, gelen aramaları yanıtlamanıza veya sonlandırmanıza ve çok daha fazlasını yapmanıza imkan tanıyan kuvvet sensörü Tere ve suya dayanıklı tasarım Tek şarjla 6 saate kadar dinleme süresi MagSafe Şarj Kutusu ile toplamda 30 saate kadar dinleme süresi “Hey Siri” diye seslenerek Siri’ye hızlı erişim Sihirli bir deneyim için zahmetsiz kurulum, kulağa takılı olduğunu algılama ve otomatik geçiş özellikleri Aksesuarlar ayrı satılır. Apple Music için abonelik gerekir. Daha fazla göster › Daha fazla ürün bilgisi",
            price = 661868,
            star = 4.5,
            comment = 1793,
            image = "https://m.media-amazon.com/images/I/61Z5J-fq7KL.__AC_SY445_SX342_QL70_ML2_.jpg",
            extras = "{\"Uyumlu Cihazlar\":\"Müzik Çalar\",\"Konnektör Türü\":\"Kablosuz\",\"Renk\":\"beyaz\",\"Marka\":\"Apple\",\"Ürün Ağırlığı\":\"0.18 Kilogram\"}"
        )
    }


}