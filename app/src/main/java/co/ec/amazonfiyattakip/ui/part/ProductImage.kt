package co.ec.amazonfiyattakip.ui.part

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.amazonfiyattakip.ui.PreviewProviders
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun ProductImage(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    ProductImage(
        image = product.image,
        title = product.title,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
fun ProductImage(
    image: String,
    title: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    showGradient: Boolean = true
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
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CardDefaults.cardColors().containerColor.copy(alpha = 1F),
                                Color.Transparent
                            )
                        )
                    )
            )
        }


    }
}

@Composable
@Preview(showBackground = true)
fun ProductImagePreview() {
    PreviewProviders {
        ProductImage(
            product = Product.fake(),
            modifier = Modifier
                .width(40.dp)
                .aspectRatio(1F)
        )
    }
}