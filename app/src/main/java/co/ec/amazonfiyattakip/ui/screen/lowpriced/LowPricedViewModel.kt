package co.ec.amazonfiyattakip.ui.screen.lowpriced

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.ProductWithStat
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.asyncRun
import kotlin.random.Random


open class LowPricedViewModel() : ViewModel() {


    val list = MutableLiveData<List<ProductWithStat>>(listOf<ProductWithStat>())

    val noPriceProduct = MutableLiveData<List<Product>>(listOf<Product>())


    fun productStats() {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().priceInfo().priceStat()
        }, {
            list.value = it
        }, {
            LogHelper.e(it.message.toString(), it)
        })
    }

    fun noPriceProduct() {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().noPrice().getProducts()
        }, {
            noPriceProduct.value = it
        }, {
            LogHelper.e(it.message.toString(), it)
        })
    }

    /**
     * emulate datas for preview
     */
    fun emulate() {
        list.value = (1..10).map {
            val min = Random.nextInt(10000)
            val max = Random.nextInt(100000)
            var ave = Random.nextInt(45000, 68000)
            ProductWithStat(
                entity = Product.fake().copy(
                    price = Random.nextInt(min, max)
                ),
                min = min,
                avg = ave,
                max = max
            )
        }
    }

}