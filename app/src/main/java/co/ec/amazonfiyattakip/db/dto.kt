package co.ec.amazonfiyattakip.db

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.room.Embedded
import androidx.room.Relation
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.predictNextPrices

data class AsinId(
    var id: Int,
    var asin: String
)

data class ProductWithPrices(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val priceInfoList: List<PriceInfo>
) {

    fun predict(days: List<Int> = listOf(7, 14, 21, 28)): List<Double> {

        return predictNextPrices(
            priceInfoList.map { it.price.toDouble() },
            days = days
        )
    }
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