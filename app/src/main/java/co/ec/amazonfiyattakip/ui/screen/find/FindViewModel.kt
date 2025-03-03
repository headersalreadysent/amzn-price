package co.ec.amazonfiyattakip.ui.screen.find

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random


open class FindViewModel : ViewModel() {


    private val searchFlow = MutableSharedFlow<Product>()
    val searchResults: SharedFlow<Product> = searchFlow

    private val dealFlow = MutableSharedFlow<Product>()
    val deals: SharedFlow<Product> = dealFlow



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
     * load deals from amazon
     */
    fun search(text: String, then: (list: List<String>) -> Unit = {}) {
        AmznScrape().search(text, { asins ->
            val semaphore = Semaphore(10)
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
                        searchFlow.emit(result)
                    }
                }
            }
            then(asins)
        })
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