package co.ec.amazonfiyattakip.service

import co.ec.amazonfiyattakip.db.product.Product
import co.ec.helper.AppLogger
import co.ec.helper.Async
import co.ec.helper.utils.unix
import com.fleeksoft.ksoup.Ksoup
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AmznScrape {

    private val DETAIL_PAGE_URL = "https://www.amazon.com.tr/_title_/dp/_asin_"

    /**
     * screpe product from asin code
     * @param asin asin code of amazon
     * @param then result callback
     * @param err error callback
     */
    fun scrapeFromAsin(
        asin: String,
        then: (res: Product) -> Unit = { _ -> },
        err: (res: Throwable) -> Unit = { _ -> }
    ) {
        Async.run({
            //generate url
            val url = DETAIL_PAGE_URL.replace("_asin_", asin).replace("_title_", asin)
            AmznRequest.request(url, { html ->
                //get html
                html?.let {
                    try {
                        //parse product from html
                        val product = extractProductDetails(it, asin)
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
     * @param asin:String product asin code
     * @return Product
     */
    private fun extractProductDetails(html: String, asin: String): Product {

        val doc = Ksoup.parse(html ?: "")

        //get title
        val title = doc.getElementById("productTitle")?.text() ?: ""
        //get description
        val description = doc.getElementById("featurebullets_feature_div")?.text()
            ?.replace("Bu ürün hakkında", "")?.trim() ?: ""
        //price
        val priceElement =
            listOf("priceValue", "items[0.base][customerVisiblePrice][amount]")
                .map {
                    return@map doc.getElementsByAttributeValue("name", it)
                }
                .first { it.isNotEmpty() }
                .getOrNull(0)
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
            price = ((priceElement?.value() ?: "0").toFloat() * 100).toInt(),
            star = starCount,
            comment = comment,
            image = img,
            extras = Json.encodeToString(dataMap)
        )
    }
}