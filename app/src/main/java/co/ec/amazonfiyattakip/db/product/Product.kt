package co.ec.amazonfiyattakip.db.product

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.helper.utils.unix
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.NumberFormat
import java.util.Locale

enum class ProductStatus {
    PASSIVE,
    ACTIVE,
    DELETED,
    ERRORSTOP;
}

@Serializable
@Entity
data class Product(
    @PrimaryKey(autoGenerate = true) var id: Int,
    val asin: String = "",
    var date: Long,
    var title: String = "",
    var description: String = "",
    var price: Int = 0,
    var star: Double = 0.0,
    var comment: Int = 0,
    var image: String = "",
    var extras: String = "",
    var nextRunTime: Long = unix(),
    var timeSpan: Int = 3600,
    var errorCount: Int = 0,
    var status: ProductStatus = ProductStatus.ACTIVE
) {
    fun extraMap(): Map<String, String> {
        return Json.decodeFromString<Map<String, String>>(extras)
    }

    /**
     * convert product to price info object
     */
    fun toPriceInfo(productId: Int, latest: Int = 0): PriceInfo {
        return PriceInfo(
            id = 0,
            productId = productId,
            asin = asin,
            date = unix(),
            price = price,
            star = star,
            comment = comment,
            priceChanged = price - latest
        )

    }

    /**
     * write price as number format
     */
    fun price(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(price.toFloat() / 100F)
    }

    /**
     * return short desc
     */
    fun shortDesc(limit: Int = 50): String {
        if (description.length > limit) {
            return description.substring(0..limit) + "..."
        }
        return description
    }

    /**
     * return short desc
     */
    fun shortTitle(limit: Int = 50): String {
        if (title.length > limit) {
            return title.substring(0..limit) + "..."
        }
        return title
    }

    companion object {
        fun fake(): Product {
            return Product(
                id = 0,
                asin = "B09JR8K6HJ",
                date = 1728308992,
                title = "Apple AirPods (3. nesil) ve MagSafe Şarj Kutusu,",
                description = "Sesin etrafınızı sarmasını sağlayan, dinamik kafa izleme özellikli uzamsal ses teknolojisi Müziği kulağınızın şekline göre otomatik olarak ayarlayan Adaptif EQ Konturlu hatlara sahip yepyeni tasarım Eğlenceyi kolayca kontrol etmenize, gelen aramaları yanıtlamanıza veya sonlandırmanıza ve çok daha fazlasını yapmanıza imkan tanıyan kuvvet sensörü Tere ve suya dayanıklı tasarım Tek şarjla 6 saate kadar dinleme süresi MagSafe Şarj Kutusu ile toplamda 30 saate kadar dinleme süresi “Hey Siri” diye seslenerek Siri’ye hızlı erişim Sihirli bir deneyim için zahmetsiz kurulum, kulağa takılı olduğunu algılama ve otomatik geçiş özellikleri Aksesuarlar ayrı satılır. Apple Music için abonelik gerekir. Daha fazla göster › Daha fazla ürün bilgisi",
                price = 661868,
                star = 4.5,
                comment = 1793,
                image = "https://m.media-amazon.com/images/I/61Z5J-fq7KL.__AC_SY445_SX342_QL70_ML2_.jpg",
                extras = "{\"Uyumlu Cihazlar\":\"Müzik Çalar\",\"Konnektör Türü\":\"Kablosuz\",\"Renk\":\"beyaz\",\"Marka\":\"Apple\",\"Ürün Ağırlığı\":\"0.18 Kilogram\"}"
            )
        }

        fun empty(): Product {
            return Product(
                id = 0,
                asin = "",
                date = 0,
                title = "",
                description = "",
                price = 0,
                star = 0.0,
                comment = 0,
                image = "",
                extras = "{}"
            )
        }

        fun decode(string: String): Product {
            return Json.decodeFromString<Product>(string)
        }


    }

    fun encode(): String {
        return try {
            Json.encodeToString(this)
        } catch (e: Exception) {
            Log.e("SerializationError", "Error serializing Product: ${e.message}", e)
            ""
        }
    }
}