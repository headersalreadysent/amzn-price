package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.DateRow
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.PriceListArea
import co.ec.amazonfiyattakip.composables.ProductBox
import co.ec.amazonfiyattakip.composables.Responsive
import co.ec.amazonfiyattakip.composables.TimeSpan
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.noprice.NoPriceDao
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.helper.condition
import co.ec.amazonfiyattakip.helper.predictNextPrices
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.helper.rememberBlink
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.SetIconColorEvent
import co.ec.amazonfiyattakip.ui.part.FakeDetailScreen
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.helpers.EventBus
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    productId: Int? = null, model: DetailViewModel = viewModel()
) {
    val density = LocalDensity.current
    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()
    val noPriceControl by model.noPriceControl.observeAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    AppModel.setFab(Icons.Filled.Settings) {
        showBottomSheet = !showBottomSheet
    }
    val secondary = MaterialTheme.colorScheme.secondaryContainer
    DisposableEffect(Unit) {
        productId?.let {
            model.loadProduct(productId)
        }
        CoroutineScope(Dispatchers.Main).launch {
            delay(500)
            EventBus.publish(SetIconColorEvent(secondary))
        }
        onDispose {
            CoroutineScope(Dispatchers.Default).launch {
                EventBus.publish(SetIconColorEvent(null))
            }
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
        FakeDetailScreen()
    }

    product?.let { product ->
        var refreshing by remember { mutableStateOf(false) }
        var size by remember { mutableStateOf(0.dp) }

        val state = rememberPullToRefreshState()
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
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    size = density.run { it.width.toDp() }
                },
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(3F),
                    isRefreshing = refreshing,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    state = state
                )
            },
        ) {
            Responsive(content = { isCompact ->
                val scrollState = rememberScrollState()
                val maxHeight = (size.value / if (isCompact) 2F else 2.5F).dp
                val minHeight = 90.dp

                val collapseRange = density.run { (maxHeight - minHeight).toPx() }
                val collapseFraction = (scrollState.value / collapseRange).coerceIn(0f, 1f)
                val animatedHeight = lerp(maxHeight, minHeight, collapseFraction)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(animatedHeight)
                        .zIndex(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ProductBox(product, height = animatedHeight)
                }



                if (isCompact) {

                    CompactScreen(
                        scrollState = scrollState,
                        product = product,
                        prices = prices,
                        maxHeight = maxHeight,
                        showOnlyChanges = showOnlyChanges,
                        showOnlyChange = {
                            showOnlyChanges = it
                        },
                        priceListData = priceListData,
                        noPriceControl = noPriceControl,
                        model = model
                    )
                } else {

                    TabletScreen(
                        scrollState = scrollState,
                        product = product,
                        prices = prices,
                        maxHeight = maxHeight,
                        showOnlyChanges = showOnlyChanges,
                        showOnlyChange = {
                            showOnlyChanges = it
                        },
                        priceListData = priceListData,
                        noPriceControl = noPriceControl,
                        model = model
                    )
                }


            })
            AppModel.cutCard(
                modifier = Modifier.then(
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
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color.Transparent,
            shape = cutShape(
                CutCorner.TOPRIGHT, 30.dp
            )
        ) {
            product?.let {
                ModalContent(model, it, prices?.maxByOrNull { it.date }?.date ?: 0L)
            }
        }
    }

}

