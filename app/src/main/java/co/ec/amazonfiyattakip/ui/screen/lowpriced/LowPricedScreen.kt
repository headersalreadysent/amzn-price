package co.ec.amazonfiyattakip.ui.screen.lowpriced


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.ui.PreviewProviders
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.ProductWithStat
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.helper.helpers.LogHelper
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.core.graphics.drawable.toDrawable
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.part.MainProductCard
import co.ec.amazonfiyattakip.ui.part.TitleBar
import kotlin.collections.getOrNull

@Composable
fun LowPricedScreen(model: LowPricedViewModel = viewModel()) {
    val navigation = LocalNavigation.current
    val products by model.list.observeAsState()
    DisposableEffect(Unit) {
        model.productStats()
        onDispose { }
    }

    AppModel.setFab(Icons.Filled.Search) {
        navigation.navigate("find")
    }
    var selectedStat: ProductWithStat? by remember { mutableStateOf(null) }
    products?.let { products ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            val settings = LocalSettings.current
            val sort = listOf("Fiyat", "Ucuzluk")
            var statSort by remember {
                mutableStateOf(
                    settings.getString("statSort") ?: "Fiyat"
                )
            }
            var statSortDir by remember {
                mutableIntStateOf(
                    settings.getInt("statSortDir", 1)
                )
            }
            TitleBar(
                title = "Fiyat İstatistikleri",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(vertical = 4.dp)
                    .statusBarsPadding(),
                extra = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = .8F),
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                if (statSortDir == 1) {
                                    statSortDir = -1
                                } else {
                                    statSortDir = 1
                                    val index = sort.indexOf(statSort)
                                    statSort = sort.getOrNull(index + 1) ?: sort[0]
                                    settings.putString("statSort", statSort)
                                }
                                settings.putInt("statSortDir", statSortDir)
                            }
                            .padding(horizontal = 8.dp)) {
                        Text(
                            statSort,
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
                                .graphicsLayer(scaleY = -1 * statSortDir.toFloat())
                        )
                    }
                }
            )
            CutCornerCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Fiyat istatistikleri, tüm takip edilen günlerin ortalamalarına göre, fiyatın ucuz ya da pahalı olup olmadığını gösterir." +
                            " Fiyat çubuğu sağ tarafta ne kadar büyükse, ürün şu an o kadar ucuzdur.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textAlign = TextAlign.Justify
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .shadow(5.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 20.dp)
                    .weight(1F)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )


                products.sortedBy {
                    when (statSort) {
                        "Fiyat" -> it.entity.price.toFloat()
                        "Ucuzluk" -> if (it.max == it.min) 0.01F else ((it.entity.price - it.min).toFloat() / (it.max - it.min).toFloat())
                        else -> it.entity.id.toFloat()
                    }
                }.let {
                    if (statSortDir == -1) it.reversed() else it
                }.forEach { item ->
                    ProductPriceStatGraph(item, selected = {
                        selectedStat = it
                    })
                }

                val cheapProducts by remember {
                    mutableStateOf(products.filter { it.entity.price <= it.avg })
                }
                if (cheapProducts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    TitleBar(
                        title = "Ucuz Ürünler",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        cheapProducts.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(.50F)
                                    .aspectRatio(2.5F)
                                    .padding(
                                        start = if (index % 2 == 0) 0.dp else 4.dp,
                                        end = if (index % 2 == 1) 0.dp else 4.dp,
                                        bottom = 4.dp
                                    )
                            ) {
                                MainProductCard(
                                    ProductWithPrices(
                                        product = item.entity,
                                        priceInfoList = listOf()
                                    ), onClick = {
                                        navigation.navigate("detail/${item.entity.id}")
                                    })
                            }
                        }

                    }

                }


                val expensiveProducts by remember {
                    mutableStateOf(products.filter { it.entity.price > it.avg })
                }


                if (expensiveProducts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    TitleBar(
                        title = "Pahalı Ürünler",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        expensiveProducts.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(.50F)
                                    .aspectRatio(2.5F)
                                    .padding(
                                        start = if (index % 2 == 0) 0.dp else 4.dp,
                                        end = if (index % 2 == 1) 0.dp else 4.dp,
                                        bottom = 4.dp
                                    )
                            ) {
                                MainProductCard(
                                    ProductWithPrices(
                                        product = item.entity,
                                        priceInfoList = listOf()
                                    ), onClick = {
                                        navigation.navigate("detail/${item.entity.id}")
                                    })
                            }
                        }

                    }
                }

            }
        }
    }
    AppModel.cutCard(if (selectedStat == null) Modifier.height(30.dp) else Modifier.wrapContentHeight()) {
        selectedStat?.let {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .padding(top = 8.dp)
            ) {
                val titleStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = .8F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                val textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        "En Düşük",
                        style = titleStyle
                    )
                    Text(
                        it.min.price(),
                        style = textStyle
                    )
                }
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        "Ortalama",
                        style = titleStyle
                    )
                    Text(
                        it.avg.price(),
                        style = textStyle
                    )
                }
                Column {
                    Text(
                        "En Yüksek",
                        style = titleStyle
                    )
                    Text(
                        it.max.price(),
                        style = textStyle
                    )
                }
            }
        }

    }

}

