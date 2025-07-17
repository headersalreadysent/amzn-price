package co.ec.amazonfiyattakip.ui.screen.lowpriced


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.AppModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.ProductWithStat
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.ExpertMode
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.MainProductCard
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.screen.detail.ModalContent
import co.ec.helper.helpers.LogHelper
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.android.gms.common.util.DeviceProperties.isPhone
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LowPricedScreen(model: LowPricedViewModel = viewModel()) {
    val navigation = LocalNavigation.current
    val settings = LocalSettings.current
    val density = LocalDensity.current
    val products by model.list.observeAsState()
    var lowPriceGraphInfoCardVisible by remember { mutableStateOf<Boolean?>(null) }
    DisposableEffect(Unit) {
        model.productStats()
        lowPriceGraphInfoCardVisible = settings.getBoolean("showLowPriceDetailInfoCard", true)
        onDispose { }
    }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showBottomSheet by remember { mutableStateOf(false) }

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
                    if (ExpertMode.current) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

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
                                    statSort, style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    "sort",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.graphicsLayer(scaleY = -1 * statSortDir.toFloat())
                                )
                            }
                            lowPriceGraphInfoCardVisible?.let {
                                if (!it) {
                                    Icon(
                                        Icons.Outlined.Info,
                                        "",
                                        modifier = Modifier
                                            .padding(start = 8.dp)
                                            .clickable(
                                                indication = null, interactionSource = null
                                            ) {
                                                showBottomSheet = true
                                            })
                                }
                            }

                        }
                    }
                })
            lowPriceGraphInfoCardVisible?.let {
                if (it) {
                    CutCornerCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "Fiyat istatistikleri, tüm takip edilen günlerin ortalamalarına göre, fiyatın ucuz ya da pahalı olup olmadığını gösterir." + " Fiyat çubuğu sağ tarafta ne kadar büyükse, ürün şu an o kadar ucuzdur.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Justify
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                        OutlinedButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .padding(horizontal = 8.dp)
                                .padding(bottom = 4.dp),
                            contentPadding = PaddingValues(2.dp),
                            onClick = {
                                lowPriceGraphInfoCardVisible = false
                                settings.putBoolean("showLowPriceDetailInfoCard", false)
                                showBottomSheet = true
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    "info",
                                    modifier = Modifier
                                        .padding(end = 1.dp)
                                        .scale(.8F)
                                )
                                Text("Detaylı Oku")
                            }
                        }
                    }
                }
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
                    .padding(bottom = 30.dp)
                    .weight(1F)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )

                products.run {
                    if (ExpertMode.current) {
                        this.sortedBy {
                            when (statSort) {
                                "Fiyat" -> it.entity.price.toFloat()
                                "Ucuzluk" -> if (it.max == it.min) 0.01F else ((it.entity.price - it.min).toFloat() / (it.max - it.min).toFloat())
                                else -> it.entity.id.toFloat()
                            }
                        }.let {
                            if (statSortDir == -1) it.reversed() else it
                        }
                    } else {
                        this
                    }
                }.forEach { item ->
                    ProductPriceStatGraph(item, selected = {
                        selectedStat = it
                    })
                }

                val cheapProducts by remember {
                    mutableStateOf(products.filter { it.entity.price <= it.avg }.sortedBy {
                        when (statSort) {
                            "Fiyat" -> it.entity.price.toFloat()
                            "Ucuzluk" -> if (it.max == it.min) 0.01F else ((it.entity.price - it.min).toFloat() / (it.max - it.min).toFloat())
                            else -> it.entity.id.toFloat()
                        }
                    }.let {
                        if (statSortDir == -1) it.reversed() else it
                    })
                }
                if (cheapProducts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    TitleBar(
                        title = "Ucuz Ürünler", style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ), modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isCompact = this.maxWidth < 600.dp
                        val boxCount = if (isCompact) 2 else 3
                        var itemSize by remember { mutableStateOf(0.dp) }

                        FlowRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 8.dp)
                                .onSizeChanged {
                                    val width = density.run { it.width.toDp() }
                                    if (width > 0.dp) {
                                        itemSize = (width - 8.dp * (boxCount - 1)) / boxCount
                                    }
                                },
                            maxItemsInEachRow = boxCount,

                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            cheapProducts.forEachIndexed { index, item ->

                                Box(
                                    modifier = Modifier
                                        .width(itemSize)
                                        .aspectRatio(2.5F)
                                ) {
                                    MainProductCard(
                                        ProductWithPrices(
                                            product = item.entity, priceInfoList = listOf()
                                        ), onClick = {
                                            navigation.navigate("detail/${item.entity.id}")
                                        })
                                }
                            }

                        }
                    }


                }


                val expensiveProducts by remember {
                    mutableStateOf(products.filter { it.entity.price > it.avg }.sortedBy {
                        when (statSort) {
                            "Fiyat" -> it.entity.price.toFloat()
                            "Ucuzluk" -> if (it.max == it.min) 0.01F else ((it.entity.price - it.min).toFloat() / (it.max - it.min).toFloat())
                            else -> it.entity.id.toFloat()
                        }
                    }.let {
                        if (statSortDir == -1) it.reversed() else it
                    })
                }


                if (expensiveProducts.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    TitleBar(
                        title = "Pahalı Ürünler", style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ), modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    )
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {

                        val isCompact = this.maxWidth < 600.dp
                        val boxCount = if (isCompact) 2 else 3
                        var itemSize by remember { mutableStateOf(0.dp) }
                        val density = LocalDensity.current
                        FlowRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 8.dp)
                                .onSizeChanged {
                                    val width = density.run { it.width.toDp() }
                                    if (width > 0.dp) {
                                        itemSize = (width - 8.dp * (boxCount - 1)) / boxCount
                                    }
                                },
                            maxItemsInEachRow = boxCount,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {

                            expensiveProducts.forEachIndexed { index, item ->
                                Box(
                                    modifier = Modifier
                                        .width(itemSize)
                                        .aspectRatio(2.5F)
                                ) {
                                    MainProductCard(
                                        ProductWithPrices(
                                            product = item.entity, priceInfoList = listOf()
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
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                        "En Düşük", style = titleStyle
                    )
                    Text(
                        it.min.price(), style = textStyle
                    )
                }
                Column(modifier = Modifier.padding(end = 12.dp)) {
                    Text(
                        "Ortalama", style = titleStyle
                    )
                    Text(
                        it.avg.price(), style = textStyle
                    )
                }
                Column {
                    Text(
                        "En Yüksek", style = titleStyle
                    )
                    Text(
                        it.max.price(), style = textStyle
                    )
                }
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
            ModalContent()

        }
    }

}