@Composable
fun CompactScreen(
    scrollState: ScrollState,
    product: Product,
    prices: List<PriceInfo>?,
    maxHeight: Dp,
    showOnlyChanges: Boolean,
    showOnlyChange: (showOnlyChanges: Boolean) -> Unit = {},
    priceListData: List<PriceInfo>,
    noPriceControl: NoPriceDao.NoPriceControl?,
    model: DetailViewModel
) {

    val urlHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .zIndex(0F)
            .padding(bottom = 35.dp)
    ) {
        Spacer(modifier = Modifier.height(maxHeight)) // boşluk bırak
        if (prices == null) {
            NoPriceArea()
        } else {

            ErrorStop(product) {
                model.changeStatus(ProductStatus.ACTIVE)
            }
            TreePriceRow(prices)
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp), onClick = {
                    urlHandler.openUri(AmznScrape.urlFromAsin(product.asin))
                }, colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ), shape = RoundedCornerShape(3.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.ShoppingCart, "", modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        "Satın Al", style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            CalendarPriceDataArea(prices)
            PricePredictionArea(prices)
            if (priceListData.isNotEmpty()) {
                TitleBar(
                    title = "Fiyat Değişimi",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                        .clickable(
                            indication = null, interactionSource = null
                        ) {
                            if (!showOnlyChanges) {
                                App.snack("Sadece fiyat değişimleri gösteriliyor.")
                            } else {
                                App.snack("Tüm sorgulamalar gösteriliyor.")
                            }
                            showOnlyChange(!showOnlyChanges)
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

                noPriceControl?.let {
                    NoPriceAlert(it)
                }
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
                .padding(horizontal = 8.dp)) {
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
                }, style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify
                ), modifier = Modifier.padding(8.dp)
            )
        }

        ExtrasArea(
            product = product, modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun TabletScreen(
    scrollState: ScrollState,
    product: Product,
    prices: List<PriceInfo>?,
    maxHeight: Dp,
    showOnlyChanges: Boolean,
    showOnlyChange: (showOnlyChanges: Boolean) -> Unit = {},
    priceListData: List<PriceInfo>,
    noPriceControl: NoPriceDao.NoPriceControl?,
    model: DetailViewModel
) {

    val urlHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()

            .verticalScroll(scrollState)
    ) {

        Spacer(modifier = Modifier.height(maxHeight)) // boşluk bırak
        Row(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0F)
        ) {
            Column(modifier = Modifier.weight(1F)) {

                prices?.let {
                    TreePriceRow(prices, vertical = true)
                }
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp), onClick = {
                        urlHandler.openUri(AmznScrape.urlFromAsin(product.asin))
                    }, colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ), shape = RoundedCornerShape(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart, "", modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "Satın Al", style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }


                PricePredictionArea(
                    prices ?: emptyList(),
                    vertical = true
                )
            }
            Column(
                modifier = Modifier.weight(2F)
            )
            {

                if (prices == null) {
                    NoPriceArea()
                } else {

                    ErrorStop(product) {
                        model.changeStatus(ProductStatus.ACTIVE)
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    )

                    CalendarPriceDataArea(prices)
                    if (priceListData.isNotEmpty()) {
                        TitleBar(
                            title = "Fiyat Değişimi",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 8.dp)
                                .clickable(
                                    indication = null, interactionSource = null
                                ) {
                                    if (!showOnlyChanges) {
                                        App.snack("Sadece fiyat değişimleri gösteriliyor.")
                                    } else {
                                        App.snack("Tüm sorgulamalar gösteriliyor.")
                                    }
                                    showOnlyChange(!showOnlyChanges)
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

                        noPriceControl?.let {
                            NoPriceAlert(it)
                        }
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
                        .padding(horizontal = 8.dp)) {
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
                        }, style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Justify
                        ), modifier = Modifier.padding(8.dp)
                    )
                }

                ExtrasArea(
                    product = product, modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(35.dp)) // boşluk bırak
            }

        }

        Spacer(modifier = Modifier.height(35.dp)) // boşluk bırak

    }
}

@Composable
fun NoPriceArea() {
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
}

@Composable
fun NoPriceAlert(info: NoPriceDao.NoPriceControl) {

    Text(
        buildAnnotatedString {
            append("Amazon fiyat politikalarından dolayı, bu ürün için ")
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append(info.date.dateString() + " " + info.date.timeString())
            }
            append(" tarihinden itibaren ")
            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append(info.count.toString())
            }
            append(" sorgulamada fiyat bulunamadı.")
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 6.dp)
            .shadow(4.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(2.dp))
            .padding(8.dp),
        style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Justify,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            fontWeight = FontWeight.Medium
        )
    )

}

