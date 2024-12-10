package co.ec.amazonfiyattakip.ui.part

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.helper.CoilTrimTransform
import co.ec.amazonfiyattakip.ui.PreviewProviders
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProductImage(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale:ContentScale = ContentScale.Crop,
) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(product.image)
            .crossfade(true)
            .transformations(CoilTrimTransform())
            .build(),
        contentDescription = product.title,
        contentScale = contentScale,
        modifier = modifier
    )
}

@Composable
@Preview(showBackground = true)
fun ProductImagePreview(){
    PreviewProviders {
        ProductImage(product = Product.fake())
    }
}