package co.ec.amazonfiyattakip.ui.part

import android.R.attr.maxLines
import androidx.activity.SystemBarStyle.Companion.auto
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.ExpertMode
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import kotlin.random.Random


@Composable
fun MainProductCard(
    productWithPrices: ProductWithPrices,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
    aspectRatio: Float = 2.5F
) {

    CutCornerCard(
        modifier = Modifier.fillMaxWidth(),
        click = { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        cutSize = 10.dp,
        shadow = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
        ) {
            ProductImage(
                productWithPrices.product,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1F)
                    .align(Alignment.TopEnd),
                color = containerColor,
                radialGradient = true
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if(ExpertMode.current) .8F else .95F)
            ) {

                val text = buildAnnotatedString {
                    if (productWithPrices.product.status == ProductStatus.ERRORSTOP) {
                        appendInlineContent("stopIcon", " ")
                    }
                    append(productWithPrices.product.title + "\n")
                }
                Text(
                    text,
                    inlineContent = mapOf(
                        "stopIcon" to InlineTextContent(
                            Placeholder(22.sp, 20.sp, PlaceholderVerticalAlign.Center)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    style = MaterialTheme.typography.titleMedium.copy(
                        lineHeight = 22.sp,
                        shadow = Shadow(MaterialTheme.colorScheme.primary, Offset(1F, 1F), 1F),
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                BasicText(
                    text = productWithPrices.product.price(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold
                    ),
                    autoSize = TextAutoSize.StepBased(10.sp, 60.sp)
                )
            }
            if (ExpertMode.current) {
                Crossfade(
                    targetState = productWithPrices.priceInfoList.size > 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.4F)
                        .align(Alignment.BottomCenter)
                ) {

                    if (it) {
                        var selectedPair by remember { mutableStateOf<PriceGraphPair?>(null) }
                        val groupedPrices by remember {
                            var group =
                                productWithPrices.priceInfoList.groupBy { it.date.dateString("yyyyMMdd") }
                            if (group.size == 1) {
                                group =
                                    productWithPrices.priceInfoList.groupBy { it.date.dateString("yyyyMMdd-hhmm") }
                            }
                            mutableStateOf(group.map {
                                val ave =
                                    it.value.toList().sumOf { it.price }.toFloat() / it.value.size
                                return@map PriceGraphPair(it.value[0].date, ave)
                            })
                        }
                        PriceGraph(
                            modifier = Modifier,
                            prices = groupedPrices,
                            onDrag = { pair ->
                                selectedPair = pair
                            },
                            hasCircles = false,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = .4F),
                            subRatio = 0F
                        )
                        val textColor =
                            contentColorFor(MaterialTheme.colorScheme.secondary.copy(alpha = .8F))
                        selectedPair?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    it.price.toInt().price(),
                                    modifier = Modifier.padding(end = 8.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                )
                            }

                        }
                    }


                }

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainProductCardPreview() {
    PreviewProviders {
        Column {
            var prices = (0..10).map {
                var price = Random.nextFloat() * 200 + 2500
                PriceInfo(
                    id = it,
                    productId = 1,
                    asin = "",
                    date = unix() - (10 - it) * 86400,
                    price = price.toInt()
                )
            }
            val fake = Product.fake()
            MainProductCard(ProductWithPrices(product = fake, prices))
            HorizontalDivider(modifier = Modifier
                .fillMaxWidth()
                .height(5.dp))
            MainProductCard(
                ProductWithPrices(
                    product = fake.copy(
                        status = ProductStatus.ERRORSTOP
                    ), prices
                )
            )
        }

    }
}