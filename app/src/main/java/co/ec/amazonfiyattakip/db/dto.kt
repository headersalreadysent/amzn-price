package co.ec.amazonfiyattakip.db

import androidx.room.Embedded
import androidx.room.Relation
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import kotlinx.serialization.Serializable

@Serializable
data class ProductWithPrices(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val priceInfoList: List<PriceInfo>
)

@Serializable
data class BackupData(
    val products: List<Product>,
    val priceInfos: List<PriceInfo>
)

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

data class ProductWithStat(
    @Embedded val entity: Product,
    val min: Int,
    val avg: Int,
    val max: Int
)

@Serializable
data class AjaxResponse(
    val ASIN: String,
    val Type: String,
    val sortOfferInfo: String,
    val isPrimeEligible: String,
    val Value: AjaxPriceValue
)

@Serializable
data class AjaxPriceValue(
    val content: AjaxPriceContent
)

@Serializable
data class AjaxPriceContent(
    val twisterSlotJson: TwisterSlotJson,
    val twisterSlotDiv: String
)

@Serializable
data class TwisterSlotJson(
    val price: String
)