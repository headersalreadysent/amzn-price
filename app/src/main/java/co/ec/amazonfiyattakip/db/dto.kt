package co.ec.amazonfiyattakip.db

import androidx.room.Embedded
import androidx.room.Relation
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product


data class ProductWithPrices(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val priceInfoList: List<PriceInfo>
) {


}

/**
 * dailt basket totals
 */
data class DailyTotal(val date: Long, val total: Long)

/**
 * latest update with product
 */
data class LatestUpdate(
    val productId: Int,
    val date: Long,
    val price: Int,
    val title: String,
    val image: String
)

/**
 * low priced products
 */
data class LowPriced(
    val id: Long,
    val title: String,
    val image: String,
    val price: Int,
    val avg: Int
)