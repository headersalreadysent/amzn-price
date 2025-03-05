package co.ec.amazonfiyattakip.composables

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.ec.amazonfiyattakip.db.product.Product
import co.ec.amazonfiyattakip.ui.PreviewProviders

@Composable
fun ProductStat(
    product: Product,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified
) {

    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val density = LocalDensity.current
        Icon(
            Icons.Filled.Star, "",
            modifier = Modifier.height(with(density) { 12.sp.toDp() }),
            tint = color
        )
        Text(
            product.star.toString(),
            fontSize = 10.sp,
            color = color
        )
        VerticalDivider(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .fillMaxHeight(.5F)
        )
        Icon(
            Icons.Filled.ChatBubble, "",
            modifier = Modifier.height(with(density) { 12.sp.toDp() }),
            tint = color
        )
        Text(
            product.comment.toString(),
            fontSize = 10.sp,
            color = color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductStatPreview(){
    PreviewProviders {
        ProductStat(product = Product.fake())
    }
}