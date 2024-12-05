package co.ec.amazonfiyattakip.db.product

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.helper.utils.unix
import kotlinx.serialization.json.Json
import java.math.BigDecimal

enum class ProductStatus {
    PASSIVE,
    ACTIVE,
    DELETED,
    ERRORSTOP;
}

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
    var timeSpan: Int = 60,
    var errorCount: Int = 0,
    var status: ProductStatus = ProductStatus.ACTIVE
) {
    fun extraMap(): Map<String, String> {
        return Json.decodeFromString<Map<String, String>>(extras)
    }

    /**
     * convert product to price info object
     */
    fun toPriceInfo(productId: Int): PriceInfo {
        return PriceInfo(
            id = 0,
            productId = productId,
            asin = asin,
            date = unix(),
            price = price,
            star = star,
            comment = comment
        )

    }
}