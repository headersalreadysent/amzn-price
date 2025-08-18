package co.ec.amazonfiyattakip.service

import android.os.SystemClock
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.db.AjaxResponse
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.helper.helpers.CacheHelper
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.google.firebase.Firebase
import com.google.firebase.perf.performance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AmznScrape(withCache: Boolean = false) {

    private var cacheTime: Int = 60 * 60
    private var cacheHelper: CacheHelper? = null

    private val json = Json {
        ignoreUnknownKeys = true

    }

    companion object {
        private const val DETAIL_PAGE_URL = "https://www.amazon.com.tr/_title_/dp/_asin_"
        fun urlFromAsin(asin: String, title: String? = null): String {
            return DETAIL_PAGE_URL.replace("_asin_", asin).replace("_title_", title ?: asin)
        }
    }

    init {
        if (withCache) {
            this.cache()
        }
    }

    /**
     * activate cache
     */
    fun cache(ttl: Int? = null): AmznScrape {
        cacheHelper = CacheHelper.get()
        ttl?.let {
            cacheTime = ttl
        }
        return this
    }


    /**
     * scrape data in back thread
     * @param url url of amazon
     * @return Product
     */
    suspend fun suspendScrape(url: String): Product {
        val trace = Firebase.performance.newTrace("AMZNScrapeTrace")
        trace.start()
        return withContext(Dispatchers.IO) {
            val pageUrl = if (url.startsWith("http")) url else urlFromAsin(url)
            // Check cache first
            cacheHelper?.get("http-$pageUrl")?.let { cachedData ->
                return@withContext Product.decode(cachedData)
            }

            val startTime = SystemClock.elapsedRealtime()
            // Make request and cache the result
            val response = AmznRequest.suspendRequest(pageUrl)
            val product = extractProductDetails(response)
            // Cache the new product data
            cacheHelper?.put("product-$pageUrl", product.encode(), cacheTime)
            val duration = SystemClock.elapsedRealtime() - startTime
            App.event(
                "product_scrape", mapOf(
                    "duration" to duration, "asin" to product.asin
                )
            )

            trace.stop()
            product
        }
    }

    /**
     * scrape product from url
     * @param url url of amazon
     * @param then result callback
     * @param err error callback
     */
    fun scrape(
        urlOrAsin: String,
        then: (res: Product) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        return runBlocking {
            try {
                val product = suspendScrape(urlOrAsin)
                LogHelper.d("AMZN-Scrape Success ${product.encode()}")
                then(product)
            } catch (e: Exception) {
                LogHelper.e("AMZN-Scrape error ${e.message}", e)
                err(e)
                null
            }
        }
    }

    /**
     * get popular
     * @param then result callback
     * @param err error callback
     */
    fun bestsellers(
        then: (res: List<Product>) -> Unit = { _ -> }, err: (res: Throwable) -> Unit = { _ -> }
    ) {
        cacheHelper?.get("popular-asin")?.let { cachedData ->
            return then(cachedData.split("#-#").map { Product.decode(it) })
        }

        val trace = Firebase.performance.newTrace("AMZNPopularTrace")
        trace.start()
        asyncRun({
            //generate url
            AmznRequest.suspendRequest("https://www.amazon.com.tr/gp/bestsellers")
        }, { html ->
            //get html
            try {
                //parse product from html
                val asins = extractPopularProducts(html)
                LogHelper.d("AMZN-Popular Success $asins")
                then(asins)
                trace.stop()
                cacheHelper?.put(
                    "popular-asin", asins.joinToString("#-#") { it.encode() }, cacheTime
                )

                cacheHelper?.let {
                    //only cache 5 second
                    AmznScrape().cache(5 * 60 * 60).cacheList(asins)
                }
            } catch (t: Throwable) {
                err(t)
            }
        }, {
            LogHelper.e("AMZN-Popular Error ${it.message}", it)
            err(it)
        })

    }

    /**
     * get popular
     * @param then result callback
     * @param err error callback
     */
    fun search(
        searchText: String,
        then: (res: List<Product>) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        val encodedKeyword = URLEncoder.encode(searchText, StandardCharsets.UTF_8.toString())
        cacheHelper?.get("search-prod-$encodedKeyword")?.let { cachedData ->
            return then(cachedData.split("#-#").map { Product.decode(it) })
        }
        val trace = Firebase.performance.newTrace("AMZNSearchTrace")
        trace.start()
        asyncRun({
            //generate url
            AmznRequest.suspendRequest("https://www.amazon.com.tr/s?k=\"$encodedKeyword\"")
        }, { html ->
            try {
                //parse product from html
                val searchAsins = extractSearchResults(html)
                then(searchAsins)
                LogHelper.d("AMZN-Search Success $searchAsins")
                trace.stop()
                cacheHelper?.put(
                    "search-prod-$encodedKeyword",
                    searchAsins.joinToString("#-#") { it.encode() },
                    cacheTime
                )
                cacheHelper?.let {
                    cacheList(searchAsins)
                }

            } catch (t: Throwable) {
                err(t)
            }
        }, {
            LogHelper.e("AMZN-Search Error ${it.message}", it)
            err(it)
        })

    }

    /**
     * scrape list from flow
     * @param asins asin list to scrape
     * @param sharedFlow flow reference value
     * @param size semaphore size
     * @return Job
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flowScrape(
        asins: List<String>, sharedFlow: MutableSharedFlow<Product>, size: Int = 15
    ): Job {
        return CoroutineScope(Dispatchers.IO).launch {
            asins.asFlow().flatMapMerge(size) { asin ->
                LogHelper.d("AMZN-Flow-Scrape $asin")
                flow {
                    try {
                        //scrape suspended
                        val result = suspendScrape(asin)
                        if (result.title.isNotEmpty() && result.price > 0) {
                            //if it is sutiable for showing
                            emit(result)
                            LogHelper.d("AMZN-Flow-Scrape ${result.encode()}")
                        }
                    } catch (_: Throwable) {

                    }
                }
            }.collect { product ->
                sharedFlow.emit(product)
            }
        }
    }

    /**
     * extract price from ajax
     */
    suspend fun collectAjaxPrice(asin: String): AjaxResponse? {
        val trace = Firebase.performance.newTrace("AMZNPriceAjaxTrace")
        trace.start()
        return try {
            //get ajax response
            val responseStr = AmznRequest.suspendRequest(
                "https://www.amazon.com.tr/gp/product/ajax?isDimensionSlotsAjax=1&asinList=$asin&experienceId=twisterDimensionSlotsDefault&asin=$asin"
            )
            LogHelper.d("AMZN-Scrape Ajax Price Success ${responseStr.trim()}")
            //parse it
            trace.stop()
            json.decodeFromString<AjaxResponse>(responseStr.trim())
        } catch (e: Throwable) {
            LogHelper.e("AMZN-Scrape Ajax Price Error ${e.message}", e)
            throw e
        }
    }


    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return Product
     */
    private fun extractProductDetails(html: String): Product {

        val doc = Ksoup.parse(html)
        val asin = doc.getElementsByAttributeValue("name", "asin").first()?.value() ?: ""
        //get title
        val title = doc.getElementById("productTitle")?.text() ?: ""
        //get description
        val description = doc.getElementById("featurebullets_feature_div")?.text()
            ?.replace("Bu ürün hakkında", "")?.trim() ?: ""
        //price
        val price = extractPrice(doc, asin)

        //star count
        val star = doc.select("#averageCustomerReviews .a-icon.a-icon-star").map {
            return@map (it.attr("class").split(" ").find { it.startsWith("a-star-") }
                ?: "").replace("a-star-", "")
        }
        val starCount =
            (if (star.isNotEmpty()) star.first().replace("-", ".").toDoubleOrNull() else null)
                ?: 0.0

        //comment
        val comment = doc.getElementById("acrCustomerReviewText")?.text()?.filter { it.isDigit() }
            ?.toIntOrNull() ?: 0
        //get detail
        val dataMap = mutableMapOf<String, String>()
        doc.select("#productOverview_feature_div table.a-normal.a-spacing-micro").first()
            ?.select("tr")?.forEach { row ->
                val keyElement = row.select("td").first()?.text()
                val valueElement = row.select("td").last()?.text()

                // Check if both key and value are not null
                if (keyElement != null && valueElement != null) {
                    dataMap[keyElement] = valueElement
                }
            }
        //extract img
        val img = doc.select("#imgTagWrapperId img").first()?.attr("src") ?: ""

        //generate new product
        return Product(
            id = 0,
            asin = asin,
            date = unix(),
            title = title,
            description = description.replace("Daha fazla ürün bilgisi", ""),
            price = price,
            star = starCount,
            comment = comment,
            image = img,
            extras = Json.encodeToString(dataMap)
        )
    }

    /**
     * extract price from html dom
     */
    private fun extractPrice(doc: Document, asin: String): Int {
        try {
            doc.getElementById("twister-plus-price-data-price")?.let {
                val value = it.value()
                if (value != "") {
                    return (value.toFloat() * 100).toInt()
                }
            }
            doc.getElementsByAttributeValue("name", "priceValue").first()?.let {
                val value = it.value()
                if (value != "") {
                    return (value.toFloat() * 100).toInt()
                }
            }
            doc.getElementsByAttributeValue("name", "items[0.base][customerVisiblePrice][amount]")
                .first()?.let {
                    val value = it.value()
                    if (value != "") {
                        return (value.toFloat() * 100).toInt()
                    }
                }
            doc.getElementsByClass("apexPriceToPay").forEach {
                if (it.tagName() == "span") {
                    it.getElementsByClass("a-offscreen").first()?.text()
                        ?.replace(Regex("[^0-9]"), "")?.let {
                            if (it != "") {
                                return it.toInt()
                            }
                        }
                }
            }

            runBlocking {
                try {
                    collectAjaxPrice(asin)
                } catch (_: Throwable) {
                    null
                }
            }?.let {
                return (it.Value.content.twisterSlotJson.price.toFloat() * 100).toInt()
            }
        } catch (e: Throwable) {
            LogHelper.d("price extract error $e")
            return 0
        }
        return 0

    }

    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return List<Product>
     */
    private fun extractPopularProducts(html: String): List<Product> {
        val doc = Ksoup.parse(html)
        return doc.select("li.a-carousel-card").map {
            val asin = it.getElementsByAttribute("data-asin").attr("data-asin")
            val title = it.select("a.a-link-normal.aok-block[role=\"link\"]").first()?.text() ?: ""
            val img = it.select("img").first()?.attr("src") ?: ""
            val price = it.select("span.a-size-base.a-color-price").first()?.text()
                ?.replace(Regex("[^0-9]"), "")?.let {
                    return@let if (it == "") null else it.toInt()
                } ?: 0
            return@map Product(
                asin = asin, id = 0, date = unix(), title = title,
                description = "", price = price, star = -1.0, comment = -1,
                image = img, extras = "[]", nextRunTime = 0, timeSpan = 0,
                errorCount = 0, status = ProductStatus.ACTIVE,
            )
        }.filter { it.price != 0 }
    }

    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return Product
     */
    private fun extractSearchResults(html: String): List<Product> {
        val doc = Ksoup.parse(html)
        return doc.select("div[role='listitem'][data-asin]").map {
            val asin = it.getElementsByAttribute("data-asin").attr("data-asin")
            val title = it.select("h2").first()?.text() ?: ""
            val img = it.select("img").first()?.attr("src") ?: ""
            val price =
                it.select("[aria-describedby=\"price-link\"] span.a-offscreen").first()?.text()
                    ?.replace(Regex("[^0-9]"), "")?.let {
                        return@let if (it == "") null else it.toInt()
                    } ?: 0
            return@map Product(
                asin = asin, id = 0, date = unix(), title = title,
                description = "", price = price, star = -1.0, comment = -1,
                image = img, extras = "[]", nextRunTime = 0, timeSpan = 0,
                errorCount = 0, status = ProductStatus.ACTIVE,
            )
        }
    }


    /**
     * cache list of asins to system
     * @param asins List<String>
     */
    @JvmName("cacheStringList")
    fun cacheList(asins: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val semaphore = Semaphore(10)
            asins.forEach {
                semaphore.withPermit {
                    try {
                        val prod = suspendScrape(it)
                        LogHelper.d("AMZN-Cache-List ${prod.encode()}")
                    } catch (_: Throwable) {

                    }
                }
            }
        }
    }

    /**
     * cache list of products to system
     * @param asins List<String>
     */
    @JvmName("cacheProductList")
    fun cacheList(asins: List<Product>) {
        cacheList(asins.map { it.asin })
    }
}