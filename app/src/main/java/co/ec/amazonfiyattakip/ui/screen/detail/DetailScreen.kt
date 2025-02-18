package co.ec.amazonfiyattakip.ui.screen.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
import co.ec.amazonfiyattakip.composables.CalendarScreen
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.DateRow
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.ProductStat
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.price_info.PriceInfo
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.db.product.ProductStatus
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.helper.rememberBlink
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.LocalSnackbar
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.composable.AutoText
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

@Composable
fun DetailScreen(
    productId: Int? = null, model: DetailViewModel = viewModel()
) {
    val urlHandler = LocalUriHandler.current
    val product by model.product.observeAsState()
    val prices by model.prices.observeAsState()
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

        Box(modifier = Modifier.fillMaxSize()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 80.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .aspectRatio(2F)
                        .shadow(1.dp)
                ) {
                    ProductImage(
                        product,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth(.5F)
                            .aspectRatio(1F)
                            .align(Alignment.CenterEnd),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(.8F)
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp)
                            .statusBarsPadding()
                    ) {
                        Text(
                            text = product.title, style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            ), maxLines = 3, overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp),
                            text = product.asin,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        AutoText(
                            modifier = Modifier.fillMaxWidth(),
                            text = product.price(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                        ProductStat(
                            product, color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                }


                if (prices == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
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
                        TreePriceRow(it)
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        CalendarPriceData(it)
                        val coroutine = rememberCoroutineScope()
                        val snackbar = LocalSnackbar.current


                        TitleBar(
                            title = "Fiyat Değişimi",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .clickable(
                                    indication = null, interactionSource = null
                                ) {
                                    showOnlyChanges = !showOnlyChanges
                                    coroutine.launch {
                                        snackbar.showSnackbar(
                                            if (showOnlyChanges)
                                                "Sadece değişimler gösteriliyor" else
                                                "Tüm sorgular gösteriliyor",
                                            duration = SnackbarDuration.Short
                                        )

                                    }
                                },
                            extra = {
                                Icon(
                                    if (showOnlyChanges) Icons.Outlined.FilterAlt else Icons.Outlined.FilterAltOff,
                                    contentDescription = "filter",
                                    modifier = Modifier.scale(.7F),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            })
                        var showPrice by remember { mutableStateOf(true) }
                        priceListData.reversed().let { list ->

                            Column(modifier = Modifier
                                .padding(8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = null
                                ) {
                                    showPrice = !showPrice
                                }) {
                                list.forEachIndexed { index, it ->
                                    val prevPrice=if(list.size>index+1){
                                        list[index+1].price
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
                                                    if(prevPrice<it.price){
                                                        Icons.AutoMirrored.Outlined.TrendingUp
                                                    } else {
                                                        Icons.AutoMirrored.Outlined.TrendingDown
                                                    },
                                                    contentDescription = "trend",
                                                    modifier = Modifier.padding(start = 4.dp).scale(.6F),
                                                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .8F)
                                                )

                                            }

                                        } else {
                                            PriceStat(it)
                                        }
                                    }
                                }
                            }

                        }

                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))


                CutCornerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                ExtrasArea(
                    product = product,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (product.status == ProductStatus.ACTIVE) {
                    OutlinedButton(
                        onClick = {
                            model.stopFollow()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = "Takibi Durdur")
                    }
                }
                if (product.status == ProductStatus.PASSIVE) {
                    OutlinedButton(
                        onClick = {
                            model.startFollow()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "Takibi Başlat")
                    }
                }


            }
            val cutCorner = cutShape(CutCorner.TOPRIGHT, 30.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(5F)
                    .align(Alignment.BottomStart)
                    .background(MaterialTheme.colorScheme.secondaryContainer, cutCorner)
                    .shadow(1.dp, cutCorner)
            ) {
                PricesGraphWithDrag(
                    prices = priceListData
                )
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Text(
                        text = (it.price * 100).toInt().price(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
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

@Composable
fun CalendarPriceData(prices: List<PriceInfo>) {
    //get scpoe
    val coroutine = rememberCoroutineScope()
    val snackbar = LocalSnackbar.current
    var calendarView by remember { mutableStateOf(false) }
    TitleBar(title = "Günlük Fiyatlar",
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(
                indication = null, interactionSource = null
            ) {
                //change view type
                calendarView = !calendarView
            },
        extra = {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = "days",
                modifier = Modifier.scale(.7F),
                tint = MaterialTheme.colorScheme.primary
            )
        })
    Crossfade(targetState = calendarView) { state ->
        if (!state) {
            //date row or
            DateRow(
                priceList = prices.map { Pair(it.date.toInt(), it.price) }.toMap(),
            ) {
                coroutine.launch {
                    snackbar.showSnackbar(
                        "${
                            it.first.toLong().dateString()
                        } ortalama fiyat ${it.second.price()}"
                    )
                }
            }
        } else {
            //or full calendar
            CalendarScreen(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 8.dp),
                priceList = prices.map { Pair(it.date.dateString(), it.price) }.toMap()
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

