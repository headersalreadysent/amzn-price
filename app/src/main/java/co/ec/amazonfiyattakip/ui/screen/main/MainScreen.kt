package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.ProductStat
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.helper.rememberBlink
import co.ec.amazonfiyattakip.helper.toHtml
import co.ec.amazonfiyattakip.helper.toHtmlWithAlpha
import co.ec.amazonfiyattakip.helper.topOuterShadow
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.composable.AutoText
import co.ec.helper.utils.dateString
import co.ec.helper.utils.html
import co.ec.helper.utils.timeString
import co.ec.helper.utils.unix


@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize()) {
        val productList by model.products.observeAsState()

        productList?.let { products ->
            if (products.isEmpty()) {
                NoProductScreen(model)
            } else {
                val dailyTotals by model.dailyTotals.observeAsState(listOf())
                val stats by model.stats.observeAsState(mapOf())
                ProductListScreen(products, dailyTotals, stats)
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NoProductScreen(
    model: MainScreenModel,
) {
    val navigator = LocalNavigation.current
    //load deals
    DisposableEffect(Unit) {
        model.loadDeals()
        onDispose {

        }
    }
    var deals by remember { mutableStateOf<List<Product>>(listOf()) }
    LaunchedEffect(Unit) {
        model.deals.collect { deal ->
            deals = deals + deal
        }
    }
    var selectedDeals by remember { mutableStateOf<List<String>>(listOf()) }
    val selectedList by remember(deals, selectedDeals) {
        mutableStateOf(deals.filter { selectedDeals.contains(it.asin) })
    }
    LaunchedEffect(selectedList) {
        if (selectedList.isEmpty()) {
            AppModel.setFab(Icons.Filled.Search, {
                navigator.navigate("find")
            })
        } else {
            AppModel.setFab(Icons.Filled.Check, {
                //add products to list
                model.addProductList(selectedList)
            })
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (deals.isEmpty()) {
                //if no deal
                Column(modifier = Modifier.fillMaxWidth(.8F)) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        "Fırsat ürünleri sorgulanıyor", modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 8.dp
                            ), style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            } else {
                Text(
                    "Fırsat Ürünleri", style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ), modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                FlowRow(
                    modifier = Modifier
                        .weight(1F)
                        .padding(horizontal = 4.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    deals.forEach {

                        var selected by remember { mutableStateOf(false) }
                        Box(modifier = Modifier
                            .fillMaxWidth(.5F)
                            .padding(4.dp)
                            .aspectRatio(2F)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .border(1.dp, MaterialTheme.colorScheme.primaryContainer)
                            .clickable {
                                selected = !selected
                                if (selected) {
                                    selectedDeals = selectedDeals + it.asin
                                } else {
                                    selectedDeals = selectedDeals.filter { a -> a != it.asin }
                                }
                            }
                            .drawWithContent {
                                drawContent()
                                if (selected) {
                                    drawRect(
                                        color = Color.Black.copy(alpha = 0.3f), // Adjust alpha for intensity
                                        blendMode = BlendMode.Multiply
                                    )
                                }
                            }) {
                            ProductImage(
                                it,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(.9F)
                                    .align(Alignment.CenterEnd)
                                    .alpha(.8F),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            )
                            Text(
                                it.shortTitle(50),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .padding(end = 16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            )
                            Text(
                                it.price(),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.BottomStart)
                                    .padding(end = 16.dp),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                )
                            )

                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .shadow(
                    1.dp,
                    cutShape(CutCorner.TOPRIGHT, 20.dp)
                )
                .padding(top = 1.dp)
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    cutShape(CutCorner.TOPRIGHT, 20.dp)
                )
                .statusBarsPadding()
                .aspectRatio(4F)

        ) {
            Crossfade(
                targetState = selectedDeals.isEmpty()
            ) {
                if (it) {

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Hiç ürün kaydedilmemiş.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )

                        Button(
                            onClick = {
                                navigator.navigate("find")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Filled.Add, "add product")
                            Text("Kendi Ürünümü Ekleyeyim")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(3F), horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                "Sepet toplamı",
                                modifier = Modifier.padding(bottom = 4.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            )
                            AutoText(
                                modifier = Modifier.fillMaxWidth(),
                                text = selectedList.sumOf { it.price }.price(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                        Column(
                            modifier = Modifier.weight(2F), horizontalAlignment = Alignment.End
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                selectedList.forEach {
                                    ProductImage(
                                        it,
                                        modifier = Modifier
                                            .padding(start = 5.dp)
                                            .padding(vertical = 2.dp)
                                            .width(25.dp)
                                            .aspectRatio(1F)
                                            .clip(CircleShape)
                                            .shadow(1.dp),
                                        showGradient = false
                                    )
                                }
                            }
                        }

                    }
                }

            }
        }
    }
}

@Composable
fun ProductListScreen(
    products: List<ProductWithPrices>,
    dailyTotals: List<DailyTotal>,
    stats: Map<String, Int>
) {
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxSize()) {

        val listState = rememberLazyListState()
        Box(
            modifier = Modifier
                .fillMaxSize(1F)
        ) {
            val navigator = LocalNavigation.current
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                state = listState,

                ) {
                items(products.size) { index ->
                    MainProductCard(
                        products[index],
                        isFirst = index == 0,
                        isLast = index == products.size - 1,
                        onClick = {
                            navigator.navigate("detail/${products[index].product.id}")
                        })

                }

            }
        }


        AppModel.cutCard(Modifier.aspectRatio(3.5F)) {
            var totalDragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
            Box(modifier = Modifier.fillMaxSize())
            {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.6F)
                        .align(Alignment.BottomEnd)
                ) {
                    PriceGraph(
                        modifier = Modifier.blur(.2.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = .8F),
                        circleColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        prices = dailyTotals.map {
                            return@map PriceGraphPair(it.date, it.total.toFloat())
                        },
                        hasCircles = false,
                        onDrag = {
                            totalDragValue = it
                        },
                        closePath = false,
                        drawStyle = Stroke(9F),
                        subRatio = .2F
                    )
                }

                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Sepet Toplamı",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                    AutoText(
                        if (totalDragValue != null) totalDragValue!!.price.toInt().price()
                        else if (dailyTotals.isEmpty()) 0.price() else dailyTotals.last().total.toInt()
                            .price(),
                        fontSize = 20..35,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    )
                    totalDragValue?.let {
                        Text(
                            it.date.dateString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.graphicsLayer {
                                translationY = with(density) { (-10).dp.toPx() }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    val titleStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 8.sp,
                        lineHeight = 8.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val numberStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End
                    )
                    if (stats.containsKey("product")) {

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {

                            Text((stats["product"] ?: 0).toString(), style = numberStyle)
                            Text("Ürün", style = titleStyle)
                        }
                    }
                    if (stats.containsKey("update")) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text((stats["update"] ?: 0).toString(), style = numberStyle)
                            Text("Fiyat", style = titleStyle)
                        }
                    }
                }
            }
        }

    }
}


