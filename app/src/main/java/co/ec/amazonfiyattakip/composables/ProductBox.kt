package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders
import co.ec.amazonfiyattakip.ui.part.ProductImage
import co.ec.helper.composable.AutoText

@Composable
fun ProductBox(
    product: Product,
    modifier: Modifier = Modifier,
    type: String = "box"
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .aspectRatio(if (type == "box") 2F else 3F)
            .shadow(1.dp)
            .then(modifier)
    ) {
        ProductImage(
            product,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth(.5F)
                .aspectRatio(1F)
                .align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.secondaryContainer
        )
        Column(
            modifier = Modifier
                .fillMaxWidth(if (type == "box") .8F else 1F)
                .fillMaxHeight()
                .padding(horizontal = 16.dp)
                .then(if (type == "box") Modifier.statusBarsPadding() else Modifier.padding(top = 8.dp))
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = if (type == "box") 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                text = product.asin,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
            AutoText(
                modifier = Modifier.fillMaxWidth(),
                text = product.price(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
            if (type == "box") {

                ProductStat(
                    product, color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun ProductBoxPreview() {
    PreviewProviders {
        ProductBox(Product.fake())
    }
}