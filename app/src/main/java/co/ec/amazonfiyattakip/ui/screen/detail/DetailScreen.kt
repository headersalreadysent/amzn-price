package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DensityLarge
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.twotone.ChatBubble
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest

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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(IntrinsicSize.Min)
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
                                text = product.shortDesc(150),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
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
                prices?.let {
                    if (it.size == 1) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Henüz fiyat değişimi oluşmadı.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            LinearProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                        }
                    } else {
                        TreePriceRow(it)
                        PricesGraphWithDrag(showOnlyChanges, it)
                    }
                }


                prices?.reversed()?.let {
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
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                                            )
                                        )
                                    }
                                },
                            )
                        }
                    }

                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))

                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        showOnlyChanges = !showOnlyChanges
                    }) {
                    Text(text = if (showOnlyChanges) "Tüm Sorgular" else "Özet Görünüm")
                }
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
    prices: List<PriceInfo>
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

    if (showOnlyChanges && onlyChanges.size == 1) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Henüz fiyat değişimi oluşmadı.",
                style = MaterialTheme.typography.bodySmall
            )
            LinearProgressIndicator(modifier = Modifier.padding(top = 4.dp))
        }
        return
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(2F),
        shape = RoundedCornerShape(4.dp)
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
            if (showOnlyChanges) onlyChanges else graphData, onDrag = {
                dragValue = it
            })
    }
}

@Composable
fun MetaIconRow(priceInfo: Any) {
    val color =
        MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = .5F
        )
    val comment = if (priceInfo is PriceInfo) priceInfo.comment else (priceInfo as Product).comment
    val star = if (priceInfo is PriceInfo) priceInfo.star else (priceInfo as Product).star

    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            Icons.Outlined.ChatBubble,
            contentDescription = "comment",
            modifier = Modifier.size(14.dp),
            tint = color
        )
        Text(
            text = comment.toString(),
            fontSize = 14.sp,
            color = color,
            modifier = Modifier.padding(
                start = 4.dp,
                end = 8.dp
            )
        )
        Icon(
            Icons.Outlined.Star,
            contentDescription = "comment",
            modifier = Modifier
                .size(14.dp),
            tint = color
        )
        Text(
            text = star.toString(),
            fontSize = 14.sp,
            color = color,
            modifier = Modifier.padding(
                start = 4.dp,
                end = 8.dp
            )
        )
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