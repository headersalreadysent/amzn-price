package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.service.AmznScrape
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage

@Composable
fun ProductBox(
    product: Product,
    height: Dp? = null
) {

    val urlHandler = LocalUriHandler.current
    val density = LocalDensity.current
    val container = MaterialTheme.colorScheme.secondaryContainer
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                urlHandler.openUri(AmznScrape.urlFromAsin(product.asin))
            }
            .padding(bottom = 8.dp)
            .background(container)
            .drawBehind {
                drawIntoCanvas {
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        setShadowLayer(30f, 0f, 10f, Color.Black.copy(.8F).toArgb())
                        color = container.toArgb()
                    }
                    it.nativeCanvas.drawRect(
                        0f, 0f, size.width, size.height, paint
                    )
                }
            }

            .then(
                if (height == null)
                    Modifier.aspectRatio(2.5F)
                else
                    Modifier.height(height)
            )
    ) {
        ProductImage(
            product,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1F)
                .align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(start = 16.dp)
                .padding(bottom = 4.dp)
                .statusBarsPadding()

        ) {
            var titleSize by remember { mutableStateOf(0.sp) }
            var titleLineHeightSize by remember { mutableStateOf(0.sp) }

            Row(
                modifier = Modifier
                    .fillMaxWidth(.9F)
                    .weight(4F)
                    .onGloballyPositioned {
                        with(density) {
                            val size = it.size.height.toDp().toSp()
                            if (size * .4 < 20.sp) {
                                return@onGloballyPositioned
                            }
                            titleSize = size * .40
                            titleLineHeightSize = size * .45
                        }
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicText(
                    modifier = Modifier.fillMaxWidth(),
                    text = product.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.SemiBold,
                        shadow = Shadow(
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                alpha = .5F
                            ), Offset(1F, 1F), 1F
                        ),

                        fontSize = titleSize,
                        lineHeight = titleLineHeightSize
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }


            var textSize by remember { mutableStateOf(0.sp) }
            Row(
                modifier = Modifier
                    .weight(3F)
                    .onGloballyPositioned {
                        textSize = density.run {
                            val height = it.size.height.toDp().toSp()
                            if (height < 20.sp) 16.sp else height * .8
                        }
                    },
                verticalAlignment = Alignment.Bottom
            ) {
                BasicText(
                    text = product.price(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Medium,
                        fontSize = textSize
                    ),
                    maxLines = 1,
                )
                Text(
                    modifier = Modifier
                        .wrapContentSize()

                        .padding(start = 4.dp),
                    text = product.asin,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 11.sp
                    )
                )
            }


        }

    }
}


@Preview(showBackground = true)
@Composable
private fun ProductBoxPreview() {
    PreviewProviders {
        Column {

            ProductBox(Product.fake())

            ProductBox(Product.fake(), 90.dp)
        }
    }
}