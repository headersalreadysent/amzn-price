package co.ec.amazonfiyattakip.ui.screen.detail


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductDao
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.helper.Async
import co.ec.helper.utils.unix
import com.fleeksoft.ksoup.KsoupEngineInstance.init
import kotlin.random.Random

open class DetailViewModel : ViewModel() {

    val product = MutableLiveData<Product>()
    val prices = MutableLiveData<List<PriceInfo>>()



    fun loadProduct(productId: Int) {
        Async.run({
            return@run Pair(
                AppDatabase.getDatabase().product().getProduct(productId),
                AppDatabase.getDatabase().priceInfo().getPricesByProduct(productId)
            )
        }, {
            product.value = it.first
            //if more than two point
            prices.value =  it.second
        })
    }

    fun stopFollow() {

        Async.run({
            val copy = product.value!!.copy(
                status = ProductStatus.PASSIVE
            )
            AppDatabase.getDatabase().product().update(copy)
            return@run copy
        }, {
            product.value = it
        })
    }

    fun startFollow() {
        Async.run({
            val copy = product.value!!.copy(
                status = ProductStatus.ACTIVE
            )
            AppDatabase.getDatabase().product().update(copy)
            return@run copy
        }, {
            product.value = it
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