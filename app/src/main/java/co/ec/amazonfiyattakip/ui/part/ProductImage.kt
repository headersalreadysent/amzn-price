package co.ec.amazonfiyattakip.ui.part

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.helper.helpers.LogHelper

import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ProductImage(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showGradient: Boolean = true,
    radialGradient: Boolean = false,
    color: Color = CardDefaults.cardColors().containerColor
) {
    ProductImage(
        image = product.image,
        title = product.title,
        modifier = modifier,
        contentScale = contentScale,
        showGradient = showGradient,
        color = color,
        radialGradient = radialGradient

    )
}

@Composable
fun ProductImage(
    image: String,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showGradient: Boolean = true,
    radialGradient: Boolean = false,
    color: Color = CardDefaults.cardColors().containerColor
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        val painter = if (LocalInspectionMode.current) {
            // Show placeholder in Preview
            ColorPainter(MaterialTheme.colorScheme.primary)
        } else {
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .transformations(CoilTrimTransform())
                    .error(ColorDrawable(Color.White.toArgb()))
                    .listener(
                        onError = { _, throwable ->
                            LogHelper.e("coil error",throwable.throwable)
                        }
                    )
                    .build()
            )
        }
        Image(
            painter = painter,
            contentDescription = title,
            contentScale = contentScale,
            modifier = Modifier.matchParentSize()
        )
        if (showGradient) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if(radialGradient){
                            Modifier.drawBehind {
                                val gradient = Brush.radialGradient(
                                    0.0f to Color.Transparent,
                                    .85F to color.copy(alpha = .2F),
                                    1.0f to color.copy(alpha = 1F),
                                    center = Offset(size.width, 0f), // Move center to top-end
                                    radius = size.minDimension
                                )
                                drawRect(gradient)
                            }
                        } else {
                            Modifier.background(
                                Brush.horizontalGradient(
                                    0F to color.copy(alpha = 1F),
                                    0.15F to color.copy(alpha = .8F),
                                    1.0f to Color.Transparent
                                )
                            )
                        }
                    )



            )

        }


    }
}

@Composable
@Preview(showBackground = true)
private fun ProductImagePreview() {
    PreviewProviders {
        Column {

            ProductImage(
                product = Product.fake(),
                modifier = Modifier
                    .width(40.dp)
                    .aspectRatio(1F)
            )
            Spacer(modifier = Modifier.fillMaxWidth().height(16.dp))

            ProductImage(
                product = Product.fake(),
                modifier = Modifier
                    .width(40.dp)
                    .aspectRatio(1F),
                radialGradient = true
            )
        }
    }
}