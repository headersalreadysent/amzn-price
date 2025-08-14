package co.ec.amazonfiyattakip.db.noprice

import androidx.room.Entity
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class NoPrice(
    @PrimaryKey(autoGenerate = true) var id: Int,
    val asin: String = "",
    var date: Long,
    var productId: Int = 0,
    var active: Boolean = true
) {
    companion object {
        fun add(asin: String, productId: Int) {
            asyncRun({
                val dao = AppDatabase.getDatabase().noPrice()
                dao.insert(NoPrice(0, asin, unix(), productId))
            })
        }

        fun has(productId: Int, then: (has: Boolean) -> Unit = {}) {
            asyncRun({
                val dao = AppDatabase.getDatabase().noPrice()
                return@asyncRun dao.getForProduct(productId).isNotEmpty()
            }, then)
        }

        fun remove(productId: Int) {
            asyncRun({
                val dao = AppDatabase.getDatabase().noPrice()
                dao.deleteByProductId(productId)
            })
        }
    }
}