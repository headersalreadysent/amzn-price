package co.ec.amazonfiyattakip.ui.screen.find

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.AppDatabase
import co.ec.amazonfiyattakip.db.FireDB
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.utils.asyncRun
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.random.Random


open class FindViewModel : ViewModel() {

    val recorded = MutableLiveData<List<Product>>()

    private val searchFlow = MutableSharedFlow<Product>()
    val searchResults: SharedFlow<Product> = searchFlow

    init {
        viewModelScope.launch {

            val serverProducts=FireDB.collect()
            asyncRun({
                return@asyncRun AppDatabase.getDatabase().product().getAllAsin()
            }, { asins ->
                recorded.value = serverProducts
                    .filter { !asins.contains(it.asin) }
                    .sortedByDescending { it.date }
            })


        }
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