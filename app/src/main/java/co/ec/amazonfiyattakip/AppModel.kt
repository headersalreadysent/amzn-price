package co.ec.amazonfiyattakip

import android.widget.Toast
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.launch
import kotlin.concurrent.thread
import kotlin.random.Random

class AppModel : ViewModel() {

    var fabAction = MutableLiveData<Pair<ImageVector, () -> Unit>?>(Pair(Icons.Filled.Add, {}))
    val cutCardContent = MutableLiveData<Pair<(@Composable () -> Unit), Modifier>>(null)

    companion object {
        @Volatile
        private var INSTANCE: AppModel? = null

        fun setFab(icon: ImageVector, action: () -> Unit) {
            INSTANCE?.fabAction?.value = Pair(icon, action)
        }

        fun noFab() {
            INSTANCE?.fabAction?.value = null
        }

        fun cutCard(
            modifier: Modifier = Modifier.aspectRatio(3F), content: @Composable () -> Unit
        ) {
            INSTANCE?.cutCardContent?.value = Pair(content, modifier)
        }
    }


    init {
        INSTANCE = this
    }


    fun generateFakePrices() {
        asyncRun({
            val products = AppDatabase.getDatabase().product().getAllProducts()
            (1..20).forEach { no ->
                products.forEach { it ->
                    val price = PriceInfo(
                        id = 0,
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