@Composable
fun ErrorStop(product: Product, activate: () -> Unit = {}) {
    if (product.status == ProductStatus.ERRORSTOP) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(5.dp)
                )
                .padding(8.dp)
        ) {
            Text(
                "Bu ürün bir çok hatalı sorgulama sebebiyle pasifleştirildi. " + "Amazon üzerinde ürüne ulaşılamıyor olabilir.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(), onClick = {
                    activate()
                }, colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.onErrorContainer,
                    contentColor = MaterialTheme.colorScheme.errorContainer
                ), shape = RoundedCornerShape(5.dp), contentPadding = PaddingValues(vertical = 1.dp)
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
fun ColumnScope.TreePriceRow(
    prices: List<PriceInfo>,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    vertical: Boolean = false
) {

    val min = (prices.minOfOrNull { it.price } ?: 0)
    val max = (prices.maxOfOrNull { it.price } ?: 0)

    @Composable
    fun SubItems(columnModifier: Modifier) {

        val titleStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center, color = contentColor
        )
        val valueStyle = MaterialTheme.typography.bodyLarge.copy(
            textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, color = contentColor
        )
        Column(modifier = columnModifier) {
            Text(
                text = "En Düşük", modifier = Modifier.fillMaxWidth(), style = titleStyle
            )
            BasicText(
                text = min.price(),
                modifier = Modifier.fillMaxWidth(),
                style = valueStyle,
                autoSize = TextAutoSize.StepBased(14.sp, 20.sp)
            )
        }
        if (max != min) {
            Column(modifier = columnModifier) {
                Text(
                    text = "Ortalama", modifier = Modifier.fillMaxWidth(), style = titleStyle
                )
                BasicText(
                    text = ((min + max) / 2F).toInt().price(),
                    modifier = Modifier.fillMaxWidth(),
                    style = valueStyle,
                    autoSize = TextAutoSize.StepBased(14.sp, 20.sp)
                )
            }
        }
        Column(modifier = columnModifier) {
            Text(
                text = "En Yüksek", modifier = Modifier.fillMaxWidth(), style = titleStyle
            )
            BasicText(
                text = max.price(),
                modifier = Modifier.fillMaxWidth(),
                style = valueStyle,
                autoSize = TextAutoSize.StepBased(14.sp, 20.sp)
            )
        }
    }
    if (vertical) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {


            SubItems(
                Modifier
                    .fillMaxWidth()
                    .background(containerColor, RoundedCornerShape(1.dp))
                    .padding(4.dp)
                    .padding(top = 4.dp)
            )
        }
    } else {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {

            SubItems(
                Modifier
                    .weight(1F)
                    .background(containerColor, RoundedCornerShape(1.dp))
                    .padding(4.dp)
                    .padding(top = 4.dp)
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
fun CalendarPriceDataArea(
    prices: List<PriceInfo>,
) {
    if (prices.isNotEmpty()) {

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

}

@Composable
fun PricePredictionArea(
    prices: List<PriceInfo>,
    vertical: Boolean = false
) {
    if (prices.size > 20) {
        val predict by remember { mutableStateOf(predictNextPrices(prices)) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {

            TitleBar(
                title = "Fiyat Tahmini", modifier = Modifier.fillMaxWidth(), extra = {
                    Icon(
                        Icons.Filled.Info,
                        "",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .scale(.8F)
                            .clickable {
                                App.snack(
                                    "Fiyat tahmini bilgisi sınırlı bir tahmin olup, regresyon hesaplaması ile bulunmaktadır.\n" +

                                            predict.second, SnackbarDuration.Long
                                )
                            })
                })
            @Composable
            fun list() {

                predict.first.forEachIndexed { index, it ->
                    Column(
                        modifier = Modifier
                            .condition(vertical) {
                                fillMaxWidth()
                            }
                            .condition(!vertical) {
                                weight(1F)
                            }
                            .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val date = it.first.dateString()
                        BasicText(
                            if (vertical) date else date.replace(" 202", "\n202"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.secondary,
                            ),
                            modifier = Modifier.padding(bottom = 2.dp)

                        )
                        BasicText(
                            it.second.toInt().price(),
                            autoSize = TextAutoSize.StepBased(12.sp, 20.sp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                            ),
                            maxLines = 1

                        )
                    }
                    if (index < predict.first.size - 1) {
                        VerticalDivider(
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
            if (vertical) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    list()
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(IntrinsicSize.Max)
                ) {
                    list()
                }
            }

        }
    }
}

/**
 * Modal content
 */
@Composable
fun ModalContent(model: DetailViewModel, product: Product, latestQuery: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp),
            )
            .padding(top = 30.dp)
            .padding(horizontal = 12.dp)
            .padding(bottom = 30.dp)
    ) {

        TimeSpan(product.timeSpan / 60, { time, text ->
            model.updateTimeSpan(time)


        }, latest = latestQuery)
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
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
                    Icon(Icons.Filled.Stop, "stop")
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
                    Icon(Icons.Filled.PlayArrow, "start")
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
                Icon(Icons.Filled.Delete, "")
                Text(text = "Takibi Sil")
            }
        }
    }


}


@Composable
fun PriceStat(
    price: PriceInfo, color: Color = MaterialTheme.colorScheme.onTertiaryContainer
) {
    Row(
        modifier = Modifier.height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val density = LocalDensity.current
        val iconHeight = density.run { 12.sp.toDp() }
        Icon(
            Icons.Filled.Star,
            "",
            modifier = Modifier
                .height(iconHeight)
                .padding(end = 4.dp),
            tint = color.copy(alpha = .8F)
        )
        Text(
            price.star.toString(), color = color, style = MaterialTheme.typography.bodySmall
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxHeight(.8F)
        )
        Icon(
            Icons.Filled.ChatBubble,
            "",
            modifier = Modifier
                .height(iconHeight)
                .padding(end = 4.dp),
            tint = color.copy(alpha = .8F)
        )
        Text(
            price.comment.toString(), color = color, style = MaterialTheme.typography.bodySmall
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview(model: DetailViewModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        DetailScreen(0, model)
    }
}


@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=240",

    )
@Composable
private fun DetailScreenTabletPreview(model: DetailViewModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        DetailScreen(0, model)
    }
}




