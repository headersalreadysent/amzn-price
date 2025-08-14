package co.ec.amazonfiyattakip.ui.screen.find

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.LogHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlin.random.Random


open class FindViewModel(isPreview: Boolean = false) : ViewModel() {

    val cache: CacheHelper? = if (!isPreview) CacheHelper.get() else null

    var searchKeyword = MutableLiveData("")
    val oldSearches = MutableLiveData(listOf<String>())
    private val searchFlow = MutableSharedFlow<Product>()


    val searchResults: SharedFlow<Product> = searchFlow
    private val recordedResults = MutableLiveData<List<Product>>()

    private val dealFlow = MutableSharedFlow<Product>()
    val bestsellers: SharedFlow<Product> = dealFlow

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
            loadBestsellers()
        }

    }

    /**
     * load deals from amazon
     */
    fun search(keyword: String, asinCallback: (product: Product) -> Unit = {}) {
        if (Regex("^[A-Z0-9]{10}$").matches(keyword)) {
            //it is like a asin
            AmznScrape(withCache = true).scrape(keyword, { product ->
                asinCallback(product)
                LogHelper.d("$product", "search")
            }, {
                startSearch(keyword)
            })
        } else {
            startSearch(keyword)
        }
    }

    /**
     * start searching on amzn
     */
    private fun startSearch(keyword: String) {
        searchKeyword.value = keyword
        cache?.put("searchKeyword", keyword, 60 * 5)
        val amznScrape = AmznScrape().cache()
        amznScrape.search(keyword, { asins ->
            //searc on amzn
            val olds = cache?.get("oldSearches")?.split("|") ?: emptyList()
            cache?.put(
                "oldSearches", (listOf(keyword) + olds).distinct().joinToString("|"), 86400 * 100
            )
            recordedResults.value = asins
            //scrape to flow
            CoroutineScope(Dispatchers.Main).launch {
                asins.forEach {
                    delay((Random.nextFloat() * 100F).toLong())
                    searchFlow.emit(it)
                }
            }

        }) {
            App.snack("Arama sonucunda bir hata oluştu.")
        }
    }


    /**
     * load deals from amazon
     */
    private fun loadBestsellers() {
        val amznScrape = AmznScrape().cache()
        amznScrape.bestsellers({ asins ->
            //show on deal screen
            CoroutineScope(Dispatchers.Main).launch {
                asins.forEach {
                    delay((Random.nextFloat() * 100F).toLong())
                    dealFlow.emit(it)
                }
            }

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