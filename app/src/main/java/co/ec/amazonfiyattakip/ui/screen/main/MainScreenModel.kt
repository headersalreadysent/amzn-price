package co.ec.amazonfiyattakip.ui.screen.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.LowPriced
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.random.Random

open class MainScreenModel : ViewModel() {

    val products = MutableLiveData<List<ProductWithPrices>>()
    var dailyTotals = MutableLiveData<List<DailyTotal>>()
    var lowPriced = MutableLiveData<List<LowPriced>>()
    val serverProducts = MutableLiveData<List<Pair<Product, List<String>>>>()
    val settings = MutableLiveData<Map<String, Any>>(mapOf<String, Any>())

    var stats = MutableLiveData<Map<String, Int>>()


    val cache: CacheHelper? = CacheHelper.get()

    init {
        cache?.get("allProducts")?.let {
            try {
                var productData = Json.decodeFromString<List<ProductWithPrices>>(it)
                products.value = productData
                LogHelper.i("MainScreen cache read ${productData.size} product")
            } catch (e: Throwable) {
                LogHelper.e("MainScreen cache error ${e.message}")
            }
        }
    }

    /**
     * load products from database
     */
    fun loadProducts() {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().product().getAllProducts()
        }, {
            products.value = it
            cache?.put("allProducts", Json.encodeToString(it), 43200)
            if (it.isNotEmpty()) {
                //if exists
                loadDailyTotals()
                loadLowPriced()
                calculateStats()
            }
        })
    }


    /**
     * load daily stats
     */
    private fun loadDailyTotals() {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().priceInfo().getDailyAverages()
        }, {
            dailyTotals.value = it
        })
    }


    /**
     * load products from database
     */
    private fun loadLowPriced() {
        asyncRun({
            return@asyncRun AppDatabase.getDatabase().priceInfo().lowPricedProducts()
        }, {
            lowPriced.value = it
        })
    }


    private fun calculateStats() {
        asyncRun({
            return@asyncRun mapOf(
                "product" to AppDatabase.getDatabase().product().getCount(),
                "update" to AppDatabase.getDatabase().priceInfo().getCount(),
                "querySpan" to AppDatabase.getDatabase().jobLog().getQuerySpan().toInt()
            )
        }, {
            stats.value = it
        })
    }


    /**
     * load populer count and cache it then
     */
    fun getPopularCount(then: (count: Int) -> Unit = {}) {
        cache?.get("popularCount")?.let {
            then(it.toInt())
        }
        AmznScrape(withCache = true).bestsellers({ asins ->
            then(asins.size)
            cache?.put("popularCount", asins.size.toString())
        })
    }

    /**
     * get server products
     */
    fun collectServerProducts() {
        cache?.get("serverProducts")?.let {
            serverProducts.value = Json.decodeFromString<List<Pair<Product, List<String>>>>(it)
        }
        viewModelScope.launch {
            val firebase = FireDB.collectWithPriceCount()
            asyncRun({
                return@asyncRun AppDatabase.getDatabase().product().getAllAsin()
            }, { asins ->
                serverProducts.value = firebase.filter { !asins.contains(it.first.asin) }
                    .sortedByDescending { it.first.date }
                cache?.put("serverProducts", Json.encodeToString(serverProducts.value), 60 * 60)
            })
        }
    }


    /**
     * emulate datas for preview
     */
    fun emulate() {
        //generate fake products
        val fake = Product.fake()
        products.value = (1..36).map {
            return@map ProductWithPrices(product = fake, priceInfoList = (0..10).map {
                var price = Random.nextFloat() * 200 + 2500
                PriceInfo(
                    id = it,
                    productId = 1,
                    asin = fake.asin,
                    date = unix() - (10 - it) * 86400,
                    price = price.toInt()
                )
            })
        }

        var firstPrice = 2000L
        dailyTotals.value = (1..30).map {
            firstPrice += (Random.nextFloat() * 400).toLong()
            DailyTotal(date = unix() - (30 - it) * 86400, firstPrice)
        }
        serverProducts.value = List(20) {
            val price = Random.nextInt(50, 100)
            val priceCount = Random.nextInt(50, 100)
            val prices = List(priceCount) {
                (unix() - (priceCount - it) * 86400).toString() + "|" + (price + Random.nextInt(
                    -5,
                    5
                )).toString() + "|0|0"
            }
            Pair(Product.fake(), prices)
        }
    }
}