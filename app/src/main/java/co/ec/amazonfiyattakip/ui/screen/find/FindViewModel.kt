package co.ec.amazonfiyattakip.ui.screen.find

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.SharedCache
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.LogHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random


open class FindViewModel(isPreview: Boolean = false) : ViewModel() {


    private val searchFlow = MutableSharedFlow<Product>()
    val searchResults: SharedFlow<Product> = searchFlow
    private val recordedResults = MutableLiveData<List<Product>>()

    private val dealFlow = MutableSharedFlow<Product>()
    val deals: SharedFlow<Product> = dealFlow

    val cache: SharedCache? = if (!isPreview) SharedCache(App.context()) else null

    init {
        LogHelper.d("search model init ${searchFlow}")
    }


    fun startAction(searchExists: (list: List<Product>, search: String) -> Unit = {_,_ ->}) {
        //look older searches
        val list = recordedResults.value.orEmpty()
        if (list.isNotEmpty()) {
            searchExists(list, cache?.get("searchKeyword") ?: "")
            LogHelper.d("search model load exits ${list.size}")
        } else {
            LogHelper.d("search model not exits load deals")
            loadDeals()
        }
    }


    /**
     * load deals from amazon
     */
    private fun loadDeals() {
        val scraper: (List<String>) -> Unit = { asins ->
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
     * load deals from amazon
     */
    fun search(text: String) {
        cache?.put("searchKeyword", text, 60 * 5)
        val scraper: (List<String>) -> Unit = { asins ->
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
        //first look cache
        val old = cache?.get("search-$text")
        if (old != null) {
            return scraper(old.split("|"))
        }
        AmznScrape().search(text, { asins ->
            cache?.put("search-$text", asins.joinToString("|"), 60)
            scraper(asins)
        }) {
            App.snack("Arama sonucunda bir hata oluştu.")
        }
    }

    /**
     * emulate datas for preview
     */
    fun emulate() {
        viewModelScope.launch {
            (1..12).forEach {
                searchFlow.emit(Product.fake())
                delay((Random.nextFloat() * 1000F).toLong())
            }
        }
    }

}