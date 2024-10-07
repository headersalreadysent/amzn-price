package co.ec.amazonfiyattakip.service

import co.ec.amazonfiyattakip.db.product.Product
import co.ec.helper.AppLogger
import co.ec.helper.Async
import co.ec.helper.utils.unix
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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


    /**
     * screpe product from url
     * @param url url of amazon
     * @param then result callback
     * @param err error callback
     */
    fun scrapeFromUrl(
        url: String,
        then: (res: Product) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        Async.run({
            //generate url
            AmznRequest.request(url, { html ->
                //get html
                html?.let {
                    try {
                        //parse product from html
                        val product = extractProductDetails(it)
                        AppLogger.d(product.toString())
                        then(product)
                    } catch (t: Throwable) {
                        err(t)
                    }
                }
            }, {
                AppLogger.e("amzn", it)
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
            description = description,
            price = price,
            star = starCount,
            comment = comment,
            image = img,
            extras = Json.encodeToString(dataMap)
        )
    }

    private fun extractPrice(doc: Document): Int {
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
        return 0;
    }
}