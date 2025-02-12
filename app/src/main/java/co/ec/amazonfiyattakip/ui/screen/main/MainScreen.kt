package co.ec.amazonfiyattakip.ui.screen.main

import android.icu.text.CaseMap.Title
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
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
import co.ec.amazonfiyattakip.db.DailyTotal
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.predictNextPrices
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.composable.AutoText
import co.ec.helper.utils.dateString
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
            AppModel.setFab(Icons.Filled.Add, {})
        } else {
            AppModel.setFab(Icons.Filled.Check, {
                //add products to list
                model.addProductList(selectedList)
            })
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                .background(
                    MaterialTheme.colorScheme.primary,
                    cutShape(CutCorner.TOPRIGHT, 20.dp)
                )
                .shadow(
                    1.dp,
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
                        Text("Hiç ürün kaydedilmemiş.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            ))
                        Button(
                            onClick = {

                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onPrimary,
                                contentColor = MaterialTheme.colorScheme.primary
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
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            )
                            AutoText(
                                modifier = Modifier.fillMaxWidth(),
                                text = selectedList.sumOf { it.price }.price(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimary
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductListScreen(
    products: List<ProductWithPrices>,
    dailyTotals: List<DailyTotal>,
    stats: Map<String, Int>
) {
    val density = LocalDensity.current
    Column (modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp)
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .aspectRatio(3F)
        ) {
            var totalDragValue by remember { mutableStateOf<PriceGraphPair?>(null) }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.8F)
                        .align(Alignment.BottomEnd)
                ) {
                    PriceGraph(
                        modifier = Modifier,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .5F),
                        prices = dailyTotals.map {
                            return@map PriceGraphPair(it.date, it.total.toFloat())
                        },
                        hasCircles = false,
                        onDrag = {
                            totalDragValue = it
                        }
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
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    AutoText(
                        if (totalDragValue != null) totalDragValue!!.price.toInt().price()
                        else if (dailyTotals.isEmpty()) 0.price() else dailyTotals.last().total.toInt()
                            .price(),
                        fontSize = 20..35,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    totalDragValue?.let {
                        Text(
                            it.date.dateString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
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
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    val numberStyle = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
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
        Box(
            modifier = Modifier
                .fillMaxSize(1F)
        ) {
            val listState = rememberLazyListState()
            val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
            val navigator = LocalNavigation.current
            LazyRow(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                state = listState,
                flingBehavior = flingBehavior,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(products.size) { index ->
                    val productWithPrices = products[index]

                    CutCornerCard(
                        modifier = Modifier
                            .fillParentMaxWidth(.8F)
                            .padding(start = 8.dp)
                            .padding(end = if (index == products.size - 1) 8.dp else 0.dp)
                            .fillMaxHeight()
                            .clickable {
                                navigator.navigate("detail/${productWithPrices.product.id}")
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        cutSize = 20.dp,
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = 15.dp
                        )

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
                                    .align(Alignment.CenterEnd),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            )
                            Row(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .height(IntrinsicSize.Max)
                                    .align(Alignment.BottomEnd),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Star, "",
                                    modifier = Modifier.height(with(density) { 12.sp.toDp() })
                                )
                                Text(
                                    productWithPrices.product.star.toString(),
                                    fontSize = 10.sp
                                )
                                VerticalDivider(
                                    modifier = Modifier.padding(2.dp)
                                )
                                Icon(
                                    Icons.Filled.ChatBubble, "",
                                    modifier = Modifier.height(with(density) { 12.sp.toDp() })
                                )
                                Text(
                                    productWithPrices.product.comment.toString(),
                                    fontSize = 10.sp
                                )
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(.8F)
                                    .padding(8.dp)
                            ) {

                                Text(
                                    productWithPrices.product.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    productWithPrices.product.asin,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Light
                                    ),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                AutoText(
                                    text = productWithPrices.product.price(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(3F)
                        ) {
                            var selectedPair by remember { mutableStateOf<PriceGraphPair?>(null) }
                            PriceGraph(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClick = {

                                        }
                                    ),
                                prices = productWithPrices.priceInfoList.map {
                                    return@map PriceGraphPair(it.date, it.price.toFloat())
                                },
                                onDrag = { pair ->
                                    selectedPair = pair
                                }
                            )
                            selectedPair?.let {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {

                                    Text(
                                        "${it.date.dateString()} ${it.date.timeString()}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Light
                                        )
                                    )
                                    Text(
                                        it.price.toInt().price(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                            }
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        if (productWithPrices.priceInfoList.size > 10) {
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


                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1F)
                                .verticalScroll(rememberScrollState())
                        ) {

                            Text(
                                productWithPrices.product.description,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textAlign = TextAlign.Justify
                                )
                            )
                            val extras = productWithPrices.product.extraMap().toList()
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                maxItemsInEachRow = 2,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                extras.forEach {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(.48F)
                                            .padding(vertical = 2.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surfaceColorAtElevation(
                                                    2.dp
                                                )
                                            )
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            it.first,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp
                                            )
                                        )
                                        Text(
                                            it.second,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 13.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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


