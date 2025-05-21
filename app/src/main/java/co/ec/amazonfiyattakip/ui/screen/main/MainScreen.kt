package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.LowPriced
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.PermissionHelper
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.LittleProductBox
import co.ec.amazonfiyattakip.ui.part.MainProductCard
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.composable.AutoText
import co.ec.helper.helpers.LogHelper
import co.ec.helper.utils.dateString


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {

    val navigation = LocalNavigation.current
    val settings = LocalSettings.current
    val productList by model.products.observeAsState(null)
    val stats by model.stats.observeAsState(null)

    AppModel.setFab(Icons.Filled.Search) {
        navigation.navigate("find")
    }
    Column(modifier = Modifier.fillMaxSize()) {
        var deals by remember { mutableStateOf<List<Product>>(listOf()) }
        var dealCount by remember { mutableStateOf<Int?>(null) }
        val lowPricedProducts by model.lowPriced.observeAsState(listOf())
        val serverProducts by model.serverProducts.observeAsState(null)
        DisposableEffect(Unit) {
            model.loadProducts()
            //load start datas
            val showServerProducts = settings.getBoolean("showServerProducts", true)
            if (showServerProducts) {
                model.collectServerProducts()
            }
            //set dealcount if user wants to see it
            val showDealsInfo = settings.getBoolean("showDealsInfo", true)
            if (showDealsInfo) {
                model.getPopularCount {
                    dealCount = it
                }
            }
            onDispose { }
        }
        LaunchedEffect(productList) {
            if (productList != null && productList!!.isEmpty()) {
                model.loadDeals(6)
            }
            model.deals.collect { deal ->
                deals = deals + deal
            }
        }
        TopArea(lowPricedProducts)
        stats?.let { stat ->
            SlowQueryArea(stat)
        }
        DealCountArea(dealCount)
        serverProducts?.let {
            ServerProductsArea(serverProducts)
        }

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
                        settings.getInt("mainActiveSortDirection", 1)
                    )
                }
                if (products.isNotEmpty()) {
                    TitleBar(
                        title = "Takip Ürünler",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        extra = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = .8F
                                        ), RoundedCornerShape(8.dp)
                                    )
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (sortDirection == 1) {
                                            sortDirection = -1
                                        } else {
                                            sortDirection = 1
                                            val index = sort.indexOf(activeSort)
                                            activeSort = sort.getOrNull(index + 1) ?: sort[0]
                                            settings.putString("mainActiveSort", activeSort)
                                        }
                                        settings.putInt("mainActiveSortDirection", sortDirection)
                                    }
                                    .padding(horizontal = 8.dp)) {
                                Text(
                                    activeSort,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    Icons.Filled.ArrowDropDown, "sort",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .graphicsLayer(scaleY = -1 * sortDirection.toFloat())
                                )
                            }
                        }
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 30.dp),
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
                                    navigation.navigate("detail/${item.product.id}")
                                })
                            }


                        }

                    }
                }
                if (products.isEmpty() && deals.isNotEmpty()) {
                    //no product but deal
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
                            LittleProductBox(
                                it,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                onClick = {
                                    navigation.navigate("add/${it.asin}")
                                })
                        }
                        if (deals.count() < (dealCount ?: 0)) {
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

    var showTotal = settings.getBoolean("showBasketTotal", false)
    val height = if (productList.orEmpty().isEmpty() || showTotal) {
        Modifier.aspectRatio(3.5F)
    } else {
        Modifier.height(30.dp)
    }
    AppModel.cutCard(height) {
        productList?.let { products ->
            if (products.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (showTotal) {
                        var totalDragValue by remember { mutableStateOf<PriceGraphPair?>(null) }
                        val dailyTotals by model.dailyTotals.observeAsState(listOf())
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
                                Text(
                                    it.date.dateString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.graphicsLayer {
                                        translationY = with(density) { (-10).dp.toPx() }
                                    })
                            }
                        }

                        stats?.let {
                            StatArea(it)
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
fun TopArea(lowPricedProducts: List<LowPriced>) {
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
        if (lowPricedProducts.isNotEmpty()) {
            val navigation = LocalNavigation.current
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clickable(indication = null, interactionSource = null) {
                        navigation.navigate("lowpriced")
                    }
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
                    lowPricedProducts.let {
                        if(lowPricedProducts.size>5) lowPricedProducts.slice(0..3) else lowPricedProducts
                    }.forEach {
                        ProductImage(
                            title = it.title,
                            image = it.image,
                            modifier = Modifier
                                .height(25.dp)
                                .aspectRatio(1F)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if(lowPricedProducts.size>5){
                        Text("+${lowPricedProducts.size-3}",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 11.sp,
                            lineHeight = with(LocalDensity.current) { 25.dp.toSp()  },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5F),CircleShape)
                                .height(25.dp)
                                .aspectRatio(1F)
                                .clip(CircleShape),)
                    }


                }
            }
        }

    }
}

@Composable
fun ServerProductsArea(serverProducts: List<Pair<Product, List<String>>>?) {
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
                        Box(
                            modifier = Modifier
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

                            PriceGraph(
                                modifier = Modifier
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
                                    pair.first.title + "\n",
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
fun DealCountArea(dealCount: Int? = null) {
    val navigation = LocalNavigation.current
    AnimatedVisibility(
        visible = (dealCount ?: 0) > 0,
        enter = fadeIn() + expandVertically()
    ) {
        Column(
            modifier = Modifier
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
}

@Composable
fun SlowQueryArea(stat: Map<String, Int>) {
    if (PermissionHelper.isIgnoringBattery()) {
        return
    }
    if (stat.containsKey("querySpan")) {
        var span = stat["querySpan"]
        var targetTime = LocalSettings.current.getInt("queryTime", 15) * 60
        if (span != null) {
            var visible by remember { mutableStateOf(true) }
            if (span > targetTime * 1.1F && visible) {
                LogHelper.i("querySpan $span $targetTime")
                CutCornerCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable {
                            visible = false
                            PermissionHelper.batteryPermission()
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    val text = buildAnnotatedString {
                        appendInlineContent("battery", " ")
                        append(
                            "Fiyat sorgulaması hedeflenen zamandan yavaş çalışıyor." +
                                    " Bu durum batarya optimizasyonundan kaynaklanıyor olabilir." +
                                    " Uygulamayı kısıtlanmamış ayarlayarak daha iyi sorgulama elde edebilirsiniz."
                        )
                    }
                    Text(
                        text,
                        inlineContent = mapOf(
                            "battery" to InlineTextContent(
                                Placeholder(11.sp, 11.sp, PlaceholderVerticalAlign.Center)
                            ) {
                                Icon(
                                    Icons.Default.BatteryAlert,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            },
                        ),
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall.copy(
                            textAlign = TextAlign.Justify,
                            fontSize = 11.sp
                        )
                    )

                }
            }
        }

    }
}

@Composable
fun BoxScope.StatArea(stat: Map<String, Int>) {
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
        if (stat.containsKey("product")) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text((stat["product"] ?: 0).toString(), style = numberStyle)
                Text("Ürün", style = titleStyle)
            }
        }
        if (stat.containsKey("update")) {
            Column(horizontalAlignment = Alignment.End) {
                Text((stat["update"] ?: 0).toString(), style = numberStyle)
                Text("Fiyat", style = titleStyle)
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

