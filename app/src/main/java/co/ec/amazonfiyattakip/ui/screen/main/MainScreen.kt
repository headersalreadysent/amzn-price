package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.composables.CutCorner
import co.ec.amazonfiyattakip.composables.CutCornerCard
import co.ec.amazonfiyattakip.composables.CutInput
import co.ec.amazonfiyattakip.composables.cutShape
import co.ec.amazonfiyattakip.db.LatestUpdate
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.helper.price
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.LocalSettings
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraph
import co.ec.amazonfiyattakip.ui.part.graph.PriceGraphPair
import co.ec.helper.AppLogger
import co.ec.helper.utils.dateString
import co.ec.helper.utils.timeString
import kotlin.math.floor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {
    val navigator = LocalNavigation.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        var selectValue by remember { mutableStateOf<PriceGraphPair?>(null) }
        val dailyTotals by model.dailyTotals.observeAsState(listOf())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 16.dp)
            ) {
                Text(
                    text = "Sepet Toplamı",
                    style = MaterialTheme.typography.bodySmall
                )
                if(dailyTotals.isNotEmpty()){

                    Text(
                        text = (selectValue?.price?.times(100) ?: dailyTotals?.last()?.total
                        ?: 0).toInt()
                            .price(),
                        style = MaterialTheme.typography.titleLarge
                            .copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 45.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                    )
                }
                Text(
                    text = (selectValue?.date?.dateString() ?: "Bugün"),
                    style = MaterialTheme.typography.bodySmall
                )

            }
            Row(
                modifier = Modifier.weight(1F),
                horizontalArrangement = Arrangement.End
            ) {

                val products by model.products.observeAsState()
                products?.forEach {
                    ProductImage(
                        title = it.product.title,
                        image = it.product.image,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(start = 2.dp)
                            .size(35.dp)
                            .clip(CircleShape),
                        showGradient = false
                    )
                }

            }
        }

        CutCornerCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.onPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            corner = CutCorner.TOPRIGHT,
            cutSize = 20.dp
        ) {
            dailyTotals?.let {
                if (it.size > 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(3F)
                    ) {
                        PriceGraph(
                            prices = it.map {
                                PriceGraphPair(it.date, it.total / 100F)
                            },
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = .6F
                            ),
                            onDrag = {
                                selectValue = it
                            }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .fillMaxWidth()
                )
                val stats by model.stats.observeAsState()
                stats?.let {
                    if (it["product"]!! > 0) {
                        FlowRow(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .padding(top = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            maxItemsInEachRow = 2
                        ) {
                            TopInfoBox(
                                icon = Icons.Filled.Search,
                                title = "Takip",
                                value = it["product"].toString()
                            )
                            TopInfoBox(
                                icon = Icons.Filled.Update,
                                title = "Güncelleme",
                                value = it["update"].toString()
                            )
                        }
                    }


                }
                var asinCode by remember { mutableStateOf("") }
                CutInput(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    value = asinCode,
                    valueChange = {
                        asinCode=it
                    },
                    action = "Ekle",
                    placeholder = "ASIN ile takip et",
                    height=50.dp,
                    icon = Icons.Filled.Code,
                    click = {

                    }
                )

                model.latestUpdates?.let { state ->
                    Column(modifier = Modifier.padding()) {
                        val latestUpdates by state.collectAsState()
                        latestUpdates.let { updates ->
                            if (updates.isNotEmpty()) {
                                TitleBar(
                                    title = "Son Güncellemeler",
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(updates.size) {
                                        LatestUpdate(
                                            updates[it],
                                            isFirst = it == 0
                                        ) {
                                            navigator.navigate("detail/${it.productId}")
                                        }

                                    }
                                }
                                HorizontalDivider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = 8.dp,
                                            vertical = 5.dp
                                        )
                                )
                            }

                        }
                    }

                }


                val products by model.products.observeAsState()
                products?.let {
                    if (it.isNotEmpty()) {
                        TitleBar(
                            title = "Son Takipler",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        it.forEach {
                            ProductLine(it)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1F)
                                .fillMaxWidth(), Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hiç Ürün Bulunmuyor",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                val settings = LocalSettings.current
                                val navigation = LocalNavigation.current
                                Button(onClick = {
                                    model.addOneDeal {
                                        settings.putString("sharedUrl", it)
                                        navigation.navigate("add")
                                    }
                                }) {
                                    Text(text = "Fırsat Ürünlerinden ekle")
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
fun ProductLine(product: ProductWithPrices) {
    val navigator = LocalNavigation.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer,
                cutShape(CutCorner.BOTTOMRIGHT, 30.dp)
            )
            .clip(cutShape(CutCorner.BOTTOMRIGHT, 30.dp))
            .clickable {
                navigator.navigate("detail/${product.product.id}")
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            ProductImage(
                product.product,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth(.3F)
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth(.75F)
                    .padding(start = 8.dp)
                    .padding(6.dp)
            ) {
                Text(
                    text = product.product.title,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.product.shortDesc(),
                    style = MaterialTheme.typography.bodySmall
                        .copy(
                            color = MaterialTheme.typography.bodySmall.color.copy(alpha = .8F)
                        )
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    val min = product.priceInfoList.minByOrNull { it.price }
                    val max = product.priceInfoList.maxByOrNull { it.price }
                    Column(
                        modifier = Modifier
                            .weight(1F)
                            .padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Şu an",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = product.product.price(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        if (min != null && max != null) {
                            PriceBar(
                                min.price.toFloat(),
                                max.price.toFloat(),
                                product.product.price.toFloat()
                            )
                        }
                    }
                    min?.let {
                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "En Düşük",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = min.date.dateString("dd.MM.YYYY"),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = min.price(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                    max?.let {
                        Column(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "En Yüksek",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = max.date.dateString("dd.MM.yyy"),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = max.price(),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowScope.TopInfoBox(
    modifier: Modifier = Modifier,
    title: String = " ",
    value: String = " ",
    icon: ImageVector? = null,
) {
    CutCornerCard(
        modifier = Modifier
            .weight(1F)
            .padding(vertical = 8.dp)
            .then(modifier),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .scale(1.1F)
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(
                            alpha = .8F
                        )
                    )
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

    }
}

@Composable
fun LazyItemScope.LatestUpdate(
    update: LatestUpdate,
    isFirst: Boolean = false,
    onClick: (update: LatestUpdate) -> Unit = {}
) {
    val cutShape = cutShape(corner = CutCorner.BOTTOMRIGHT)

    Box(
        modifier = Modifier
            .then(if (isFirst) Modifier.padding(start = 8.dp) else Modifier)
            .fillParentMaxWidth(.30F)
            .aspectRatio(1F)
            .padding(vertical = 8.dp)
            .clickable {
                onClick(update)
            }
            .border(2.dp, MaterialTheme.colorScheme.surfaceVariant, cutShape)
            .background(MaterialTheme.colorScheme.secondaryContainer, cutShape)
            .clip(cutShape)

    ) {
        ProductImage(
            image = update.image, title = update.title,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent, cutShape)
                .blur(1.dp),
            showGradient = false
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = .5F))
        )
        /* Text(
             text = update.title,
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(8.dp)
                 .background(
                     MaterialTheme.colorScheme.surface.copy(alpha = .9F),
                     cutShape(corner = CutCorner.TOPLEFT)
                 )
                 .padding(top = 8.dp)
                 .padding(horizontal = 4.dp)
                 .padding(bottom = 4.dp),
             style = MaterialTheme.typography.bodyMedium.copy(
                 textAlign = TextAlign.Justify,
                 fontWeight = FontWeight.SemiBold,
                 color = MaterialTheme.colorScheme.primary
             ),
             maxLines = 2,
             overflow = TextOverflow.Ellipsis
         )*/
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = .9F)
                )
                .padding(top = 4.dp)
                .padding(horizontal = 4.dp)
                .padding(bottom = 4.dp),
            horizontalAlignment = Alignment.End
        ) {

            Text(
                text = update.date.timeString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 10.sp
                )
            )
            Text(
                text = update.price.price(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }


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


