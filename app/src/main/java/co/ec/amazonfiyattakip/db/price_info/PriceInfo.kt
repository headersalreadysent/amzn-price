package co.ec.amazonfiyattakip.db.price_info

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.product.Product

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
data class PriceInfo(
    @PrimaryKey(autoGenerate = true) var id: Int,
    val productId: Int,
    val asin: String = "",
    var date: Long,
    var price: Int = 0,
    var star: Double = 0.0,
    var comment: Int = 0,
)