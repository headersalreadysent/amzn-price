package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.DateRow
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.composables.TimeSpan
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.helper.predictNextPrices
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.helper.rememberBlink
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.composable.AutoText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    productId: Int? = null, model: DetailViewModel = viewModel()
) {
    val urlHandler = LocalUriHandler.current
    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()


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
    //graph and lists show only changes
    var showOnlyChanges by remember { mutableStateOf(true) }
    val priceListData by remember(showOnlyChanges, prices) {
        var lastPrice = -1
        mutableStateOf(prices.orEmpty().filter {
            //filter by show only
            if (!showOnlyChanges) {
                return@filter true
            }
            if (lastPrice == it.price) {
                return@filter false
            }
            lastPrice = it.price
            return@filter true
        })
    }


    if (product == null) {
        //loader screen
        Progress("Yükleniyor")
    }

    product?.let { product ->
        var refreshing by remember { mutableStateOf(false) }
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                refreshing = true
                model.refreshProduct({
                    App.snack("Ürün güncellendi.")
                    refreshing = false
                }) {
                    refreshing = false
                    App.snack("Güncelleme sırasında bir sorun oluştu.")
                }
            },
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {
                ProductBox(product)


                if (prices == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val alpha by rememberBlink()
                        Text(
                            text = "Fiyat değişimleri bekleniyor.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.alpha(alpha)
                        )
                        LinearProgressIndicator(modifier = Modifier.padding(top = 4.dp))
                    }
                } else {

                    prices?.let {
                        ErrorStop(product) {
                            model.changeStatus(ProductStatus.ACTIVE)
                        }
                        TreePriceRow(it)
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        CalendarPriceDataArea(it)
                        PricePredictionArea(it)
                        TitleBar(
                            title = "Fiyat Değişimi",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable(
                                    indication = null, interactionSource = null
                                ) {
                                    showOnlyChanges = !showOnlyChanges
                                },
                            extra = {
                                Text(
                                    text = if (showOnlyChanges) "(Değişimler)" else "(Tüm)",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontStyle = FontStyle.Italic
                                    )
                                )
                                Icon(
                                    if (showOnlyChanges) Icons.Outlined.FilterAlt else Icons.Outlined.FilterAltOff,
                                    contentDescription = "filter",
                                    modifier = Modifier.scale(.7F),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            })
                        PriceListArea(priceListData)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))

                val canExtend = product.description.length > 300
                var showFull by remember { mutableStateOf(canExtend) }
                CutCornerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .clickable(enabled = canExtend) {
                            showFull = !showFull
                        }
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = if (canExtend && showFull) buildAnnotatedString {
                            append(product.shortDesc(300))
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 14.sp,
                                )
                            ) {
                                append("    Devamını oku »")
                            }
                        } else buildAnnotatedString {
                            append(product.description)
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                ExtrasArea(
                    product = product,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
                TimeSpan(product.timeSpan / 60, {
                    model.updateTimeSpan(it)
                }, latest = prices?.maxByOrNull { it.date }?.date)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (product.status == ProductStatus.ACTIVE) {
                        OutlinedButton(
                            onClick = {
                                model.changeStatus(ProductStatus.PASSIVE)
                            },
                            modifier = Modifier
                                .weight(1F)
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Text(text = "Takibi Durdur")
                        }
                    }
                    if (product.status == ProductStatus.PASSIVE) {
                        OutlinedButton(
                            onClick = {
                                model.changeStatus(ProductStatus.ACTIVE)
                            },
                            modifier = Modifier
                                .weight(1F)
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(text = "Takibi Başlat")
                        }
                    }

                    val navigation = LocalNavigation.current
                    OutlinedButton(
                        onClick = {
                            model.deleteProduct {
                                App.snack("Ürün silindi.")
                                navigation.navigate("main")
                            }
                        },
                        modifier = Modifier
                            .weight(1F)
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(text = "Takibi Sil")
                    }
                }
            }
            AppModel.cutCard(
                modifier = Modifier
                    .then(
                        if (priceListData.size > 2) Modifier.aspectRatio(5F) else Modifier.height(
                            40.dp
                        )
                    )
            ) {
                PricesGraphWithDrag(
                    prices = priceListData
                )
            }

        }

    }


}

@Composable
fun ErrorStop(product: Product, activate: () -> Unit = {}) {
    if (product.status == ProductStatus.ERRORSTOP) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.errorContainer,
                    RoundedCornerShape(5.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                "Bu ürün bir çok hatalı sorgulama sebebiyle pasifleştirildi. " +
                        "Amazon üzerinde ürüne ulaşılamıyor olabilir.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    activate()
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(5.dp),
                contentPadding = PaddingValues(vertical = 1.dp)
            ) {
                Text("Aktifleştir")
            }
        }
    }

}

/**
 * show three price info
 */