@Composable
fun MainProductCard(
    productWithPrices: ProductWithPrices, isFirst: Boolean, isLast: Boolean,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    CutCornerCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 12.dp)
            .then(
                if (isFirst) Modifier.statusBarsPadding() else Modifier
            )
            .then(
                if (isLast) Modifier.padding(bottom = 45.dp) else Modifier
            ),
        click = { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        cutSize = 20.dp,
        shadow = 8.dp

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3F)
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
                    .fillMaxHeight(.8F)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                color = contentColor.copy(alpha = .8F),
                                fontSize = 11.sp
                            )
                        ) {
                            append(productWithPrices.product.asin + " ")
                        }
                        withStyle(
                            SpanStyle(
                                color = contentColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        ) {
                            append(productWithPrices.product.title)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AutoText(
                    text = productWithPrices.product.price(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(.4F)
                    .align(Alignment.BottomCenter)
            ) {
                Crossfade(targetState = productWithPrices.priceInfoList.size > 1) {

                    if (it) {
                        var selectedPair by remember { mutableStateOf<PriceGraphPair?>(null) }
                        val grouppedPrices by remember {
                            var group = productWithPrices.priceInfoList
                                .groupBy { it.date.dateString("yyyyMMdd") }
                            if (group.size == 1) {
                                group = productWithPrices.priceInfoList
                                    .groupBy { it.date.dateString("yyyyMMdd-hhmm") }
                            }


                            mutableStateOf(group.map {

                                val ave = it.value.toList().sumOf { it.price }
                                    .toFloat() / it.value.size
                                return@map PriceGraphPair(it.value[0].date, ave)
                            })
                        }
                        PriceGraph(
                            modifier = Modifier,
                            prices = grouppedPrices,
                            onDrag = { pair ->
                                selectedPair = pair
                            },
                            hasCircles = false,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = .4F),
                            subRatio = .2F
                        )
                        val textColor =
                            contentColorFor(MaterialTheme.colorScheme.secondary.copy(alpha = .8F))
                        selectedPair?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {

                                Text(
                                    "${it.date.dateString()} ${it.date.timeString()}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light,
                                        color = textColor
                                    )
                                )
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
                    } else {
                        val alpha by rememberBlink()
                        Text(
                            "Fiyat değişimleri bekleniyor.",
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(alpha),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Italic,
                                fontSize = 11.sp
                            )
                        )

                    }
                }

            }
            val contentOver = contentColorFor(contentColor)
            ProductStat(
                productWithPrices.product,
                modifier = Modifier.align(Alignment.BottomCenter),
                color = contentOver
            )

        }


        if (productWithPrices.priceInfoList.size > 1000) {
            val predict by remember { mutableStateOf(productWithPrices.predict()) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(IntrinsicSize.Max)
            ) {
                (0..3).forEach {
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .padding(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val date = (unix() + 7 * it * 86400).dateString()

                        Text(
                            date.replace(" 202", "\n202"),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Light,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                        AutoText(
                            predict[it].toInt().price(),
                            fontSize = 1..16
                        )
                    }
                    if (it < 3) {
                        VerticalDivider(
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }
        }


    }
}

@Composable
@Preview(showBackground = true)
fun MainScreenPreviewNoProduct(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        NoProductScreen(model)
    }
}

@Composable
@Preview(showBackground = true)
fun MainScreenPreview(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        MainScreen(model)
    }
}


