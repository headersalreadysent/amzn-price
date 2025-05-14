package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.product.Product
import kotlinx.serialization.Serializable
import java.text.NumberFormat
import java.util.Locale

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE // Optional: Handle deletion
        )
    ],
    indices = [
        Index(value = ["productId", "date"], unique = true)
    ]
)
@Serializable
data class PriceInfo(
    @PrimaryKey(autoGenerate = true) var id: Int,
    val productId: Int,
    val asin: String = "",
    var date: Long,
    var price: Int = 0,
    var star: Double = 0.0,
    var comment: Int = 0,
    val priceChanged: Int = 0
) {
    fun price(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        return format.format(price.toFloat() / 100F)
    }
}