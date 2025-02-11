package co.ec.amazonfiyattakip

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.PrimaryKey
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.helper.Async
import co.ec.helper.utils.unix
import kotlin.random.Random

class AppModel : ViewModel() {

    var fabAction = MutableLiveData<Pair<ImageVector, () -> Unit>?>(Pair(Icons.Filled.Add, {}))

    companion object {
        @Volatile
        private var INSTANCE: AppModel? = null

        fun setFab(icon: ImageVector, action: () -> Unit) {
            INSTANCE?.fabAction?.value = Pair(icon, action)
        }

        fun noFab() {
            INSTANCE?.fabAction?.value = null
        }


    }

    init {
        INSTANCE = this
    }


    fun generateFakePrices(){
        Async.run({
            val products=AppDatabase.getDatabase().product().getAllProducts()
            (1..20).forEach { no ->
                products.forEach { it ->
                    val price=PriceInfo(
                        id=0,
                        productId = it.product.id,
                        asin = it.product.asin,
                        date = unix() - no * 86400,
                        price = (it.product.price.toFloat() + (if (Random.nextFloat() > .5F) +1 else -1) *
                                (Random.nextFloat() * .05F * it.product.price)).toInt(),
                        star = 0.0,
                        comment = 0,
                        priceChanged = 1,
                    )
                    AppDatabase.getDatabase().priceInfo().insert(price)
                }
            }

        })
    }
}