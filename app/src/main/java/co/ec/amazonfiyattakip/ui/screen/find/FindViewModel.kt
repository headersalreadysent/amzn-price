package co.ec.amazonfiyattakip.ui.screen.find

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.LogHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random


open class FindViewModel(isPreview: Boolean = false) : ViewModel() {

    val cache: CacheHelper? = if (!isPreview) CacheHelper.get() else null

    var searchKeyword = MutableLiveData<String>("")
    val oldSearches = MutableLiveData<List<String>>(listOf<String>())
    private val searchFlow = MutableSharedFlow<Product>()


    val searchResults: SharedFlow<Product> = searchFlow
    private val recordedResults = MutableLiveData<List<Product>>()

    private var dealLoadJob: Job? = null
    private val dealFlow = MutableSharedFlow<Product>()
    val deals: SharedFlow<Product> = dealFlow

    init {
        oldSearches.value =
            (cache?.get("oldSearches") ?: "").split("|").filter { it != "" }.distinct()
    }


    fun isSearchExists(searchExists: (keyword: String, list: List<Product>) -> Unit = { _, _ -> }) {
        //look older searches
        recordedResults.value?.let {
            if (it.isNotEmpty()) {
                val cachedKeyword = cache?.get("searchKeyword") ?: ""
                searchKeyword.value = cachedKeyword
                searchExists(cachedKeyword, it)
            }
            return
        }
        if (App.settings().getBoolean("showDealsInfo", true)) {
            loadDeals()
        }

    }

    /**
     * search scrape
     */
    private fun searchScraper(asins: List<String>) {
        val semaphore = Semaphore(10)
        viewModelScope.launch {
            channelFlow {
                asins.map { asin ->
                    async {
                        semaphore.withPermit {
                            runCatching { AmznScrape().suspendScrape(asin) }
                                .onSuccess { send(it) }
                        }
                    }
                }.forEach { it.await() }
            }.collect { result ->
                if (result.title.isNotEmpty() && result.price > 0) {
                    searchFlow.emit(result)
                    recordedResults.value = recordedResults.value.orEmpty() + result
                    LogHelper.d("search model recorded ${recordedResults.value?.size}")
                }
            }
        }
    }


    /**
     * load deals from amazon
     */
    fun search(keyword: String, asinCallback: (product: Product) -> Unit = {}) {
        dealLoadJob?.let {
            //cancel deals on search start
            it.cancel()
            dealLoadJob = null
        }
        val regex = Regex("^[A-Z0-9]{10}$")
        if (regex.matches(keyword)) {
            AmznScrape().scrapeFromAsin(keyword, { product ->
                //call asin callback. it will redirect to add
                cache?.put("storeProduct", product.encode(), 30)
                asinCallback(product)
                LogHelper.d("$product", "search")
            }, {
                startSearch(keyword)
            })
        } else {
            startSearch(keyword)
        }
    }

    private fun startSearch(keyword: String) {
        searchKeyword.value = keyword
        cache?.put("searchKeyword", keyword, 60 * 5)
        cache?.get("search-$keyword")?.let {
            return searchScraper(it.split("|"))
        }
        AmznScrape().search(keyword, { asins ->
            cache?.put("search-$keyword", asins.joinToString("|"), 60)
            val olds = (cache?.get("oldSearches") ?: "").split("|")
            val newList = listOf(keyword) + olds
            cache?.put("oldSearches", newList.distinct().joinToString("|"), 86400 * 100)
            searchScraper(asins)
        }) {
            App.snack("Arama sonucunda bir hata oluştu.")
        }
    }


    /**
     * load deals from amazon
     */
    private fun loadDeals() {
        val scraper: (List<String>) -> Unit = { asins ->
            val semaphore = Semaphore(10)
            dealLoadJob = viewModelScope.launch {
                channelFlow {
                    asins.map { asin ->
                        async {
                            semaphore.withPermit {
                                runCatching { AmznScrape().suspendScrape(asin) }
                                    .onSuccess { send(it) }
                            }
                        }
                    }.forEach { it.await() }
                }.collect { result ->
                    if (result.title.isNotEmpty() && result.price > 0) {
                        dealFlow.emit(result)
                    }
                }
            }
        }

        val old = cache?.get("latestDeals")
        if (old != null) {
            return scraper(old.split("|"))
        }
        AmznScrape().getPopular({ asins ->
            cache?.put("latestDeals", asins.joinToString("|"), 60 * 60)
            scraper(asins)
        })
    }

    /**
     * emulate datas for preview
     */
    fun emulate() {
        viewModelScope.launch {
            (1..12).forEach { _ ->
                searchFlow.emit(Product.fake())
                delay((Random.nextFloat() * 1000F).toLong())
            }
        }
    }

}