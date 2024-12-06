package co.ec.amazonfiyattakip.ui.screen.main

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ec.amazonfiyattakip.db.ProductWithPrices
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
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
        products?.forEach {
            ProductLine(it)
        }
    }
}

@Composable
fun ProductLine(product: ProductWithPrices) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(8.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(product.product.image),
            contentDescription = product.product.title,
            modifier = Modifier
                .weight(1F)
                .aspectRatio(1F)
        )
        Column(
            modifier = Modifier
                .weight(4F)
                .padding(start = 8.dp)
                .padding(2.dp)
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
                        PriceCanvas(
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

@Composable
fun PriceCanvas(minPrice: Float, maxPrice: Float, currentPrice: Float) {
    val triangleSize = 8.dp
    val primary = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val startX = 8F
        val endX = canvasWidth - 8f

        // Calculate positions for prices
        val currentX = startX + (currentPrice - minPrice) / (maxPrice - minPrice) * (size.width-16F)

        // Draw line
        drawLine(
            color = Color.Gray,
            start = Offset(startX, canvasHeight / 2),
            end = Offset(endX, canvasHeight / 2),
            strokeWidth = 4f
        )

        // Draw min point
        drawCircle(
            color = Color.Gray,
            radius = 8f,
            center = Offset(startX, canvasHeight / 2)
        )

        // Draw max point
        drawCircle(
            color = Color.Gray,
            radius = 8f,
            center = Offset(endX, canvasHeight / 2)
        )

        // Draw current price triangle
        drawPath(
            path = Path().apply {
                moveTo(currentX, canvasHeight / 2 - triangleSize.toPx() / 2)
                lineTo(
                    currentX - triangleSize.toPx() / 2,
                    canvasHeight / 2 + triangleSize.toPx() / 2
                )
                lineTo(
                    currentX + triangleSize.toPx() / 2,
                    canvasHeight / 2 + triangleSize.toPx() / 2
                )
                close()
            },
            color = primary
        )
    }
}