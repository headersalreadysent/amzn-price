package co.ec.amazonfiyattakip.ui.screen.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.Async
import co.ec.helper.utils.unix
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random

open class MainScreenModel : ViewModel() {

    val products = MutableLiveData<List<ProductWithPrices>>()
    var dailyTotals = MutableLiveData<List<DailyTotal>>()
    var latestUpdates: StateFlow<List<LatestUpdate>>? = null

    var stats = MutableLiveData<Map<String, Int>>()

    private val dealFlow = MutableSharedFlow<Product>()
    val deals: SharedFlow<Product> = dealFlow


    init {
        loadProducts()
        loadDailyTotals()
        loadLatestUpdates()
        calculateStats()
    }

    private fun calculateStats() {
        Async.run({
            return@run mapOf(
                "product" to AppDatabase.getDatabase().product().getCount(),
                "update" to AppDatabase.getDatabase().priceInfo().getCount()
            )
        }, {
            stats.value = it
        })
    }


    /**
     * load daily updates
     */
    private fun loadLatestUpdates() {
        try {
            latestUpdates = AppDatabase.getDatabase().priceInfo().getLatestUpdates()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } catch (_: Throwable) {

        }

    }

    /**
     * load daily stats
     */
    private fun loadDailyTotals(format: String = "%Y-%m-%d") {
        Async.run({
            return@run AppDatabase.getDatabase().priceInfo().getDailyTotalPrices(format = format)
        }, {
            dailyTotals.value = it
        })
    }

    /**
     * load products from database
     */
    private fun loadProducts() {
        Async.run({
            return@run AppDatabase.getDatabase().product().getAllProducts()
        }, {
            products.value = it
        })
    }

    /**
     * load deals from amazon
     */
    fun loadDeals(then: (list: List<String>) -> Unit = {}) {
        val semaphore = Semaphore(10)

        AmznScrape().getPopular({ asins ->
            viewModelScope.launch {
                channelFlow {
                    asins.forEach { asin ->
                        launch {
                            semaphore.withPermit {
                                runCatching { AmznScrape().suspendScrape(asin) }
                                    .onSuccess { send(it) }
                            }
                        }
                    }
                }.collect { result ->
                    if (result.title.isNotEmpty() && result.price > 0) {
                        dealFlow.emit(result)
                    }
                }
            }
            then(asins)
        })
    }

    /**
     * add product list
     */
    fun addProductList(list: List<Product>) {
        Async.run({
            return@run AppDatabase.getDatabase().product().insertAll(list)
        }, {
            loadProducts()
        })
    }


    /**
     * emulate datas for preview
     */
    fun emulate() {
        //generate fake products
        val fake = Product.fake()
        products.value = (1..5).map {
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
        viewModelScope.launch {
            (1..12).forEach {
                dealFlow.emit(fake)
                delay((Random.nextFloat() * 1000F).toLong())
            }
        }
        var firstPrice = 2000L
        dailyTotals.value = (1..30).map {
            firstPrice += (Random.nextFloat() * 400).toLong()
            DailyTotal(date = unix() - (30 - it) * 86400, firstPrice)
        }

        val list = (1..30).map {
            return@map LatestUpdate(
                productId = 0,
                date = unix() - it * 60 * 60,
                price = 2000 + (if (Random.nextFloat() > 0.5F) 1 else -1) + (Random.nextFloat() * 200L).toInt(),
                title = fake.title,
                image = fake.image
            )
        }

        latestUpdates = MutableStateFlow<List<LatestUpdate>>(emptyList())
        viewModelScope.launch {
            (latestUpdates as MutableStateFlow<List<LatestUpdate>>).emit(list)
        }
    }
}