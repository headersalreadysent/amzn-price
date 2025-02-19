package co.ec.amazonfiyattakip.ui.screen.find

import androidx.core.app.PendingIntentCompat.send
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.utils.unix
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlin.random.Random


open class FindViewModel : ViewModel() {


    private val searchFlow = MutableSharedFlow<Product>()
    val searchResults: SharedFlow<Product> = searchFlow


    /**
     * load deals from amazon
     */
    fun search(text: String, then: (list: List<String>) -> Unit = {}) {
        AmznScrape().search(text, { asins ->
            viewModelScope.launch {
                channelFlow {
                    asins.chunked(10).forEach { asinList ->
                        asinList.forEach { asin ->
                            launch {
                                runCatching { AmznScrape().suspendScrape(asin) }
                                    .onSuccess { send(it) }
                            }
                        }
                        delay(20*1000)
                    }
                }.collect { result ->
                    searchFlow.emit(result)
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
                delay((Random.nextFloat()*1000F).toLong())
            }
        }
    }

}