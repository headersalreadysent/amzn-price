package co.ec.amazonfiyattakip.db.product

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.json.Json
import java.math.BigDecimal

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
    var extras: String = ""
) {
    fun getExtras(): Map<String, String> {
        return Json.decodeFromString<Map<String, String>>(extras)
    }
}