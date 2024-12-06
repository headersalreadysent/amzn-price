package co.ec.amazonfiyattakip.db

import androidx.room.Embedded
import androidx.room.Relation
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product

data class AsinId(
    var id:Int,
    var asin:String
)

data class ProductWithPrices(
    @Embedded val product: Product,
    @Relation(
        parentColumn = "id",
        entityColumn = "productId"
    )
    val priceInfoList: List<PriceInfo>
)