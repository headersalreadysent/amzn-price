package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.Progress
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.LittleProductBox
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.composable.AutoText
import co.ec.helper.utils.dateString
import co.ec.helper.utils.unix
import kotlin.math.absoluteValue


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {

    val navigation = LocalNavigation.current
    val settings = LocalSettings.current
    val dailyTotals by model.dailyTotals.observeAsState(listOf())
    val stats by model.stats.observeAsState(mapOf())
    val productList by model.products.observeAsState(null)
    Column(modifier = Modifier.fillMaxSize()) {
        var deals by remember { mutableStateOf<List<Product>>(listOf()) }
        LaunchedEffect(productList) {
            if (productList != null && productList!!.isEmpty()) {
                model.loadDeals(6)
            }
            model.deals.collect { deal ->
                deals = deals + deal
            }
        }
        var dealCount by remember { mutableIntStateOf(0) }
        val serverProducts by model.serverProducts.observeAsState()

        DisposableEffect(Unit) {
            model.collectServerProducts()
            model.getPopularCount {
                dealCount = it
            }
            onDispose { }
        }


        TopArea(productList)
        AnimatedVisibility(
            visible = dealCount != 0, enter = fadeIn() + expandVertically()
        ) {
            Column(modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.tertiaryContainer)
                .clickable {
                    navigation.navigate("find")
                }
                .padding(8.dp)
            ) {
                Text(
                    "Amazondaki fırsatları görerek hızlaca takip et.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
                Text(
                    "$dealCount adet fırsat var",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .8F)
                    )
                )
            }
        }
        ServerProducts(serverProducts)
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .shadow(5.dp)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1F)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {

            productList?.let { products ->
                val sort = listOf("Tarih", "Fiyat", "Son Güncelleme")
                var activeSort by remember {
                    mutableStateOf(
                        settings.getString("mainActiveSort") ?: "Tarih"
                    )
                }
                var sortDirection by remember {
                    mutableIntStateOf(
                        settings.getInt("mainActiveSortDirection",1)
                    )
                }
                if (products.isNotEmpty()) {
                    TitleBar(
                        title = "Takip Ürünler",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        extra = {
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable {
                                        if (sortDirection == 1) {
                                            sortDirection = -1
                                        } else {
                                            sortDirection = 1
                                            val index = sort.indexOf(activeSort)
                                            activeSort = sort.getOrNull(index + 1) ?: sort[0]
                                            settings.putString("mainActiveSort",activeSort)
                                        }
                                        settings.putInt("mainActiveSortDirection",sortDirection)
                                    }
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(3.dp))) {
                                Text(
                                    activeSort,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Icon(
                                    Icons.Outlined.Sort, "sort",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .scale(scaleY = .8F, scaleX = .5F)
                                        .graphicsLayer(scaleY = -1 * sortDirection.toFloat())
                                )
                            }
                        }
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize(),
                        maxItemsInEachRow = 2
                    ) {
                        products.let {
                            when (activeSort) {
                                "Tarih" -> it.sortedBy { it.product.date }
                                "Fiyat" -> it.sortedBy { it.product.price }
                                "Son Güncelleme" -> it.sortedBy { it.priceInfoList.lastOrNull()?.date }
                                else -> it
                            }
                        }.let {
                            if (sortDirection == -1) it.reversed() else it
                        }.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(.5F)
                                    .aspectRatio(2.5F)
                                    .padding(
                                        start = if (index % 2 == 0) 8.dp else 4.dp,
                                        end = if (index % 2 == 1) 8.dp else 4.dp
                                    )

                            ) {
                                MainProductCard(item, onClick = {
                                    navigation.navigate("detail/${productList!![index].product.id}")
                                })
                            }


                        }

                    }
                }
                if (products.isEmpty() && deals.isNotEmpty()) {
                    TitleBar(
                        title = "Amazon Fırsatlar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp)
                    ) {
                        deals.forEach {
                            LittleProductBox(it,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                onClick = {
                                    navigation.navigate("add/${it.asin}")
                                })
                        }
                        if (deals.count() < dealCount) {
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    navigation.navigate("find")
                                }) {
                                Text("Tüm Fırsatları Görüntüle")
                            }
                        }
                    }
                }

            }
        }
    }



    AppModel.cutCard(Modifier.aspectRatio(3.5F)) {
        productList?.let { products ->
            if (products.isNotEmpty()) {
                var totalDragValue by remember { mutableStateOf<PriceGraphPair?>(null) }

                Box(modifier = Modifier.fillMaxSize()) {
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
                            "Sepet Toplamı", style = MaterialTheme.typography.bodyMedium.copy(
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
                            Text(it.date.dateString(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.graphicsLayer {
                                    translationY = with(density) { (-10).dp.toPx() }
                                })
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
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        val numberStyle = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
            } else {
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
                            navigation.navigate("find")
                        }, colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ), modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, "add product")
                        Text("Kendi Ürünümü Ekleyeyim")
                    }
                }
            }
        }

    }
}

@Composable
fun TopArea(productList: List<ProductWithPrices>?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Amazon",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    lineHeight = 30.sp
                )
            )
            Text(
                "Fiyat Takibi",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 12.sp
                ),
                modifier = Modifier.offset(y = (-3).dp)
            )
        }
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                "Ucuz ürünler",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                productList?.forEach {

                    ProductImage(
                        product = it.product,
                        modifier = Modifier
                            .height(25.dp)
                            .aspectRatio(1F)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary
                    )
                }


            }
        }
    }
}

@Composable
fun ServerProducts(serverProducts: List<Pair<Product, List<String>>>?) {
    AnimatedVisibility(
        visible = serverProducts !== null, enter = fadeIn() + expandVertically()
    ) {
        val navigator = LocalNavigation.current
        val shape = cutShape(CutCorner.BOTTOMRIGHT, 5.dp)
        serverProducts?.let { serverProducts ->
            Column(modifier = Modifier.fillMaxWidth()) {
                TitleBar(
                    title = "Hazır Takipli Ürünler",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
                LazyRow(
                    modifier = Modifier.padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(serverProducts.size) {
                        val pair = serverProducts[it]
                        Box(modifier = Modifier
                            .fillParentMaxWidth(.55F)
                            .height(IntrinsicSize.Max)
                            .padding(
                                start = if (it == 0) 8.dp else 0.dp,
                                end = if (it == serverProducts.size - 1) 8.dp else 0.dp
                            )
                            .background(MaterialTheme.colorScheme.tertiaryContainer, shape)
                            .clickable {
                                navigator.navigate("add/${pair.first.asin}")
                            }
                            .clip(shape)) {

                            PriceGraph(modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(.8F)
                                .align(Alignment.BottomCenter),
                                prices = pair.second.map { it.split("|") }.map {
                                    PriceGraphPair(it[0].toLong(), it[1].toFloat())
                                },
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = .5F),
                                hasCircles = false
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                Text(
                                    pair.first.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    pair.first.price(),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 5.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Medium
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


@Composable
fun MainProductCard(
    productWithPrices: ProductWithPrices,
    onClick: () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp),
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
                .aspectRatio(2.5F)
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
                    productWithPrices.product.title + "\n",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    style = TextStyle(
                        lineHeight = 18.sp,
                        shadow = Shadow(MaterialTheme.colorScheme.primary, Offset(1F, 1F), 1F)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,

                    )
                AutoText(
                    text = productWithPrices.product.price(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                        .padding(horizontal = 8.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold
                    )
                )
            }


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


@Composable
@Preview(showBackground = true)
private fun MainScreenPreview(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        MainScreen(model)
    }
}

