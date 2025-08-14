package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.screen.detail.PriceStat
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import kotlin.random.Random


/**
 * prices list area
 */
@Composable
fun PriceListArea(
    priceListData: List<PriceInfo>,
    showAll: Boolean = false,
    filterDuplicates: Boolean = false
) {
    if (priceListData.isEmpty()) {
        return
    }
    var showAllList by remember { mutableStateOf(showAll) }
    val visibleList by remember {
        mutableStateOf(priceListData.let { list ->
            if (filterDuplicates) {
                var lastPrice = 0
                return@let list.filter {
                    if (it.price == lastPrice) {
                        return@filter false
                    } else {
                        lastPrice = it.price
                        return@filter true
                    }
                }
            }
            list
        }.reversed().let {
            if (it.size > 10 && !showAllList) it.slice(0..10)
            else it
        })
    }

    val max by remember(visibleList, showAllList) {
        mutableFloatStateOf(visibleList.maxBy { it.price }.price.toFloat())
    }

    val min by remember(visibleList, showAllList) {
        mutableFloatStateOf(visibleList.minBy { it.price }.price.toFloat())
    }
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        visibleList.forEachIndexed { index, it ->

            val prevPrice = visibleList.getOrNull(index + 1)?.price ?: 0
            val collapseFraction = ((it.price - min) / (max - min))
            val color = MaterialTheme.colorScheme.tertiaryContainer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .shadow(4.dp)
                    .background(color.copy(alpha = .90F), RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0.5F to color,
                                0.5F + collapseFraction to color.copy(alpha = .95F),
                                0.5F + collapseFraction + 0.05F to Color.Transparent,
                            )
                        ), RoundedCornerShape(2.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1F)
                        .padding(8.dp)
                ) {
                    Text(
                        it.date.dateString() + " " + it.date.timeString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                    PriceStat(it)
                }
                Row(
                    modifier = Modifier
                        .weight(1F)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    BasicText(
                        it.price(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        autoSize = TextAutoSize.StepBased(12.sp, 26.sp)

                    )
                    Icon(
                        if (prevPrice == it.price) {
                            Icons.AutoMirrored.Outlined.TrendingFlat
                        } else if (prevPrice < it.price) {
                            Icons.AutoMirrored.Outlined.TrendingUp
                        } else {
                            Icons.AutoMirrored.Outlined.TrendingDown
                        },
                        contentDescription = "trend",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .scale(.8F),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                            alpha = .8F
                        )
                    )

                }

            }

        }
        if (!showAllList && priceListData.size > 10) {
            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick = {
                    showAllList = true
                }
            ) {
                Text("Tüm Listeyi Göster")
            }
        }
    }


}

@Composable
@Preview(showBackground = true)
private fun PriceListPreview() {
    PreviewProviders {
        val product = Product.fake()
        PriceListArea(priceListData = (0..30).map {
            val price = Random.nextFloat() * 20000 + product.price
            PriceInfo(
                id = it,
                productId = 1,
                asin = product.asin,
                date = unix() - (10 - it) * 86400,
                price = price.toInt()
            )
        })
    }
}