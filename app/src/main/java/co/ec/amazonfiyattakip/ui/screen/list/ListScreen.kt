package co.ec.amazonfiyattakip.ui.screen.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TrendingDown
import androidx.compose.material.icons.automirrored.outlined.TrendingFlat
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FilterAltOff
import androidx.compose.material.icons.outlined.Money
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.App
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.DateRow
import co.ec.amazonfiyattakip.composables.ExtrasArea
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.amazonfiyattakip.ui.screen.detail.PriceStat
import co.ec.amazonfiyattakip.ui.screen.detail.TreePriceRow
import co.ec.helper.composable.AutoText
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString

@Composable
fun ListScreen(model: ListScreenModel = viewModel()) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(listState)

    val products by model.products.observeAsState(listOf())
    DisposableEffect(products) {
        products.firstOrNull()?.let { product ->
            AppModel.cutCard {
                SubProductCard(product.product)
            }
        }
        onDispose {
        }
    }


    LaunchedEffect(products, listState) {
        LogHelper.d("list item ${products.map { it.product.id }}", "detail list")
        mostVisibleChange(listState) {
            products.getOrNull(it)?.let { product ->
                AppModel.cutCard {
                    SubProductCard(product.product)
                }
            }
        }

    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        state = listState,
        flingBehavior = flingBehavior,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(products.size) {
            val product = products[it]
            CutCornerCard(
                modifier = Modifier
                    .fillParentMaxWidth(.8F)
                    .fillMaxHeight()
                    .padding(start = if (it == 0) 8.dp else 0.dp)
                    .padding(end = if (it == products.size - 1) 8.dp else 0.dp),
                corner = CutCorner.BOTTOMRIGHT,
                cutSize = 10.dp,
                shadow = 8.dp
            ) {
                ProductDetail(
                    product
                )
            }
        }


    }


}

@Composable
fun ProductDetail(
    productWithPrices: ProductWithPrices,
) {
    var showOnlyChanges by remember { mutableStateOf(true) }
    var graphShow by remember { mutableIntStateOf(0) }
    val priceListData by remember(showOnlyChanges, graphShow) {
        //generate list by showChanges and graph item
        var lastPrice = -1F
        mutableStateOf(productWithPrices.priceInfoList.map {
            PriceGraphPair(
                it.date,
                when (graphShow) {
                    0 -> it.price / 100F
                    1 -> it.comment.toFloat()
                    2 -> it.star.toFloat()
                    else -> it.price / 100F
                }
            )
        }.filter {
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

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth(), Alignment.TopEnd){
            CutCornerCard(
                modifier = Modifier.wrapContentWidth(),
                corner = CutCorner.BOTTOMLEFT,
                cutSize = 5.dp,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentWidth(),
                    horizontalArrangement = Arrangement.End
                ) {

                    Row(modifier = Modifier
                        .wrapContentWidth()
                        .clickable {
                            graphShow = if (graphShow == 2) 0 else graphShow + 1
                        }
                        .padding(horizontal = 8.dp)
                        .clip(cutShape(CutCorner.BOTTOMLEFT, 5.dp)),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = listOf("Fiyat", "Yorum", "Puan")[graphShow],
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontStyle = FontStyle.Italic
                            )
                        )
                        Icon(
                            listOf(
                                Icons.Outlined.Money,
                                Icons.Outlined.Comment,
                                Icons.Outlined.Star
                            )[graphShow],
                            contentDescription = "filter",
                            modifier = Modifier.scale(.7F),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(modifier = Modifier
                        .wrapContentWidth()
                        .clickable {
                            showOnlyChanges = !showOnlyChanges
                        }
                        .padding(horizontal = 8.dp)
                        .clip(cutShape(CutCorner.BOTTOMLEFT, 5.dp)),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (showOnlyChanges) "(Değişimler)" else "(Tüm)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontStyle = FontStyle.Italic
                            )
                        )
                        Icon(
                            if (showOnlyChanges) Icons.Outlined.FilterAlt else Icons.Outlined.FilterAltOff,
                            contentDescription = "filter",
                            modifier = Modifier.scale(.7F),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.2F)
        ) {
            var dragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
            PriceGraph(
                modifier = Modifier
                    .align(Alignment.BottomEnd),
                prices = priceListData,
                onDrag = {
                    dragValue = it
                },
                hasCircles = false,
                closePath = false,
                drawStyle = Stroke(6F)
            )

            Column(
                modifier = Modifier
                    .padding(end = 20.dp, start = 8.dp)
                    .padding(top = 16.dp)
                    .wrapContentWidth()
                    .align(Alignment.TopStart)
            ) {
                dragValue?.let {
                    Text(
                        text = it.date.dateString() + " " + it.date.timeString(),
                        style = MaterialTheme.typography.bodySmall.copy(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.8F)
        ) {

            TreePriceRow(prices = productWithPrices.priceInfoList)
            HorizontalDivider()
            Text(
                text = productWithPrices.product.shortDesc(300),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Justify
                ),
                modifier = Modifier.padding(8.dp)
            )
            HorizontalDivider()
            priceListData.reversed().let { list ->
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    list.forEachIndexed { index, it ->
                        val prevPrice = list.getOrNull(index + 1)?.price ?: 0F
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp)
                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                .height(30.dp)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (graphShow == 0) (it.price * 100).toInt()
                                        .price() else it.price.toInt().toString(),
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
                                        .padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(
                                        alpha = .8F
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
fun SubProductCard(product: Product) {
    val containerColor = MaterialTheme.colorScheme.secondaryContainer
    val contentColor = contentColorFor(containerColor)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3F)
    ) {
        ProductImage(
            product,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .align(Alignment.TopEnd),
            color = containerColor,
            radialGradient = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .padding(end = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium.copy(

                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            )
            Text(
                product.asin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(top = 8.dp),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = contentColor.copy(alpha = .8F),
                    fontSize = 11.sp
                )
            )
            AutoText(
                text = product.price(),
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
    }
}


@Preview(showBackground = true)
@Composable
private fun SubProductCardPreview() {
    PreviewProviders {
        SubProductCard(Product.fake())
    }
}

@Preview(showBackground = true)
@Composable
private fun ListScreenPreview() {
    PreviewProviders {
        val model = ListScreenModel()
        model.emulate()
        ListScreen(model)
    }
}


@Preview(showBackground = true)
@Composable
private fun ProductDetailPreview() {
    PreviewProviders {
        val model = ListScreenModel()
        model.emulate()
        model.products.value?.get(0)?.let {
            ProductDetail(it)
        }

    }
}


suspend fun mostVisibleChange(state: LazyListState, then: (index: Int) -> Unit = {}) {

    var mostVisibleItemIndex = -1
    snapshotFlow { state.layoutInfo }
        .collect { layoutInfo ->
            val viewportStart = layoutInfo.viewportStartOffset
            val viewportEnd = layoutInfo.viewportEndOffset

            val mostVisibleItem = layoutInfo.visibleItemsInfo.maxByOrNull { item ->
                val itemStart = item.offset
                val itemEnd = item.offset + item.size
                val visibleStart = maxOf(itemStart, viewportStart)
                val visibleEnd = minOf(itemEnd, viewportEnd)
                visibleEnd - visibleStart
            }

            mostVisibleItem?.index?.let { index ->
                if (mostVisibleItemIndex != index) {
                    mostVisibleItemIndex = index
                    LogHelper.d("first visible $index", "detail list")
                    then(index)
                }
            }
        }

}