@Composable
fun ProductPriceStatGraph(
    stat: ProductWithStat,
    showPrices: Boolean = false,
    selected: (selected: ProductWithStat?) -> Unit = {}
) {
    var showTitle by remember { mutableStateOf(false) }
    val paddingVertical by animateDpAsState(
        targetValue = if (showTitle) 4.dp else 0.dp,
        label = "rowPadding"
    )
    LaunchedEffect(showTitle) {
        selected(if (showTitle) stat else null)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = paddingVertical)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isPressed = event.changes.any { it.pressed }
                        showTitle = isPressed
                    }
                }
            }
    ) {
        val minSize by remember(stat) {
            mutableIntStateOf(
                stat.entity.price - stat.min
            )
        }
        val maxSize by remember(stat) {
            mutableIntStateOf(
                stat.max - stat.entity.price
            )
        }


        //calcaulate distances
        val maxDistance by remember(stat, minSize, maxSize) {
            val max = listOf(minSize, maxSize).max().toInt()
            mutableIntStateOf(if (max == 0) 1 else max)
        }

        val minRatio by remember(
            minSize,
            maxDistance
        ) { mutableFloatStateOf(if (maxDistance == 1) .5F else minSize.toFloat() / maxDistance.toFloat()) }
        val maxRatio by remember(
            maxSize,
            maxDistance
        ) { mutableFloatStateOf(if (maxDistance == 1) .5F else maxSize.toFloat() / maxDistance.toFloat()) }
        var width by remember { mutableIntStateOf(0) }
        var height by remember { mutableIntStateOf(0) }
        val errorContainer = MaterialTheme.colorScheme.error
        val primaryContainer = MaterialTheme.colorScheme.primary
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val titleHeight = with(LocalDensity.current) { 20.sp.toDp() }

            Text(
                stat.entity.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(titleHeight),
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
            )


            if (showPrices) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                ) {
                    Text(
                        stat.min.price(),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stat.entity.price.price(),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stat.avg.price(),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        stat.max.price(),
                        modifier = Modifier
                            .weight(1F)
                            .fillMaxHeight(),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
                horizontalArrangement = Arrangement.Center
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(.8F)
                        .height(20.dp)
                        .onGloballyPositioned {
                            width = it.size.width
                            height = it.size.height / 2
                        }
                ) {
                    if (minRatio < 1F) {
                        Box(
                            modifier = Modifier
                                .weight(1F - minRatio)
                                .fillMaxHeight()
                        )
                    }
                    if (minRatio > 0F && width > 0) {
                        Box(
                            modifier = Modifier
                                .weight(minRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.radialGradient(
                                        0.0f to errorContainer,
                                        1f to errorContainer.copy(alpha = .3F),
                                        radius = width * minRatio.toFloat(),
                                        center = Offset(
                                            width * minRatio.toFloat(),
                                            height.toFloat()
                                        ),
                                        tileMode = TileMode.Clamp
                                    )
                                )
                        )
                    }
                    val painter = if (LocalInspectionMode.current) {
                        // Show placeholder in Preview
                        ColorPainter(MaterialTheme.colorScheme.secondary)
                    } else {
                        rememberAsyncImagePainter(
                            model = ImageRequest.Builder(LocalContext.current)
                                .networkCachePolicy(CachePolicy.ENABLED)
                                .data(stat.entity.image)
                                .crossfade(true)
                                .transformations(CoilTrimTransform())
                                .error(Color.White.toArgb().toDrawable())
                                .listener(
                                    onError = { _, throwable ->
                                        LogHelper.e("coil error", throwable.throwable)
                                    }
                                )
                                .build()
                        )
                    }
                    Image(
                        painter = painter,
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(20.dp)
                            .aspectRatio(1F)
                    )

                    if (maxRatio > 0F && width > 0) {
                        Box(
                            modifier = Modifier
                                .weight(maxRatio)
                                .fillMaxHeight()
                                .background(
                                    Brush.radialGradient(
                                        0.0f to primaryContainer,
                                        1f to primaryContainer.copy(alpha = .3F),
                                        radius = width * maxRatio.toFloat(),
                                        center = Offset(0F, height.toFloat()),
                                        tileMode = TileMode.Clamp
                                    )
                                )
                        )
                    }
                    if (maxRatio < 1F) {
                        Box(
                            modifier = Modifier
                                .weight(1F - maxRatio)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LowPricedScreenPreview(model: LowPricedViewModel = viewModel()) {
    PreviewProviders {
        LowPricedScreen(model.apply { emulate() })
    }
}