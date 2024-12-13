package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString

@Composable
fun DetailScreen(
    productId: Int? = null,
    model: DetailViewModel = viewModel()
) {
    val urlHandler = LocalUriHandler.current
    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()
    var showOnlyChanges by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        productId?.let {
            model.loadProduct(productId)
            AppModel.setFab(Icons.Filled.ShoppingCart) {
                urlHandler.openUri(AmznScrape.urlFromAsin(product?.asin ?: ""))
            }
        }
        onDispose {

        }
    }

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), Alignment.Center) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(.8F)
            )
        }
    }
    val scrollState = rememberScrollState()

    product?.let { product ->


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .statusBarsPadding()
        ) {

            CutCornerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(IntrinsicSize.Min),
                corner = CutCorner.BOTTOMRIGHT,
                cutSize = 30.dp
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ProductImage(
                        product,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(.4F)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(.7F)
                            .fillMaxHeight()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = product.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                text = "Fiyat",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = product.price(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            MetaIconRow(product)
                            prices?.let {
                                val min = (it.minOfOrNull { it.price } ?: 0).toFloat()
                                val max = (it.maxOfOrNull { it.price } ?: 0).toFloat()
                                if (max > min) {
                                    Box(modifier = Modifier.fillMaxWidth(.6F)) {
                                        PriceBar(min, max, (product.price / 100F))
                                    }
                                }

                            }


                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                if (prices == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Henüz fiyat değişimi oluşmadı.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        LinearProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    prices?.let {
                        TreePriceRow(it)
                        PricesGraphWithDrag(showOnlyChanges, it) {
                            showOnlyChanges = !showOnlyChanges
                        }
                    }
                }



                prices?.reversed()?.let {

                    TitleBar(title = "Fiyat Değişimi",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable(
                                indication = null,
                                interactionSource = null
                            ) {
                                showOnlyChanges = !showOnlyChanges
                            }, extra = {
                            Icon(
                                if (showOnlyChanges) Icons.Outlined.FilterAlt else Icons.Outlined.FilterAltOff,
                                contentDescription = "filter",
                                modifier = Modifier.scale(.7F)
                            )
                        })
                    Column(modifier = Modifier.padding(top = 8.dp)) {

                        var lastPrice = 0
                        it.forEach {
                            if (showOnlyChanges && lastPrice == it.price) {
                                return@forEach
                            }
                            lastPrice = it.price
                            ListItem(
                                modifier = Modifier.padding(bottom = 2.dp),
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,

                                ),
                                overlineContent = {
                                    Text(text = it.date.timeString())
                                },
                                headlineContent = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(it.date.dateString())
                                            MetaIconRow(it)
                                        }
                                        Text(
                                            it.price(),
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            modifier = Modifier.padding(top = 2.dp),
                                        )
                                    }
                                }
                            )
                        }
                    }

                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))


                CutCornerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                }

                ExtrasArea(product = product)
                OutlinedButton(
                    onClick = {
                        model.stopFallowProduct()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Takibi Bırak")
                }
            }
        }
    }


}

/**
 * show three price info
 */
@Composable
fun TreePriceRow(prices: List<PriceInfo>) {
    val min = (prices.minOfOrNull { it.price } ?: 0)
    val max = (prices.maxOfOrNull { it.price } ?: 0)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val columnModifier = Modifier
            .weight(1F)
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(2.dp)
            )
            .padding(4.dp)
        Column(modifier = columnModifier) {
            Text(
                text = "En Düşük",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = min.price(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Column(modifier = columnModifier) {
            Text(
                text = "Ortalama",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = ((min + max) / 2F).toInt().price(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        Column(modifier = columnModifier) {
            Text(
                text = "En Yüksek",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = max.price(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun PricesGraphWithDrag(
    showOnlyChanges: Boolean,
    prices: List<PriceInfo>,
    then: (() -> Unit) = {}
) {
    val graphData by remember {
        mutableStateOf(
            prices.map { PriceGraphPair(it.date, it.price / 100F) }
        )
    }
    var lastPrice = -1F
    val onlyChanges by remember {
        mutableStateOf(
            prices.map { PriceGraphPair(it.date, it.price / 100F) }
                .filter {
                    if (lastPrice == it.price) {
                        return@filter false
                    }
                    lastPrice = it.price
                    return@filter true
                }
        )
    }
    if (onlyChanges.size > 2) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2F),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(
                containerColor =
                MaterialTheme.colorScheme.surfaceContainer,
            )
        ) {
            var dragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
            Text(
                text = "Fiyat Geçmişi", modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            AnimatedVisibility(visible = dragValue != null) {
                //animate by dragValue
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 2.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dragValue?.let {
                        Text(
                            text = it.date.dateString() + " " + it.date.timeString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = (it.price * 100).toInt().price(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
            PriceGraph(
                modifier = Modifier.clickable(
                    interactionSource = null,
                    indication = null
                ) {
                    then()
                },
                if (showOnlyChanges) onlyChanges else graphData, onDrag = {
                    dragValue = it
                })
        }
    }

}

@Composable
fun MetaIconRow(priceInfo: Any) {
    val comment = if (priceInfo is PriceInfo) priceInfo.comment else (priceInfo as Product).comment
    val star = if (priceInfo is PriceInfo) priceInfo.star else (priceInfo as Product).star

    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier
                .padding(end = 4.dp)
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    cutShape(CutCorner.BOTTOMRIGHT, 5.dp)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    cutShape(CutCorner.BOTTOMRIGHT, 5.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                Icons.Outlined.ChatBubble,
                contentDescription = "comment",
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = comment.toString(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(
                    start = 2.dp,
                )
            )
        }
        Row(
            modifier = Modifier
                .padding(end = 4.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    cutShape(CutCorner.BOTTOMRIGHT, 5.dp)
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = "comment",
                modifier = Modifier
                    .size(12.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = star.toString(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(
                    start = 2.dp,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(model: DetailViewModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        DetailScreen(0, model)
    }
}