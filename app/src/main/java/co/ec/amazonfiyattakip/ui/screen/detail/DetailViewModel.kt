package co.ec.amazonfiyattakip.ui.screen.detail


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.FireDB.ProductSync
import co.ec.amazonfiyattakip.db.noprice.NoPriceDao
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.service.job.PriceUpdate
import co.ec.helper.helpers.EventBus
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

open class DetailViewModel : ViewModel() {

    val product = MutableLiveData<Product>()
    val prices = MutableLiveData<List<PriceInfo>>()
    val noPriceControl = MutableLiveData<NoPriceDao.NoPriceControl>()

    init {
        viewModelScope.launch {
            EventBus.subscribe<ProductSync> { update ->
                product.value?.let {
                    if (product.value?.asin == update.asin) {
                        loadProduct(it.id, false)
                    }
                }
            }
        }
    }

    //load product
    fun loadProduct(productId: Int, refresh: Boolean = true) {
        viewModelScope.launch {
            val productData = withContext(Dispatchers.IO) {
                Triple(
                    AppDatabase.getDatabase().product().getProduct(productId),
                    AppDatabase.getDatabase().priceInfo().getPricesByProduct(productId),
                    AppDatabase.getDatabase().noPrice().controlProduct(productId)
                )
            }
            product.value = productData.first
            prices.value = productData.second
            productData.third?.let {
                if(it.count>0){
                    noPriceControl.value=it
                }
            }


            if (refresh) {
                withContext(Dispatchers.IO) {
                    FireDB.syncProduct(productData.first)
                }
            }
        }
    }


    fun changeStatus(status: ProductStatus) {
        asyncRun({
            val copy = product.value!!.copy(
                status = status
            )

            App.event(
                "product_change_status", mapOf(
                    "productAsin" to copy.asin,
                    "status" to copy.status.toString()
                )
            )
            AppDatabase.getDatabase().product().update(copy)
            return@asyncRun copy
        }, {
            product.value = it
        })

    }

    fun deleteProduct(then: () -> Unit = {}) {
        asyncRun({
            product.value?.let {
                App.event(
                    "product_delete", mapOf(
                        "productAsin" to it.asin,
                    )
                )
                AppDatabase.getDatabase().priceInfo().delete(it.id)
                AppDatabase.getDatabase().product().delete(it.id)
            }
            return@asyncRun
        }, {
            then()
        })
    }

    /**
     * update time span
     */
    fun updateTimeSpan(minute: Int) {
        asyncRun({
            product.value?.let {
                val newProduct = it.copy(
                    timeSpan = minute * 60
                )
                AppDatabase.getDatabase().product().update(newProduct)
                viewModelScope.launch {
                    product.value = newProduct
                }
            }
            return@asyncRun product.value
        }, {
            loadProduct(it?.id ?: 0)
        })
    }

    /**
     * refresh product with pull
     */
    fun refreshProduct(then: () -> Unit = {}, err: (e: Throwable) -> Unit = {}) {
        product.value?.let { product ->
            viewModelScope.launch {
                try {
                    val update = PriceUpdate.collectProduct(product)
                    loadProduct(product.id)
                    then()
                } catch (e: Throwable) {
                    err(e)
                }
            }
        }
    }


    fun emulate() {
        //generate fake products
        product.value = Product.fake()
        prices.value = (0..30).map {
            val price = Random.nextFloat() * 20000 + product.value?.price!!
            PriceInfo(
                id = it,
                productId = 1,
                asin = product.value?.asin ?: "",
                date = unix() - (10 - it) * 86400,
                price = price.toInt()
            )
        }
        noPriceControl.value = NoPriceDao.NoPriceControl(20,unix())
    }


}