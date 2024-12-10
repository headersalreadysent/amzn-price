package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.ui.LocalNavigation
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.amazonfiyattakip.ui.part.TitleBar
import co.ec.amazonfiyattakip.ui.part.graph.PriceBar
import co.ec.helper.utils.dateString
import coil.compose.rememberAsyncImagePainter

@Composable
fun MainScreen(model: MainScreenModel = viewModel()) {
    val products by model.products.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp)) {
            TitleBar(title = "Son Takipler")
            products?.forEach {
                ProductLine(it)
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
            .padding(bottom = 8.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer,
                RoundedCornerShape(4.dp)
            )
            .clickable {
                navigator.navigate("detail/${product.product.id}")
            }
    ) {
        Box(modifier = Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)) {
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


@Composable
@Preview(showBackground = true)
fun MainScreenPreview(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        MainScreen(model)
    }
}

@Composable
@Preview(showBackground = true)
fun ProductLinePreview(model: MainScreenModel = viewModel()) {
    model.emulate()
    PreviewProviders {
        model.products.value?.let {
            ProductLine(it[0])
        }
    }
}
