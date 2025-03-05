package co.ec.amazonfiyattakip.ui.part

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders

@Composable
fun LittleProductBox(
    product: Product,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.tertiaryContainer,
    contentColor :Color= MaterialTheme.colorScheme.onTertiaryContainer,
    onClick: (() -> Unit)? = null
) {
    Box(modifier = Modifier
        .fillMaxWidth(.5F)
        .padding(4.dp)
        .aspectRatio(2.5F)
        .background(containerColor)
        .border(1.dp, contentColor.copy(alpha = .2F))
        .clickable(enabled = onClick != null) {
            onClick?.let {
                it()
            }
        }
        .then(modifier)) {
        ProductImage(
            product,
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(.8F)
                .align(Alignment.CenterEnd)
                .alpha(.9F),
            color = containerColor
        )
        Text(
            product.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .padding(end = 16.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(MaterialTheme.colorScheme.tertiary, Offset(3F, 3F), 1F)
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            product.price(),
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.BottomStart),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = contentColor,
                fontWeight = FontWeight.Bold,
            )
        )

    }
}

@Preview(showBackground = true)
@Composable
private fun LittleProductBoxPreview() {
    PreviewProviders {
        LittleProductBox(Product.fake())
    }
}