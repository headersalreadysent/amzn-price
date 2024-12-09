package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
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
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.AppLogger
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import coil.compose.rememberAsyncImagePainter
import kotlin.random.Random

@Composable
fun DetailScreen(
    productId: Int? = null,
    model: DetailViewModel = viewModel()
) {
    val urlHandler = LocalUriHandler.current


    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()
    var graphData by remember { mutableStateOf<List<PriceGraphPair>?>(null) }
    var changeGraphData by remember { mutableStateOf<List<PriceGraphPair>?>(null) }
    var showOnlyChanges by remember { mutableStateOf(true) }

    val openUrl by remember(product) {
        mutableStateOf(AmznScrape.urlFromAsin(product?.asin ?: ""))
    }

    DisposableEffect(Unit) {
        productId?.let {
            model.loadProduct(productId)
            AppModel.setFab(Icons.Filled.ShoppingCart) {
                urlHandler.openUri(openUrl)
            }
        }
        onDispose {

        }
    }


    LaunchedEffect(prices) {
        graphData = prices?.map {
            return@map PriceGraphPair(it.date, it.price.toFloat() / 100F)
        }
        var lastPrice = -1F
        changeGraphData = graphData?.filter {
            if (lastPrice == it.price) {
                return@filter false
            }
            lastPrice = it.price
            return@filter true
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
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.image),
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.25F)
                    .statusBarsPadding()
                    .padding(bottom = 5.dp),
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    Text(
                        modifier = Modifier.weight(2F),
                        text = product.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Column(modifier = Modifier.weight(1F)) {

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "Fiyat",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.End,
                            )
                        )
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = product.price(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                textAlign = TextAlign.End,
                                fontWeight = FontWeight.Bold
                            )
                        )

                    }

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
                            Text(
                                text = if (dragValue !== null) dragValue!!.date.dateString() else " ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = if (dragValue !== null) (dragValue!!.price * 100).toInt()
                                    .price() else " ",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    if (showOnlyChanges) {
                        changeGraphData?.let {
                            if (it.size > 1) {
                                PriceGraph(it, onDrag = {
                                    dragValue = it
                                })
                            }
                        }
                    } else {
                        graphData?.let {
                            PriceGraph(it, onDrag = {
                                dragValue = it
                            })
                        }
                    }

                }

                AnimatedVisibility(visible = prices != null) {
                    prices?.let {
                        TreePriceRow(it)
                    }
                }
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 5.dp)
                )
                Button(
                    onClick = {
                        urlHandler.openUri(openUrl)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Uygulamada Görüntüle")
                }
                OutlinedButton(
                    onClick = {
                        model.stopFallowProduct()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(text = "Takibi Bırak")
                }

                prices?.reversed()?.let {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        val color =
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = .5F
                            )
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
                                            Row(verticalAlignment = Alignment.CenterVertically) {

                                                Icon(
                                                    Icons.Outlined.ChatBubble,
                                                    contentDescription = "comment",
                                                    modifier = Modifier.size(14.dp),
                                                    tint = color
                                                )

                                                Text(
                                                    text = it.comment.toString(),
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
                                                    text = it.star.toString(),
                                                    fontSize = 14.sp,
                                                    color = color,
                                                    modifier = Modifier.padding(
                                                        start = 4.dp,
                                                        end = 8.dp
                                                    )
                                                )
                                            }
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    onClick = {
                        showOnlyChanges = !showOnlyChanges
                    }) {
                    Text(text = if (showOnlyChanges) "Tüm Sorgular" else "Özet Görünüm")
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

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview(model: DetailViewModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        DetailScreen(0, model)
    }
}