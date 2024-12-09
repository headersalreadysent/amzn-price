package co.ec.amazonfiyattakip.ui.screen.detail


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.helper.Async
import co.ec.helper.utils.unix
import kotlin.random.Random

open class DetailViewModel : ViewModel() {

    val product = MutableLiveData<Product>()
    val prices = MutableLiveData<List<PriceInfo>>()

    init {
    }

    fun loadProduct(productId: Int) {
        Async.run({
            return@run Pair(
                AppDatabase.getDatabase().product().getProduct(productId),
                AppDatabase.getDatabase().priceInfo().getPricesByProduct(productId)
            )
        }, {
            product.value = it.first
            prices.value = it.second
        })
    }

    fun stopFallowProduct() {
        Async.run({
            product.value?.let {
                val copy = it.copy(
                    status = ProductStatus.PASSIVE
                )
                return@run AppDatabase.getDatabase().product().update(copy)
            }
            return@run null
        }, {
            product.value = product.value?.let {
                return@let it.copy(
                    status = ProductStatus.PASSIVE
                )
            }
        })
    }

    fun emulate() {
        //generate fake products
        product.value = Product.fake()
        prices.value = (0..10).map {
            val price = Random.nextFloat() * 20000 + product.value?.price!!
            PriceInfo(
                id = it,
                productId = 1,
                asin = product.value?.asin ?: "",
                date = unix() - (10 - it) * 86400,
                price = price.toInt()
            )
        }
    }

}