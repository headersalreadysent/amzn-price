package co.ec.amazonfiyattakip.ui.screen.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.Async
import co.ec.helper.utils.unix
import kotlin.random.Random
import kotlin.random.nextUInt

open class MainScreenModel : ViewModel() {

    val products = MutableLiveData<List<ProductWithPrices>>()

    init {
        loadProducts()
    }

    fun loadProducts() {
        Async.run({
            return@run AppDatabase.getDatabase().product().getAllProducts()
        }, {
            products.value = it
        })
    }

    fun emulate() {
        //generate fake products
        products.value = (1..15).map {
            val fake = Product.fake()
            return@map ProductWithPrices(product = fake, priceInfoList = (0..10).map {
                var price=Random.nextFloat()*200+2500
                PriceInfo(
                    id = it,
                    productId = 1,
                    asin = fake.asin,
                    date = unix() - (10 - it) * 86400,
                    price = price.toInt()
                )
            })
        }
    }

}