@Composable
fun ProductPriceStatGraph(
    stat: ProductWithStat,
    showPrices: Boolean = false,
    selected: (selected: ProductWithStat?) -> Unit = {}
) {
    var showInfo by remember { mutableStateOf(false) }
    LaunchedEffect(showInfo) {
        selected(if (showInfo) stat else null)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val isPressed = event.changes.any { it.pressed }
                        showInfo = isPressed
                    }
                }
            }) {
        val minSize by remember(stat) {
            mutableIntStateOf(
                stat.entity.price - min(stat.entity.price, stat.min)
            )
        }
        val maxSize by remember(stat) {
            mutableIntStateOf(
                max(stat.entity.price, stat.max) - stat.entity.price
            )
        }


        //calcaulate distances
        val maxDistance by remember(stat, minSize, maxSize) {
            val max = listOf(minSize, maxSize).max().toInt()
            mutableIntStateOf(if (max == 0) 1 else max)
        }

        val minRatio by remember(
            minSize, maxDistance
        ) { mutableFloatStateOf(if (maxDistance == 1) .5F else minSize.toFloat() / maxDistance.toFloat()) }
        val maxRatio by remember(
            maxSize, maxDistance
        ) { mutableFloatStateOf(if (maxDistance == 1) .5F else maxSize.toFloat() / maxDistance.toFloat()) }
        var width by remember { mutableIntStateOf(0) }
        var height by remember { mutableIntStateOf(0) }
        val errorContainer = MaterialTheme.colorScheme.error
        val primaryContainer = MaterialTheme.colorScheme.primary
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isPhone = this.maxWidth < 600.dp
            Column(
                modifier = Modifier.fillMaxWidth()
                    .padding(top = if (isPhone) 4.dp else 10.dp)
                    .border(.5.dp,MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    .padding(top = 1.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val titleHeight = with(LocalDensity.current) { 20.sp.toDp() }

                Text(
                    stat.entity.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = .5.dp)
                        .height(titleHeight),
                    overflow = TextOverflow.MiddleEllipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        fontSize = if (isPhone) 14.sp else 18.sp,
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
                            .height(if (isPhone) 20.dp else 30.dp)
                            .onGloballyPositioned {
                                width = it.size.width
                                height = it.size.height / 2
                            }) {
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
                                                width * minRatio.toFloat(), height.toFloat()
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
                                    .networkCachePolicy(CachePolicy.ENABLED).data(stat.entity.image)
                                    .crossfade(true).transformations(CoilTrimTransform())
                                    .error(Color.White.toArgb().toDrawable()).listener(
                                        onError = { _, throwable ->
                                            LogHelper.e("coil error", throwable.throwable)
                                        }).build()
                            )
                        }
                        Image(
                            painter = painter,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(if (isPhone) 20.dp else 30.dp)
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
}

@Composable
fun ModalContent() {
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
        Text(
            "Grafikler Nasıl Okunur?",
            modifier = Modifier.padding(bottom = 8.dp),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Text(
            "Fiyat grafikleri, ürünün şimdiki fiyatını fiyat çubuğu üzerinde ortalayarak gösterir. " + "Ürünün son fiyatı anlık fiyatına göre daha düşük olan ürünlerde grafik sağa yaslı olarak görüntülenir ve tüm zamanlara göre fiyatın ne kadar düşük olduğu gösterilmiş olur. ",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify
            )
        )
        val tempProduct = Product.fake().copy(
            image = ""
        )
        ProductPriceStatGraph(
            ProductWithStat(
                entity = tempProduct.copy(
                    price = 20000,
                    title = "Ortalama Fiyattan Ucuz Ürün",
                ),
                min = 1000, avg = 50000, max = 100000,
            )
        )

        Text(
            "Anlık fiyatın, ortalama fiyattan yüksek olduğu durumlarda grafik sola yaslı olarak görünür.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify
            )
        )
        ProductPriceStatGraph(
            ProductWithStat(
                entity = tempProduct.copy(
                    price = 80000,
                    title = "Ortalama Fiyattan Pahalı Ürün",
                ),
                min = 1000, avg = 50000, max = 100000,
            )
        )

        Text(
            "Anlık fiyatın en düşük değerde olduğu durumlarda grafik tamamen sağ tarafta yer alır.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify
            )
        )
        ProductPriceStatGraph(
            ProductWithStat(
                entity = tempProduct.copy(
                    price = 1000,
                    title = "En Ucuz Zamanında Olan Ürün",
                ),
                min = 1000, avg = 50000, max = 100000,
            )
        )
        Text(
            "Anlık fiyatın en yüksek değerde olduğu durumlarda grafik tamamen sol tarafta yer alır.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                textAlign = TextAlign.Justify
            )
        )
        ProductPriceStatGraph(
            ProductWithStat(
                entity = tempProduct.copy(
                    price = 100000,
                    title = "En Pahalı Zamanında Olan Ürün",
                ),
                min = 1000, avg = 50000, max = 100000,
            )
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            "Örnek Ürün", modifier = Modifier, style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            var prices by remember {
                mutableStateOf(
                    listOf(
                        Random.nextInt(100, 10000),
                        Random.nextInt(0, 10000),
                        Random.nextInt(0, 100),
                        Random.nextInt(0, 100),
                    )
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    nameValue(
                        "Anlık Fiyat",
                        prices[0] + (prices[1] * (prices[2].toFloat() / 100F)).toInt()
                    ),
                    modifier = Modifier.weight(1F),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Text(
                    nameValue(
                        "Ortalama",
                        prices[0] + (prices[1] * (prices[3].toFloat() / 100F)).toInt(),
                    ),
                    modifier = Modifier.weight(1F),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    nameValue("En Düşük", prices[0]),
                    modifier = Modifier.weight(1F),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Text(
                    nameValue("En Yüksek", prices[0] + prices[1]),
                    modifier = Modifier.weight(1F),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        prices = listOf(
                            Random.nextInt(100, 10000),
                            Random.nextInt(0, 10000),
                            Random.nextInt(0, 100),
                            Random.nextInt(0, 100),
                        )
                    },
                    contentPadding = PaddingValues(16.dp, 4.dp),
                    modifier = Modifier.height(25.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, "", modifier = Modifier.scale(.8F))
                        Text("Yenile")
                    }
                }
            }
            ProductPriceStatGraph(
                ProductWithStat(
                    entity = tempProduct.copy(
                        price = prices[0] + (prices[1] * (prices[2].toFloat() / 100F)).toInt(),
                        title = "Örnek Ürün",
                    ),
                    min = prices[0],
                    avg = prices[0] + (prices[1] * (prices[3].toFloat() / 100F)).toInt(),
                    max = prices[0] + prices[1]
                )
            )
        }

    }
}

fun nameValue(name: String, value: Int): AnnotatedString {
    return buildAnnotatedString {
        append("$name: ")
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.SemiBold, fontSize = 16.sp
            )
        ) {
            append(value.price())
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

@Preview(showBackground = true)
@Composable
fun LowPricedScreenModelContentPreview(model: LowPricedViewModel = viewModel()) {
    PreviewProviders {
        ModalContent()
    }
}