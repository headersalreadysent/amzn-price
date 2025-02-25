package co.ec.amazonfiyattakip.service

import androidx.core.app.PendingIntentCompat.send
import androidx.lifecycle.viewModelScope
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.asyncRun
import co.ec.helper.utils.unix
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.concurrent.thread

class AmznScrape {


    companion object {
        private const val DETAIL_PAGE_URL = "https://www.amazon.com.tr/_title_/dp/_asin_"


        fun urlFromAsin(asin: String, title: String? = null): String {
            return DETAIL_PAGE_URL.replace("_asin_", asin).replace("_title_", title ?: asin)
        }
    }

    /**
     * screpe product from asin
     * @param asin asin of amazon
     * @param then result callback
     * @param err error callback
     */
    fun scrapeFromAsin(
        asin: String,
        then: (res: Product) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        scrapeFromUrl(urlFromAsin(asin), then, err)

    }

    suspend fun suspendScrape(url: String): Product = withContext(Dispatchers.IO) {
        val pageUrl = if (url.startsWith("http")) url else urlFromAsin(url)
        val response = AmznRequest.suspendRequest(pageUrl)
        extractProductDetails(response)
    }


    /**
     * scrape product from url
     * @param url url of amazon
     * @param then result callback
     * @param err error callback
     */
    fun scrapeFromUrl(
        url: String,
        then: (res: Product) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        asyncRun({
            //generate url
            AmznRequest.request(url, { html ->
                //get html
                html?.let {
                    try {
                        //parse product from html
                        val product = extractProductDetails(it)
                        LogHelper.d(product.toString())
                        then(product)
                    } catch (t: Throwable) {
                        err(t)
                    }
                }
            }, {
                LogHelper.e("amzn", it)
                err(it)
            })
        })
    }

    /**
     * get popular
     * @param then result callback
     * @param err error callback
     */
    fun getPopular(
        then: (res: List<String>) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        asyncRun({
            //generate url
            AmznRequest.request("https://www.amazon.com.tr/gp/bestsellers", { html ->
                //get html
                html?.let {
                    try {
                        //parse product from html
                        val deals = extractPopularProducts(it)
                        then(deals)
                    } catch (t: Throwable) {
                        err(t)
                    }
                }
            }, {
                LogHelper.e("amzn", it)
                err(it)
            })
        })
    }

    /**
     * get popular
     * @param then result callback
     * @param err error callback
     */
    fun search(
        searchText: String,
        then: (res: List<String>) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        asyncRun({
            //generate url
            val encoded = URLEncoder.encode(searchText, StandardCharsets.UTF_8.toString())
            AmznRequest.request("https://www.amazon.com.tr/s?k=\"$encoded\"", { html ->
                //get html
                html?.let {
                    try {
                        //parse product from html
                        val searchAsins = extractSearchResults(it)
                        then(searchAsins)
                    } catch (t: Throwable) {
                        err(t)
                    }
                }
            }, {
                LogHelper.e("amzn", it)
                err(it)
            })
        })
    }


    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return Product
     */
    private fun extractProductDetails(html: String): Product {

        val doc = Ksoup.parse(html ?: "")
        val asin = doc.getElementsByAttributeValue("name", "asin").first()?.value() ?: ""
        //get title
        val title = doc.getElementById("productTitle")?.text() ?: ""
        //get description
        val description = doc.getElementById("featurebullets_feature_div")?.text()
            ?.replace("Bu ürün hakkında", "")?.trim() ?: ""
        //price
        val price = extractPrice(doc)

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

    private fun extractPrice(doc: Document): Int {
        try {
            doc.getElementById("twister-plus-price-data-price")?.let {
                return (it.value().toFloat() * 100).toInt()
            }
            doc.getElementsByAttributeValue("name", "priceValue").first()?.let {
                return (it.value().toFloat() * 100).toInt()
            }
            doc.getElementsByAttributeValue("name", "items[0.base][customerVisiblePrice][amount]")
                .first()?.let {
                    return (it.value().toFloat() * 100).toInt()
                }
        } catch (e: Throwable) {
            return 0
        }
        return 0

    }

    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return Product
     */
    private fun extractPopularProducts(html: String): List<String> {

        val doc = Ksoup.parse(html ?: "")
        return doc.select("li.a-carousel-card").map {
            val asin = it.getElementsByAttribute("data-asin").attr("data-asin")
            return@map asin
        }
    }

    /**
     * extract product details from amazon page content
     * @param html:String page content
     * @return Product
     */
    private fun extractSearchResults(html: String): List<String> {

        val doc = Ksoup.parse(html ?: "")
        return doc.select("div[role='listitem'][data-asin]").map {
            val asin = it.getElementsByAttribute("data-asin").attr("data-asin")
            return@map asin
        }
    }


}