package co.ec.amazonfiyattakip.ui.screen.detail


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.FireDB.ProductSync
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.helper.helpers.EventBus
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.launch
import kotlin.random.Random

open class DetailViewModel : ViewModel() {

    val product = MutableLiveData<Product>()
    val prices = MutableLiveData<List<PriceInfo>>()

    init {

        viewModelScope.launch {
            EventBus.subscribe<ProductSync> { update ->
                product.value?.let {
                    if(it.asin==update.asin){
                        loadProduct(it.id,false)
                    }
                }
            }
        }
    }


    fun loadProduct(productId: Int,refresh:Boolean=true) {
        asyncRun({
            return@asyncRun Pair(
                AppDatabase.getDatabase().product().getProduct(productId),
                AppDatabase.getDatabase().priceInfo().getPricesByProduct(productId)
            )
        }, {
            product.value = it.first
            //if more than two point
            prices.value =  it.second
            if(refresh){
                //refreshes value
                viewModelScope.launch {
                    FireDB.syncProduct(it.first)
                }
            }
        })
    }

    fun stopFollow() {

        asyncRun({
            val copy = product.value!!.copy(
                status = ProductStatus.PASSIVE
            )
            AppDatabase.getDatabase().product().update(copy)
            return@asyncRun copy
        }, {
            product.value = it
        })
    }

    fun startFollow() {
        asyncRun({
            val copy = product.value!!.copy(
                status = ProductStatus.ACTIVE
            )
            AppDatabase.getDatabase().product().update(copy)
            return@asyncRun copy
        }, {
            product.value = it
        })
    }

    fun deleteProduct(then: () -> Unit = {}){
        asyncRun({
            product.value?.let {
                AppDatabase.getDatabase().priceInfo().delete(it.id)
                AppDatabase.getDatabase().product().delete(it.id)
            }
            return@asyncRun
        },{
            then()
        })
    }


    fun activate(product: Product) {
        asyncRun({
            product.status=ProductStatus.ACTIVE
            return@asyncRun AppDatabase.getDatabase().product().update(product)
        },{
            loadProduct(product.id)
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