@Composable
fun TreePriceRow(
    prices: List<PriceInfo>,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    val min = (prices.minOfOrNull { it.price } ?: 0)
    val max = (prices.maxOfOrNull { it.price } ?: 0)
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val columnModifier =
            Modifier
                .weight(1F)
                .background(containerColor)
                .padding(4.dp)
        val titleStyle = MaterialTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Center,
            color = contentColor.copy(alpha = .8F)
        )
        val valueStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
        Column(modifier = columnModifier) {
            Text(
                text = "En Düşük", modifier = Modifier.fillMaxWidth(), style = titleStyle
            )
            Text(
                text = min.price(), modifier = Modifier.fillMaxWidth(), style = valueStyle
            )
        }
        if (max != min) {

            Column(modifier = columnModifier) {
                Text(
                    text = "Ortalama", modifier = Modifier.fillMaxWidth(), style = titleStyle
                )
                Text(
                    text = ((min + max) / 2F).toInt().price(),
                    modifier = Modifier.fillMaxWidth(),
                    style = valueStyle
                )
            }
        }
        Column(modifier = columnModifier) {
            Text(
                text = "En Yüksek", modifier = Modifier.fillMaxWidth(), style = titleStyle
            )
            Text(
                text = max.price(), modifier = Modifier.fillMaxWidth(), style = valueStyle
            )
        }
    }
}

@Composable
fun PricesGraphWithDrag(prices: List<PriceInfo> = listOf()) {
    val graphData by remember {
        mutableStateOf(prices.map {
            PriceGraphPair(
                it.date, it.price / 100F
            )
        })
    }
    if (graphData.size > 2) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            var dragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
            PriceGraph(
                modifier = Modifier.align(Alignment.BottomEnd),
                aspectRatio = 6F,
                prices = graphData,
                onDrag = {
                    dragValue = it
                },
                hasCircles = false,
                closePath = false,
                drawStyle = Stroke(10F)
            )
            Row(
                modifier = Modifier
                    .padding(end = 20.dp, start = 8.dp)
                    .padding(top = 16.dp)
                    .fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dragValue?.let {
                    Text(
                        text = it.date.dateString() + " " + it.date.timeString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    Text(
                        text = (it.price * 100).toInt().price(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
        }
    }

}


@Composable
fun CalendarPriceDataArea(prices: List<PriceInfo>) {
    TitleBar(
        title = "Günlük Fiyatlar",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )
    DateRow(
        priceList = prices.associate { Pair(it.date.toInt(), it.price) },
    ) {
        if (it.second != 0) {
            App.snack(
                "${
                    it.first.toLong().dateString()
                } ortalama fiyat ${it.second.price()}"
            )
        }
    }
}

@Composable
fun PricePredictionArea(prices: List<PriceInfo>) {
    if (prices.size > 20) {
        val predict by remember {
            mutableStateOf(predictNextPrices(prices))
        }
        var showDialog by remember { mutableStateOf(false) }
        if (showDialog) {
            AlertDialog(
                text = {
                    Column {
                        Text(
                            text = "Fiyat tahmini bilgisi sınırlı bir tahmin olup, regresyon hesaplaması ile bulunmaktadır.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Justify
                            )
                        )
                        Text(
                            "Tahmin Fonksiyonu",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Start
                            )
                        )
                        Text(
                            predict.second,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Start
                            )
                        )
                    }
                },
                onDismissRequest = {
                    showDialog = false
                },
                confirmButton = {
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDialog = false
                        }
                    ) {
                        Text("Kapat")
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {

            TitleBar(
                title = "Fiyat Tahmini",
                modifier = Modifier
                    .fillMaxWidth(),
                extra = {
                    Icon(
                        Icons.Filled.Info, "",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .scale(.8F)
                            .clickable {
                                showDialog = true
                            })
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(IntrinsicSize.Max)
            ) {
                predict.first.forEachIndexed { index, it ->
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val date = it.first.dateString()
                        Text(
                            date.replace(" 202", "\n202"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        AutoText(
                            it.second.toInt().price(),
                            fontSize = 1..16,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (index < predict.first.size - 1) {
                        VerticalDivider(
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * prices list area
 */
@Composable
fun PriceListArea(priceListData: List<PriceInfo>) {
    var showPrice by remember { mutableStateOf(true) }
    var showAllList by remember { mutableStateOf(false) }
    priceListData.reversed().let { list ->
        Column(
            modifier = Modifier
                .padding(8.dp)
                .clickable(indication = null, interactionSource = null) {
                    showPrice = !showPrice
                }) {
            list.let {
                if (it.size > 10 && !showAllList) it.slice(0..10)
                else it
            }.forEachIndexed { index, it ->
                val prevPrice = if (list.size > index + 1) {
                    list[index + 1].price
                } else 0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .height(40.dp)
                        .shadow(.5.dp)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        it.date.dateString() + " " + it.date.timeString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    )
                    if (showPrice) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                it.price(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
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
                                    .scale(.6F),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                    alpha = .8F
                                )
                            )

                        }

                    } else {
                        PriceStat(it)
                    }
                }
            }
        }
    }
    if (showAllList == false) {
        OutlinedButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            onClick = {
                showAllList = true
            }) {
            Text("Tüm Listeyi Göster")
        }
    }
}

@Composable
fun PriceStat(
    price: PriceInfo,
    color: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val density = LocalDensity.current
        Icon(
            Icons.Filled.Star, "",
            modifier = Modifier
                .height(with(density) { 13.sp.toDp() })
                .padding(end = 4.dp),
            tint = color.copy(alpha = .8F)
        )
        Text(
            price.star.toString(),
            color = color,
            style = MaterialTheme.typography.bodyMedium
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxHeight(.8F)
        )
        Icon(
            Icons.Filled.ChatBubble, "",
            modifier = Modifier
                .height(with(density) { 13.sp.toDp() })
                .padding(end = 4.dp),
            tint = color.copy(alpha = .8F)
        )
        Text(
            price.comment.toString(),
            color = color,
            style = MaterialTheme.typography.bodyMedium
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


